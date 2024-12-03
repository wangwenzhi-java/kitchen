package com.wwz.kitchen.framework.runner;

import com.wwz.kitchen.framework.property.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContextEvent;

/**
 * 程序启动后通过ApplicationRunner处理一些事务
 * Created by wenzhi.wang.
 * on 2024/11/14.
 * @since 1.0
 */
@Slf4j
@Component
public class KitchenApplicationRunner extends ContextLoaderListener implements ApplicationRunner {

    @Value("${server.port}")
    private int port;
    @Autowired
    private AppProperties appProperties;
    @Autowired
    private DataSourceProperties dataSourceProperties;
    @Autowired
    private RedisProperties redisProperties;
    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;
    @Override
    public void run(ApplicationArguments applicationArguments) {
        switch(port) {
            case 8888:
                log.info("老喵私房菜部署完成，访问地址：http://localhost:" + port);
                break;
            case 8899:
                log.info("老喵私房菜聊天系统部署完成，访问地址：http://localhost:" + port);
                break;
        }

    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        log.info("current wblog version：{}", appProperties.getVersion());
        if (appProperties.isEnabledPrintConfig()) {
            log.info("博客关键配置信息：");
            String[] activeProfiles = configurableApplicationContext.getEnvironment().getActiveProfiles();
            if (ObjectUtils.isEmpty(activeProfiles)) {
                String[] defaultProfiles = configurableApplicationContext.getEnvironment().getDefaultProfiles();
                log.info("No active profile set, falling back to default profiles: " + StringUtils.arrayToCommaDelimitedString(defaultProfiles));
            } else {
                log.info("The following profiles are active: " + StringUtils.arrayToCommaDelimitedString(activeProfiles));
            }

            log.info("Data Source：");
            log.info("  - url：{}", dataSourceProperties.getUrl());
            log.info("  - username：{}", dataSourceProperties.getUsername());
            log.info("  - password：{}", dataSourceProperties.getPassword());

            log.info("Redis：");
            log.info("  - master：{}",redisProperties.getSentinel().getMaster());
            log.info("  - nodes：{}",redisProperties.getSentinel().getNodes());
            log.info("  - password：{}",redisProperties.getPassword());
            log.info("  - database：{}", redisProperties.getDatabase());

        }
    }
}
