package com.wwz.kitchen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ServletComponentScan
@EnableTransactionManagement
public class KitchenWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(KitchenWebApplication.class, args);
    }

}
