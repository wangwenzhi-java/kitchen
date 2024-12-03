package com.wwz.kitchen;

import com.wwz.kitchen.framework.property.RabbitMQProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ServletComponentScan
@EnableTransactionManagement
public class KitchenChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(KitchenChatApplication.class, args);
    }
}
