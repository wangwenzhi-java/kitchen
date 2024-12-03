package com.wwz.kitchen.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wwz.kitchen.persistence.beans.KitchenUserMode;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-16
 */
public interface KitchenUserModeService extends IService<KitchenUserMode> {

    KitchenUserMode getUserModeByUid(Integer uid);

    boolean updateByUidAndUserMode(Integer id, Integer kitchenUserMode);
}
