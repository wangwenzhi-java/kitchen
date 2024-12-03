package com.wwz.kitchen.business.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wwz.kitchen.business.dto.LogForRabbitDTO;
import com.wwz.kitchen.business.enums.LogLevelEnum;
import com.wwz.kitchen.business.enums.PlatformEnum;
import com.wwz.kitchen.business.service.KitchenLogService;
import com.wwz.kitchen.business.service.KitchenUsersService;
import com.wwz.kitchen.persistence.beans.KitchenLog;
import com.wwz.kitchen.persistence.beans.KitchenUsers;
import com.wwz.kitchen.persistence.mapper.KitchenLogMapper;
import com.wwz.kitchen.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-15
 */
@Service
public class KitchenLogServiceImpl extends ServiceImpl<KitchenLogMapper, KitchenLog> implements KitchenLogService {

    @Autowired
    private KitchenUsersService kitchenUsersService;

    //保存日志
    @Override
    public void asyncSaveSystemLog(PlatformEnum platform, String bussinessName, LogForRabbitDTO logForRabbitDTO) {
        KitchenLog sysLog = new KitchenLog();
        sysLog.setType(logForRabbitDTO.getPlatformEnum().toString());
        sysLog.setLogLevel(LogLevelEnum.INFO.toString());
        sysLog.setIp(logForRabbitDTO.getIp());
        sysLog.setParams(logForRabbitDTO.getParams());
        sysLog.setRequestUrl(logForRabbitDTO.getRequestUrl());
        sysLog.setReferer(logForRabbitDTO.getReferer());
        sysLog.setBrowser(logForRabbitDTO.getBrowser());
        if (logForRabbitDTO.getUsername() != null && !"".equals(logForRabbitDTO.getUsername()) && !"anonymousUser".equals(logForRabbitDTO.getUsername())) {
            KitchenUsers kitchenUsers = kitchenUsersService.getUserByUserName(logForRabbitDTO.getUsername());
            if (kitchenUsers != null) {
                sysLog.setUserId(kitchenUsers.getId());
            }
            sysLog.setContent(String.format("用户: [%s] | 操作: %s", logForRabbitDTO.getUsername(), bussinessName));
        } else {
            sysLog.setContent(String.format("访客: [%s] | 操作: %s", sysLog.getIp(), bussinessName));
        }
        try {
            this.save(sysLog);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
