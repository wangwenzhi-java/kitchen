package com.wwz.kitchen.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wwz.kitchen.persistence.beans.KitchenFriendship;
import com.wwz.kitchen.persistence.beans.KitchenUsers;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-21
 */
public interface KitchenFriendshipService extends IService<KitchenFriendship> {
    List<KitchenUsers> getFriend(Integer uid);

    boolean deleteFriendship(Integer uid, Integer friendId);
}
