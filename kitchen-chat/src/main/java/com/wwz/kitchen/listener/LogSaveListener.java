package com.wwz.kitchen.listener;

import com.rabbitmq.client.Channel;
import com.wwz.kitchen.business.dto.FriendshipRabbitDto;
import com.wwz.kitchen.business.dto.LogForRabbitDTO;
import com.wwz.kitchen.business.enums.PlatformEnum;
import com.wwz.kitchen.business.service.KitchenLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;


/**
 * 日志存储
 * Created by wenzhi.wang.
 * on 2024/11/29.
 */
@Service
@Slf4j
public class LogSaveListener {

    @Autowired
    private KitchenLogService kitchenLogService;

    private static final String REQUEST_QUEUE = "log_save_queue";

    @RabbitListener(queues = REQUEST_QUEUE , ackMode = "MANUAL")  // 使用手动确认模式
    public void receiveLogSave(LogForRabbitDTO logForRabbitDTO, Channel channel, Message messageObj){
        try {
            PlatformEnum platformEnum = logForRabbitDTO.getPlatformEnum();
            String bussinessName = logForRabbitDTO.getBussinessName();
            kitchenLogService.asyncSaveSystemLog(platformEnum,bussinessName,logForRabbitDTO);
            channel.basicAck(messageObj.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error(e.getMessage());
            try {
                // 消息重新入队，避免丢失
                channel.basicNack(messageObj.getMessageProperties().getDeliveryTag(), false, true);
            } catch (IOException ex) {
                log.error("Error when nack message: {}", ex.getMessage());
            }
        }
    }
}
