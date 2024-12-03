package com.wwz.kitchen.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wwz.kitchen.persistence.beans.KitchenMenusShare;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-27
 */
public interface KitchenMenusShareService extends IService<KitchenMenusShare> {

    List<KitchenMenusShare> listByUid(Integer uid);

    List<KitchenMenusShare> listBySendUIdAndReceiveUid(Integer sendUid, Integer receiveUid);

    boolean saveMenusShare(KitchenMenusShare kitchenMenusShare);

    boolean updatMenusShare(KitchenMenusShare kitchenMenusShare);
}
