package com.wwz.kitchen.business.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wwz.kitchen.business.dto.ChatMessageDTO;
import com.wwz.kitchen.business.enums.ChatMessageReadStatusEnum;
import com.wwz.kitchen.business.enums.MessageTypeEnum;
import com.wwz.kitchen.business.enums.StatusEnum;
import com.wwz.kitchen.business.service.KitchenMessageService;
import com.wwz.kitchen.persistence.beans.KitchenMessage;
import com.wwz.kitchen.persistence.mapper.KitchenMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-21
 */
@Service
@Slf4j
public class KitchenMessageServiceImpl extends ServiceImpl<KitchenMessageMapper, KitchenMessage> implements KitchenMessageService {


    @Autowired
    private KitchenMessageMapper kitchenMessageMapper;
    // 创建 SimpleDateFormat 对象，指定格式
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
    public Page<ChatMessageDTO> getChatHistoryMessage(Integer uid, Integer friendId, Integer lastMessageId, int size) {
        Page<ChatMessageDTO> pageForChatMessageDTO = new Page<>();
        try{
            // 如果是首次加载聊天记录，则传入的 lastSendTime 为 null，我们设置最大时间戳来获取最新消息
            if (lastMessageId == null) {
                lastMessageId = Integer.MAX_VALUE;  // 默认最大时间戳，获取最新的消息
            }
            QueryWrapper<KitchenMessage> qw = new QueryWrapper<>();
            qw.lambda().eq(KitchenMessage::getStatus, StatusEnum.RELEASE.toString()) // 查询可用的消息
                    .in(KitchenMessage::getSenderId, uid, friendId) // senderID 为 uid 或 friendId
                    .in(KitchenMessage::getReceiverId, uid, friendId) // receiverID 为 uid 或 friendId
                    .lt(KitchenMessage::getId, lastMessageId)  // 按时间戳过滤，获取小于 lastSendTime 的消息
                    .orderByDesc(KitchenMessage::getCreateTime); // 按发送时间升序排序 前端展示时时最新消息append在最底端 故时间正序
            // 创建 Page 对象，当前页码，页大小
            Page<KitchenMessage> pageBean = new Page<>(1, size);
            Page<KitchenMessage> pageForKitchenMessage = this.page(pageBean, qw);

            // 将 KitchenMessage 转换为 KitchenMessageDTO
            List<ChatMessageDTO> kitchenMessageDtoList = pageForKitchenMessage.getRecords().stream()
                    .map(kitchenMessage -> {
                        ChatMessageDTO dto = new ChatMessageDTO();
                        dto.setId(kitchenMessage.getId());
                        dto.setSender(kitchenMessage.getSenderId());
                        dto.setReceiver(kitchenMessage.getReceiverId());
                        dto.setContent(kitchenMessage.getContent());
                        dto.setType(MessageTypeEnum.SEND_MESSAGE.getCode());
                        dto.setMessageContentType(kitchenMessage.getMessageType());//消息类型 1文本、2图片、3站内信息推送、4请求共享自定义信息
                        dto.setSendTime(sdf.format(kitchenMessage.getCreateTime()));
                        return dto;
                    }).collect(Collectors.toList());

            pageForChatMessageDTO.setRecords(kitchenMessageDtoList);
            pageForChatMessageDTO.setTotal(pageForKitchenMessage.getTotal());
            pageForChatMessageDTO.setCurrent(pageForKitchenMessage.getCurrent());
            pageForChatMessageDTO.setPages(pageForKitchenMessage.getPages());

        } catch (Exception e) {
            log.error("KitchenMessageServiceImpl=====getChatHistoryMessage:{}", e.getMessage());
        }
        return pageForChatMessageDTO;
    }

    @Override
    public Integer getUnreadMessagesByFriendId(Integer uid, Integer friendId) {
        try {
            QueryWrapper<KitchenMessage> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(KitchenMessage::getSenderId, friendId);
            queryWrapper.lambda().eq(KitchenMessage::getReceiverId, uid);
            queryWrapper.lambda().eq(KitchenMessage::getIsRead, ChatMessageReadStatusEnum.NOT_READ);
            queryWrapper.lambda().eq(KitchenMessage::getStatus, StatusEnum.RELEASE.toString());
            return this.count(queryWrapper);
        } catch (Exception e) {
            log.error("KitchenMessageServiceImpl=====getUnreadMessagesByFriendId:{}", e.getMessage());
        }
        return 0;
    }

    @Override
    public boolean readMessages(Integer senderId, Integer receiverId) {
        return kitchenMessageMapper.readMessages(senderId,receiverId);
    }
}
