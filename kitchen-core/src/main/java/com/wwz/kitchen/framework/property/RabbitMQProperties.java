package com.wwz.kitchen.framework.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by wenzhi.wang.
 * on 2024/11/23.
 */
@Component
@ConfigurationProperties(prefix = "spring.rabbitmq")
public class RabbitMQProperties {

    private String host;
    private int port;
    private String username;
    private String password;
    private String virtualHost;
    private boolean publisherReturns;

    private Map<String, ExchangeConfig> exchanges;
    private List<QueueConfig> queues;
    private List<BindingConfig> bindings;

    // Getters and Setters

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public boolean isPublisherReturns() {
        return publisherReturns;
    }

    public void setPublisherReturns(boolean publisherReturns) {
        this.publisherReturns = publisherReturns;
    }

    public Map<String, ExchangeConfig> getExchanges() {
        return exchanges;
    }

    public void setExchanges(Map<String, ExchangeConfig> exchanges) {
        this.exchanges = exchanges;
    }

    public List<QueueConfig> getQueues() {
        return queues;
    }

    public void setQueues(List<QueueConfig> queues) {
        this.queues = queues;
    }

    public List<BindingConfig> getBindings() {
        return bindings;
    }

    public void setBindings(List<BindingConfig> bindings) {
        this.bindings = bindings;
    }

    // Inner classes

    public static class ExchangeConfig {
        private String name;
        private String type;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class QueueConfig {
        private String name;
        private String routingKey;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRoutingKey() {
            return routingKey;
        }

        public void setRoutingKey(String routingKey) {
            this.routingKey = routingKey;
        }
    }

    public static class BindingConfig {
        private String exchange;
        private String queue;
        private String routingKey;

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getQueue() {
            return queue;
        }

        public void setQueue(String queue) {
            this.queue = queue;
        }

        public String getRoutingKey() {
            return routingKey;
        }

        public void setRoutingKey(String routingKey) {
            this.routingKey = routingKey;
        }
    }
}
