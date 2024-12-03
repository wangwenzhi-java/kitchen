package com.wwz.kitchen.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wwz.kitchen.persistence.beans.KitchenMenu;

import java.util.List;
import java.util.Set;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-18
 */
public interface KitchenMenuService extends IService<KitchenMenu> {

    boolean saveMenu(KitchenMenu kitchenMenu);

    boolean updateMenu(KitchenMenu kitchenMenu);

    List<KitchenMenu> listByUidAndTid(Integer uid, Integer tid, String status);

    List<KitchenMenu> listByUidsAndCid(Set<Integer> uniqueUids, Integer cid);
}
