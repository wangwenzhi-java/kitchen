package com.wwz.kitchen.listener;

import com.rabbitmq.client.Channel;
import com.wwz.kitchen.business.dto.ChatMessageDTO;
import com.wwz.kitchen.business.enums.MessageTypeEnum;
import com.wwz.kitchen.business.service.KitchenMessageService;
import com.wwz.kitchen.persistence.beans.KitchenMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Created by wenzhi.wang.
 * on 2024/11/26.
 */
@Service
@Slf4j
public class ChatMessageListener {
    private static final String Message_QUEUE = "friend_message_queue";

    @Autowired
    private KitchenMessageService kitchenMessageService;


    @RabbitListener(queues = Message_QUEUE, ackMode = "MANUAL")
    public void saveChatMessage(ChatMessageDTO message, Channel channel, Message messageObj) { // 使用手动确认模式
        try {
            // 校验消息类型
            Integer messageType = message.getType();
            if (messageType == null || !messageType.equals(MessageTypeEnum.SEND_MESSAGE.getCode())) {
                log.error("ChatMessageListener==========saveChatMessage: Invalid message type");
                channel.basicReject(messageObj.getMessageProperties().getDeliveryTag(), false);
                return;
            }
            // 解析消息内容
            Integer uid = message.getSender();
            Integer friendId = message.getReceiver();
            String content = message.getContent() == null ? "" : message.getContent();
            String sendTime = message.getSendTime();
            Integer messageContentType = message.getMessageContentType();

            // 保存到数据库
            KitchenMessage kitchenMessage = new KitchenMessage();
            kitchenMessage.setSenderId(uid);
            kitchenMessage.setReceiverId(friendId);
            kitchenMessage.setContent(content);
            kitchenMessage.setMessageType(messageContentType);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 日期格式
            kitchenMessage.setCreateTime(formatter.parse(sendTime));

            boolean save = kitchenMessageService.save(kitchenMessage);

            // 消息确认或拒绝
            if (save) {
                log.info("ChatMessageListener==========saveChatMessage: save success");
                channel.basicAck(messageObj.getMessageProperties().getDeliveryTag(), false);
            } else {
                channel.basicNack(messageObj.getMessageProperties().getDeliveryTag(), false, true);
            }
        } catch (Exception e) {
            log.error("ChatMessageListener===============saveChatMessage: {}", e.getMessage());
            try {
                // 消息重新入队，避免丢失
                channel.basicNack(messageObj.getMessageProperties().getDeliveryTag(), false, true);
            } catch (IOException ex) {
                log.error("Error when nack message: {}", ex.getMessage());
            }
        }
    }

}
