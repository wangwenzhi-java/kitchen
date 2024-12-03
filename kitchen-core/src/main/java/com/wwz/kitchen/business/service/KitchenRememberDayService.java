package com.wwz.kitchen.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wwz.kitchen.persistence.beans.KitchenRememberDay;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-20
 */
public interface KitchenRememberDayService extends IService<KitchenRememberDay> {


    List<KitchenRememberDay> listByUid(Integer uid);

    boolean saveRememberDay(KitchenRememberDay kitchenRememberDay);

    KitchenRememberDay getByIdFromDb(Integer rid);

    boolean updateRememberDayById(KitchenRememberDay kitchenRememberDay);
}
