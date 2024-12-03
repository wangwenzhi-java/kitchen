package com.wwz.kitchen.framework.config;

import com.wwz.kitchen.framework.property.RabbitMQProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wenzhi.wang.
 * on 2024/11/23.
 */
@EnableRabbit
@Configuration
@Slf4j
public class RabbitConfig {
    @Autowired
    private RabbitMQProperties rabbitMQProperties;
    @Autowired
    private AmqpAdmin amqpAdmin;


    // 定义交换机
    @Bean
    public Map<String, DirectExchange> exchanges() {
        Map<String, DirectExchange> exchanges = new HashMap<>();
        for (RabbitMQProperties.ExchangeConfig exchangeConfig : rabbitMQProperties.getExchanges().values()) {
            exchanges.put(exchangeConfig.getName(),
                    new DirectExchange(exchangeConfig.getName()));
        }
        return exchanges;
    }
    // 定义队列
    @Bean
    public Map<String, Queue> queues() {
        Map<String, Queue> queues = new java.util.HashMap<>();
        for (RabbitMQProperties.QueueConfig queueConfig : rabbitMQProperties.getQueues()) {
            queues.put(queueConfig.getName(), new Queue(queueConfig.getName(), true));
        }
        return queues;
    }
    // 定义绑定
    @Bean
    public List<Binding> bindings(Map<String, DirectExchange> exchanges, Map<String, Queue> queues) {
        List<Binding> bindings = new ArrayList<>();
        for (RabbitMQProperties.BindingConfig bindingConfig : rabbitMQProperties.getBindings()) {
            DirectExchange exchange = exchanges.get(bindingConfig.getExchange());
            Queue queue = queues.get(bindingConfig.getQueue());
            bindings.add(BindingBuilder.bind(queue).to(exchange).with(bindingConfig.getRoutingKey()));
        }
        return bindings;
    }

    @Bean
    public void declareExchangesAndQueues() {
        // Declare exchanges first
        rabbitMQProperties.getExchanges().values().forEach(exchangeConfig -> {
            Exchange exchange;
            // 根据配置类型创建不同类型的交换机
            if ("topic".equalsIgnoreCase(exchangeConfig.getType())) {
                exchange = new TopicExchange(exchangeConfig.getName());
            } else {
                exchange = new DirectExchange(exchangeConfig.getName());
            }
            amqpAdmin.declareExchange(exchange);
        });

        // Declare queues next
        rabbitMQProperties.getQueues().forEach(queueConfig -> {
            Queue queue = new Queue(queueConfig.getName(), true);  // 设置持久化
            amqpAdmin.declareQueue(queue);
        });

        // Declare bindings last
        rabbitMQProperties.getBindings().forEach(bindingConfig -> {
            DirectExchange exchange = new DirectExchange(bindingConfig.getExchange());
            Queue queue = new Queue(bindingConfig.getQueue(), true);  // 设置持久化
            Binding binding = BindingBuilder.bind(queue).to(exchange).with(bindingConfig.getRoutingKey());
            amqpAdmin.declareBinding(binding);
        });
    }

    /**
     * 配置消息转换器
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置 RabbitTemplate，设置消息转换器
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("消息成功发送到 RabbitMQ");
            } else {
                log.error("消息发送失败，原因：" + cause);
                // 如果消息发送失败，触发重试机制
                Message message = correlationData.getReturnedMessage();
                int maxRetries = 3;
                int attempt = 0;
                while (attempt < maxRetries) {
                    try {
                        // 获取消息的 Exchange 和 RoutingKey
                        String exchange = message.getMessageProperties().getReceivedExchange();
                        String routingKey = message.getMessageProperties().getReceivedRoutingKey();

                        // 调用 RabbitTemplate 发送消息
                        rabbitTemplate.convertAndSend(exchange, routingKey, message);
                        log.info("消息重试发送成功，交换机：" + exchange + "，路由键：" + routingKey);
                        break;  // 如果成功，跳出循环
                    } catch (AmqpException e) {
                        attempt++;
                        log.error("消息重试发送失败，第" + attempt + "次重试：" + e.getMessage());
                        if (attempt >= maxRetries) {
                            log.error("重试失败，消息丢失：" + e.getMessage());
                            // 可以选择记录失败，或者执行其他补偿逻辑
                        } else {
                            try {
                                // 添加重试间隔，避免立即再次重试
                                Thread.sleep(2000);  // 暂停2秒再尝试重试
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }
            }
        });
        return rabbitTemplate;
    }

}
