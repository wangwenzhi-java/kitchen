package com.wwz.kitchen.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wwz.kitchen.business.dto.KitchenPickDTO;
import com.wwz.kitchen.persistence.beans.KitchenPick;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-19
 */
public interface KitchenPickService extends IService<KitchenPick> {
    String addPick(KitchenPickDTO kitchenPickDTO,Integer uid, HttpServletRequest request);

}
