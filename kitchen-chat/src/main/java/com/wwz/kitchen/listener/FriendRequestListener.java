package com.wwz.kitchen.listener;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import com.wwz.kitchen.business.dto.FriendshipRabbitDto;
import com.wwz.kitchen.business.enums.FriendshipStatusEnum;
import com.wwz.kitchen.business.enums.MessageTypeEnum;
import com.wwz.kitchen.business.enums.ShareStatusEnum;
import com.wwz.kitchen.business.enums.StatusEnum;
import com.wwz.kitchen.business.service.KitchenFriendRequestService;
import com.wwz.kitchen.business.service.KitchenFriendshipService;
import com.wwz.kitchen.business.service.KitchenMenuService;
import com.wwz.kitchen.business.service.KitchenMenusShareService;
import com.wwz.kitchen.persistence.beans.KitchenFriendRequest;
import com.wwz.kitchen.persistence.beans.KitchenFriendship;
import com.wwz.kitchen.persistence.beans.KitchenMenu;
import com.wwz.kitchen.persistence.beans.KitchenMenusShare;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


/**
 * 用于处理好友请求的
 * Created by wenzhi.wang.
 * on 2024/11/23.
 */
@Service
@Slf4j
public class FriendRequestListener {
    private static final String REQUEST_QUEUE = "friend_request_queue";

    @Autowired
    private KitchenFriendshipService kitchenFriendshipService;
    @Autowired
    private KitchenFriendRequestService kitchenFriendRequestService;
    @Autowired
    private KitchenMenusShareService kitchenMenusShareService;
    @Autowired
    private KitchenMenuService kitchenMenuService;

    @RabbitListener(queues = REQUEST_QUEUE, ackMode = "MANUAL")  // 使用手动确认模式
    public void receiveFriendRequest(FriendshipRabbitDto message, Channel channel, Message messageObj) throws IOException {
        try {
            Integer messageType = message.getType();
            MessageTypeEnum messageTypeEnum = MessageTypeEnum.fromValue(messageType);
            boolean isSuccess = false;

            switch (messageTypeEnum) {
                case REQUEST_FRIEND: // 请求
                    // 好友请求，更新数据库
                    isSuccess = handleFriendRequest(message);
                    break;
                case DO_REQUEST_FRIEND: // 处理请求
                    // 处理加好友
                    isSuccess = handleDoFriendRequest(message);
                    break;
                case DELETE_FRIEND: // 删除好友后异步处理旧的相关好友请求  真删除
                    isSuccess = handleDeleteFriendRequest(message);
                    break;
                case REQUEST_SHARE_MENU: // 好友请求共享菜单
                    isSuccess = handleFriendShareRequest(message);
                    break;
                case DO_REQUEST_SHARE_MENU: // 处理好友请求共享菜单 并且双向共享
                    isSuccess = handleDoFriendShareRequest(message);
                    break;
            }
            if (isSuccess) {
                // 自动确认处理成功的消息，不做额外操作
                log.info("Message processed successfully, auto-acknowledged.");
                channel.basicAck(messageObj.getMessageProperties().getDeliveryTag(), false);
            } else {
                channel.basicNack(messageObj.getMessageProperties().getDeliveryTag(), false, true);
                // 处理失败，不回退消息，不重新入队
                log.warn("Message processing failed, not requeued.");
            }
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage());
            // 处理失败，不回退消息，不重新入队
            try {
                // 消息重新入队，避免丢失
                channel.basicNack(messageObj.getMessageProperties().getDeliveryTag(), false, true);
            } catch (IOException ex) {
                log.error("Error when nack message: {}", ex.getMessage());
            }
            log.error("Message not requeued after failure.");
        }
    }


    @Transactional
    public boolean handleDoFriendShareRequest(FriendshipRabbitDto message) {
        Integer sendUid = message.getSendUid();
        Integer receiveUid = message.getReceiveUid();
        boolean isAccept = message.isAccept();

        List<KitchenMenusShare> list = kitchenMenusShareService.listBySendUIdAndReceiveUid(sendUid,receiveUid);

        if (isAccept) {//同意
            AtomicBoolean flag = new AtomicBoolean(false);
            if (list == null || list.isEmpty()) {
                KitchenMenusShare kitchenMenusShare = new KitchenMenusShare();
                kitchenMenusShare.setShareFromUid(receiveUid);
                kitchenMenusShare.setShareToUid(sendUid);
                kitchenMenusShare.setStatus(ShareStatusEnum.CONFIRM.getCode());
                flag .set(kitchenMenusShareService.saveMenusShare(kitchenMenusShare));
            } else {
                list.forEach(kitchenMenusShare -> {
                    kitchenMenusShare.setStatus(ShareStatusEnum.CONFIRM.getCode());
                    flag.set(kitchenMenusShareService.updatMenusShare(kitchenMenusShare));
                });
            }
            return flag.get();
        } else {//拒绝
            AtomicBoolean flag = new AtomicBoolean(false);
            if (list.isEmpty() || list == null) {
                flag.set(true);
            } else {
                list.forEach(kitchenMenusShare -> {
                    kitchenMenusShare.setStatus(ShareStatusEnum.REFUSED.getCode());
                    flag.set(kitchenMenusShareService.updatMenusShare(kitchenMenusShare));
                });
            }
            if (flag.get()) {
                return true;
            }
        }
        return false;
    }

    private boolean handleFriendShareRequest(FriendshipRabbitDto message) {
        QueryWrapper<KitchenMenusShare> kitchenMenusShareQueryWrapper = new QueryWrapper<>();
        kitchenMenusShareQueryWrapper.lambda()
                .eq(KitchenMenusShare::getShareFromUid, message.getSendUid())
                .eq(KitchenMenusShare::getShareToUid, message.getReceiveUid());
        KitchenMenusShare one = kitchenMenusShareService.getOne(kitchenMenusShareQueryWrapper);
        if (one == null) {
            one  = new KitchenMenusShare();
            one.setShareFromUid(message.getSendUid());
            one.setShareToUid(message.getReceiveUid());
            one.setStatus(ShareStatusEnum.NOT_CONFIRM.getCode());
            boolean save = kitchenMenusShareService.saveMenusShare(one);
            if (save) {
                return true;
            }
        }
        if (one != null && one.getStatus() != ShareStatusEnum.REFUSED.getCode()) {
            one.setStatus(ShareStatusEnum.NOT_CONFIRM.getCode());
            boolean b = kitchenMenusShareService.updatMenusShare(one);
            if (b) {
                return true;
            }
        }
        return true;
    }

    private boolean handleDeleteFriendRequest(FriendshipRabbitDto message) {
        boolean a = true;
        boolean b = true;
        Integer sendUid = message.getSendUid();
        Integer receiveUid = message.getReceiveUid();
        // 处理第一个请求
        a = removeFriendRequest(receiveUid, sendUid);

        // 处理第二个请求
        b = removeFriendRequest(sendUid, receiveUid);

        return a && b;
    }

    private boolean removeFriendRequest(Integer senderId, Integer receiverId) {
        QueryWrapper<KitchenFriendRequest> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(KitchenFriendRequest::getSenderId, senderId);
        queryWrapper.lambda().eq(KitchenFriendRequest::getReceiverId, receiverId);

        KitchenFriendRequest kitchenFriendRequest = kitchenFriendRequestService.getOne(queryWrapper);
        if (kitchenFriendRequest != null && kitchenFriendRequest.getStatus() != FriendshipStatusEnum.TO_BE_CONFIRMED.getValue()) {
            return kitchenFriendRequestService.removeById(kitchenFriendRequest);
        }
        return true; // 如果没有找到请求或状态为待确认，不做删除，返回 true
    }

    //处理好友申请
    @Transactional
    public boolean handleDoFriendRequest(FriendshipRabbitDto message) {
        Integer sendUid = message.getSendUid();
        Integer receiveUid = message.getReceiveUid();
        QueryWrapper<KitchenFriendRequest> kitchenFriendRequestQueryWrapper = new QueryWrapper<>();
        kitchenFriendRequestQueryWrapper.lambda().eq(KitchenFriendRequest::getSenderId, receiveUid);//这里是处理receiveUid发送的请求 所以查询SenderId是我们处理的的receiveUid（friendId）
        kitchenFriendRequestQueryWrapper.lambda().eq(KitchenFriendRequest::getReceiverId,sendUid);
        KitchenFriendRequest kitchenFriendRequest = kitchenFriendRequestService.getOne(kitchenFriendRequestQueryWrapper);
        if(!message.isAccept()) {//不同意
            if(kitchenFriendRequest == null) {
                return true;
            }
            kitchenFriendRequest.setStatus(FriendshipStatusEnum.REFUSE.getValue());
            boolean update = kitchenFriendRequestService.updateById(kitchenFriendRequest);
            return update;
        } else { //同意
            boolean update = true;
            boolean save = true;
            boolean update1 = true;
            boolean update2 = true;
            boolean save1 = true;
            if(kitchenFriendRequest != null) {
                kitchenFriendRequest.setStatus(FriendshipStatusEnum.CONFIRMED.getValue());
                update = kitchenFriendRequestService.updateById(kitchenFriendRequest);
            }
            //正向
            QueryWrapper<KitchenFriendship> kitchenFriendshipQueryWrapper = new QueryWrapper<>();
            kitchenFriendshipQueryWrapper.lambda().eq(KitchenFriendship::getUid,message.getReceiveUid());
            kitchenFriendshipQueryWrapper.lambda().eq(KitchenFriendship::getFriendId,message.getSendUid());
            KitchenFriendship kitchenFriendship = kitchenFriendshipService.getOne(kitchenFriendshipQueryWrapper);
            if(kitchenFriendship == null) {
                kitchenFriendship = new KitchenFriendship();
                kitchenFriendship.setUid(receiveUid);
                kitchenFriendship.setFriendId(sendUid);
                kitchenFriendship.setStatus(FriendshipStatusEnum.CONFIRMED.getValue());
                save = kitchenFriendshipService.save(kitchenFriendship);
            } else {
                kitchenFriendship.setStatus(FriendshipStatusEnum.CONFIRMED.getValue());
                update1 = kitchenFriendshipService.updateById(kitchenFriendship);
            }
            //反向
            QueryWrapper<KitchenFriendship> kitchenFriendshipQueryWrapper1 = new QueryWrapper<>();
            kitchenFriendshipQueryWrapper1.lambda().eq(KitchenFriendship::getUid,message.getSendUid());
            kitchenFriendshipQueryWrapper1.lambda().eq(KitchenFriendship::getFriendId,message.getReceiveUid());
            KitchenFriendship KitchenFriendship1 = kitchenFriendshipService.getOne(kitchenFriendshipQueryWrapper1);
            if(KitchenFriendship1 == null) {
                KitchenFriendship1 = new KitchenFriendship();
                KitchenFriendship1.setUid(sendUid);
                KitchenFriendship1.setFriendId(receiveUid);
                KitchenFriendship1.setStatus(FriendshipStatusEnum.CONFIRMED.getValue());
                save1 = kitchenFriendshipService.save(KitchenFriendship1);
            } else {
                KitchenFriendship1.setStatus(FriendshipStatusEnum.CONFIRMED.getValue());
                update2 = kitchenFriendshipService.updateById(KitchenFriendship1);
            }
            if (save && update && update1 && update2 && save1) {
                return true;
            }
        }
        return false;
    }

    //好友申请
    private boolean handleFriendRequest(FriendshipRabbitDto message) {
        QueryWrapper<KitchenFriendship> kitchenFriendshipQueryWrapper = new QueryWrapper<>();
        kitchenFriendshipQueryWrapper.lambda().eq(KitchenFriendship::getUid,message.getSendUid());
        kitchenFriendshipQueryWrapper.lambda().eq(KitchenFriendship::getFriendId,message.getReceiveUid());
        kitchenFriendshipQueryWrapper.lambda().eq(KitchenFriendship::getStatus,FriendshipStatusEnum.CONFIRMED.getValue());
        KitchenFriendship oldKitchenFriendship = kitchenFriendshipService.getOne(kitchenFriendshipQueryWrapper);
        if (oldKitchenFriendship != null) {
            return true;
        }
        QueryWrapper<KitchenFriendRequest> kitchenFriendRequestQueryWrapper = new QueryWrapper<>();
        kitchenFriendRequestQueryWrapper.lambda().eq(KitchenFriendRequest::getSenderId, message.getSendUid());
        kitchenFriendRequestQueryWrapper.lambda().eq(KitchenFriendRequest::getReceiverId,message.getReceiveUid());
        KitchenFriendRequest oldKitchenFriendRequest = kitchenFriendRequestService.getOne(kitchenFriendRequestQueryWrapper);
        if (oldKitchenFriendRequest != null) {
            oldKitchenFriendRequest.setStatus(FriendshipStatusEnum.TO_BE_CONFIRMED.getValue());
            boolean update = kitchenFriendRequestService.updateById(oldKitchenFriendRequest);
            return update;
        }
        oldKitchenFriendRequest = new KitchenFriendRequest();
        oldKitchenFriendRequest.setReceiverId(message.getReceiveUid());
        oldKitchenFriendRequest.setSenderId(message.getSendUid());
        oldKitchenFriendRequest.setStatus(FriendshipStatusEnum.TO_BE_CONFIRMED.getValue());
        boolean save = kitchenFriendRequestService.save(oldKitchenFriendRequest);
        return save;
    }
}
