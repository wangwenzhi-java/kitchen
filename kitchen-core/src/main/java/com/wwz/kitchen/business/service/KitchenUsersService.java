package com.wwz.kitchen.business.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.wwz.kitchen.business.dto.KitchenUsersRegistryDTO;
import com.wwz.kitchen.persistence.beans.KitchenUsers;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-14
 */
public interface KitchenUsersService extends IService<KitchenUsers> {

    int registry(KitchenUsersRegistryDTO kitchenUsersRegistryDTO);

    KitchenUsers getUserByEmail(String email);

    KitchenUsers getUserByUserName(String username);

    boolean updateByUser(KitchenUsers user);
}
