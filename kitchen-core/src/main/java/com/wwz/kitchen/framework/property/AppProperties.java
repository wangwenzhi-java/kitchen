package com.wwz.kitchen.framework.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by wenzhi.wang.
 * on 2024/11/14.
 */
@Component
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {
    /**
     * 是否在项目启动时，打印配置文件中的 【数据库配置】，包括 mysql、redis，默认关闭，生产环境不建议开启
     */
    private boolean enabledPrintConfig;
    /**
     * 系统版本，不建议修改。
     */
    private String version;

    /**
     * 是否启用 redis 切面缓存。
     */
    private boolean enableRedisCache;
}
