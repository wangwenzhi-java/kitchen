package com.wwz.kitchen;

import com.wwz.kitchen.business.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
class KitchenWebApplicationTests {

    @Autowired
    private EmailService emailService;

    @Test
    void sendEmail() throws Exception {
        emailService.sendSimpleMail("wblog_mail@qq.com", "test email", "test code");
    }
}