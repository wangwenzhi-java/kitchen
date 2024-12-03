package com.wwz.kitchen.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wwz.kitchen.persistence.beans.KitchenCategory;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-18
 */
public interface KitchenCategoryService extends IService<KitchenCategory> {

    List<KitchenCategory> listByTid(Integer tid);
}
