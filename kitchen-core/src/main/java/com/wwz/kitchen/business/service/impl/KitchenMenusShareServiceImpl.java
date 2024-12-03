package com.wwz.kitchen.business.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wwz.kitchen.business.enums.CacheKeyType;
import com.wwz.kitchen.business.enums.ShareStatusEnum;
import com.wwz.kitchen.business.service.KitchenMenusShareService;
import com.wwz.kitchen.framework.annotation.CacheKeyStrategy;
import com.wwz.kitchen.framework.annotation.RedisCache;
import com.wwz.kitchen.framework.annotation.RequiresLogin;
import com.wwz.kitchen.persistence.beans.KitchenMenusShare;
import com.wwz.kitchen.persistence.mapper.KitchenMenusShareMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-27
 */
@Service
public class KitchenMenusShareServiceImpl extends ServiceImpl<KitchenMenusShareMapper, KitchenMenusShare> implements KitchenMenusShareService {

    private static final String PREFIX = "kitchen_menu_share";


    @RequiresLogin
    @CacheKeyStrategy(prefix = PREFIX,value = CacheKeyType.LOGIN_REQUIRED)
    @RedisCache
    @Override
    public List<KitchenMenusShare> listByUid(Integer uid) {
        QueryWrapper<KitchenMenusShare> queryWrapperForMenusShare = new QueryWrapper<>();//查询共享用户(去重)
        queryWrapperForMenusShare.lambda()
                .eq(KitchenMenusShare::getShareFromUid,uid)
                .or(q -> q.eq(KitchenMenusShare::getShareToUid, uid))
                .eq(KitchenMenusShare::getStatus, ShareStatusEnum.CONFIRM.getCode());
        return this.list(queryWrapperForMenusShare);
    }

    @Override
    public List<KitchenMenusShare> listBySendUIdAndReceiveUid(Integer sendUid, Integer receiveUid) {
        QueryWrapper<KitchenMenusShare> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .in(KitchenMenusShare::getShareFromUid, sendUid, receiveUid)  // 使用in匹配ShareFromUid
                .in(KitchenMenusShare::getShareToUid, sendUid, receiveUid);   // 使用in匹配ShareToUid
        return this.list(queryWrapper);
    }


    @Override
    public boolean saveMenusShare(KitchenMenusShare kitchenMenusShare) {
        boolean save = this.save(kitchenMenusShare);
        return save;
    }

    @Override
    public boolean updatMenusShare(KitchenMenusShare kitchenMenusShare) {
        boolean b = this.updateById(kitchenMenusShare);
        return b;
    }
}
