package com.wwz.kitchen.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wwz.kitchen.business.enums.CacheKeyType;
import com.wwz.kitchen.business.service.KitchenUserModeService;
import com.wwz.kitchen.framework.annotation.CacheKeyStrategy;
import com.wwz.kitchen.framework.annotation.RedisCache;
import com.wwz.kitchen.framework.annotation.RequiresLogin;
import com.wwz.kitchen.framework.cachekey.LoginRequiredCacheKeyGenerator;
import com.wwz.kitchen.persistence.beans.KitchenUserMode;
import com.wwz.kitchen.persistence.mapper.KitchenUserModeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-16
 */
@Service
public class KitchenUserModeServiceImpl extends ServiceImpl<KitchenUserModeMapper, KitchenUserMode> implements KitchenUserModeService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private LoginRequiredCacheKeyGenerator loginRequiredCacheKeyGenerator;

    private static final String PREFIX = "kitchen_user_mode";
    /**
     * 需登录使用
     * @param uid
     * @return
     */
    @RequiresLogin
    @CacheKeyStrategy(prefix = PREFIX, value = CacheKeyType.LOGIN_REQUIRED)
    @RedisCache
    @Override
    public KitchenUserMode getUserModeByUid(Integer uid) {
        QueryWrapper<KitchenUserMode> queryWrapperforUserMode = new QueryWrapper<>();
        queryWrapperforUserMode.lambda().eq(KitchenUserMode::getUid,uid);
        KitchenUserMode one = this.getOne(queryWrapperforUserMode);
        return one;
    }

    @RequiresLogin
    @Override
    public boolean updateByUidAndUserMode(Integer uid, Integer kitchenUserMode) {
        UpdateWrapper<KitchenUserMode> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(KitchenUserMode::getUid,uid);
        updateWrapper.lambda().set(KitchenUserMode::getUserMode, kitchenUserMode);  // 设置更新字段
        boolean update = this.update(updateWrapper);
        if (update) {//此处需更新缓存
            String methodName = "getUserModeByUid";
            Object[] args = {uid};
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            String redisKey = PREFIX + ":" + loginRequiredCacheKeyGenerator.getGenerateKey(methodName,args,username);
            redisTemplate.delete(redisKey);
        }
        return update;
    }
}
