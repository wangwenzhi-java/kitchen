package com.wwz.kitchen.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wwz.kitchen.business.dto.LogForRabbitDTO;
import com.wwz.kitchen.business.enums.PlatformEnum;
import com.wwz.kitchen.persistence.beans.KitchenLog;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-15
 */
public interface KitchenLogService extends IService<KitchenLog> {
    void asyncSaveSystemLog(PlatformEnum platform, String bussinessName, LogForRabbitDTO logForRabbitDTO);
}
