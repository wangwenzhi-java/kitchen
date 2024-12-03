package com.wwz.kitchen.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wwz.kitchen.business.enums.MessageTypeEnum;
import com.wwz.kitchen.framework.property.RabbitMQProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 处理聊天的队列工具类
 * Created by wenzhi.wang.
 * on 2024/11/24.
 */
@Slf4j
@Component
public class RabbitMqUtil {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties rabbitMQProperties;

    // 使用构造函数注入
    @Autowired
    public RabbitMqUtil(RabbitTemplate rabbitTemplate, RabbitMQProperties rabbitMQProperties) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitMQProperties = rabbitMQProperties;
    }


    /**
     * 公共队列
     * @param exchangeName 交换机
     * @param routingKeyName 路由键
     * @param message 消息体
     * @param <T>
     */
    public <T> void sendToRabbit(String exchangeName,String routingKeyName, T message) {
        // 获取交换机名称
        String exchange = rabbitMQProperties.getExchanges().get(exchangeName).getName();
        // 获取对应的路由键
        String routingKey = routingKeyName;
        try {
            // 发送消息之前的日志
            //log.info("Sending message to RabbitMQ: Exchange={}, RoutingKey={}, Message={}", exchange, routingKey, message);
            //byte[] messageBytes = new ObjectMapper().writeValueAsBytes(message);
            //log.info("Message size: {}", messageBytes.length);
            // 发送消息
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            //log.info("Message sent successfully to RabbitMQ: Exchange={}, RoutingKey={}, Message={}", exchange, routingKey, message);
        } catch (AmqpException e) {
            log.error("RabbitMqUtil sendToRabbit failed: {}", e.getMessage());
            // 处理异常逻辑，确保消息不会丢失
            // 可在此加入重试逻辑
        }
    }

    // 聊天交换机名称
    private static final String EXCHANGE_NAME = "chat_direct_exchange";

    /**
     * 发送消息到 RabbitMQ 处理聊天的异步持久化投入队列
     * @param messageType 消息类型
     * @param message 消息内容
     * @param <T> 消息类型
     */
    public <T> void sendToRabbit(int messageType, T message) {
        // 获取交换机名称
        String exchange = rabbitMQProperties.getExchanges().get(EXCHANGE_NAME).getName();
        // 获取对应的路由键
        String routingKey = getRoutingKeyForMessageType(messageType);
        if (routingKey != null) {
            try {
                // 发送消息
                rabbitTemplate.convertAndSend(exchange, routingKey, message);
            } catch (AmqpException e) {
                log.error("RabbitMqUtil sendToRabbit failed: {}", e.getMessage());
                // 处理异常逻辑，确保消息不会丢失
                // 可在此加入重试逻辑
            }
        } else {
            throw new IllegalArgumentException("未知消息类型: " + messageType);
        }
    }

    /**
     * 根据消息类型获取对应的 routingKey
     * @param messageType 消息类型
     * @return routingKey
     */
    private String getRoutingKeyForMessageType(Integer messageType) {
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.fromValue(messageType);
        switch (messageTypeEnum) {
            case REQUEST_FRIEND:
            case DO_REQUEST_FRIEND:
            case DELETE_FRIEND: //删除
            case REQUEST_SHARE_MENU:
            case DO_REQUEST_SHARE_MENU:
                return "friendRequest";
            case SEND_MESSAGE:
                return "friendMessage";
            default:
                return null;
        }
    }
}
