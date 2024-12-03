package com.wwz.kitchen.business.dto;

import com.wwz.kitchen.business.enums.PlatformEnum;
import lombok.Data;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serializable;

/**
 * 投递日志到队列
 * Created by wenzhi.wang.
 *
 * on 2024/11/29.
 */
@Data
public class LogForRabbitDTO implements Serializable {

    private String username;

    private String ip;
    private String referer;
    private String requestUrl;
    private String params;
    private String browser;

    private PlatformEnum platformEnum;
    private String bussinessName;
}
