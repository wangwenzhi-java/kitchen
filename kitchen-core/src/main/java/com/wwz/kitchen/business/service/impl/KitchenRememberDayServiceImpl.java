package com.wwz.kitchen.business.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wwz.kitchen.business.enums.CacheKeyType;
import com.wwz.kitchen.business.enums.StatusEnum;
import com.wwz.kitchen.business.service.KitchenRememberDayService;
import com.wwz.kitchen.framework.annotation.CacheKeyStrategy;
import com.wwz.kitchen.framework.annotation.RedisCache;
import com.wwz.kitchen.framework.annotation.RequiresLogin;
import com.wwz.kitchen.framework.cachekey.LoginRequiredCacheKeyGenerator;
import com.wwz.kitchen.persistence.beans.KitchenRememberDay;
import com.wwz.kitchen.persistence.mapper.KitchenRememberDayMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-20
 */
@Service
public class KitchenRememberDayServiceImpl extends ServiceImpl<KitchenRememberDayMapper, KitchenRememberDay> implements KitchenRememberDayService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private LoginRequiredCacheKeyGenerator loginRequiredCacheKeyGenerator;
    @Autowired
    private KitchenRememberDayMapper kitchenRememberDayMapper;

    //
    private static final String PREFIX = "kitchen_remember";

    @RedisCache
    @CacheKeyStrategy(prefix = PREFIX ,value = CacheKeyType.LOGIN_REQUIRED)
    @RequiresLogin
    @Override
    public List<KitchenRememberDay> listByUid(Integer uid) {
        QueryWrapper<KitchenRememberDay> kitchenRememberDayQueryWrapper = new QueryWrapper<>();
        kitchenRememberDayQueryWrapper.lambda().eq(KitchenRememberDay::getUid, uid);
        kitchenRememberDayQueryWrapper.lambda().eq(KitchenRememberDay::getStatus, StatusEnum.RELEASE.toString());
        kitchenRememberDayQueryWrapper.lambda().orderByDesc(KitchenRememberDay::getCreateTime);
        return this.list(kitchenRememberDayQueryWrapper);
    }

    @RequiresLogin
    @Override
    public boolean saveRememberDay(KitchenRememberDay kitchenRememberDay) {
        boolean save = this.save(kitchenRememberDay);
        if (save) {
            String methodName = "listByUid";
            Object[] args = {kitchenRememberDay.getUid()};
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            String key = PREFIX + ":" + loginRequiredCacheKeyGenerator.getGenerateKey(methodName,args,username);
            redisTemplate.delete(key);
        }
        return save;
    }

    //此处查询必须查询数据库
    @Override
    public KitchenRememberDay getByIdFromDb(Integer rid) {
        return kitchenRememberDayMapper.selectById(rid);
    }

    //数据库更新 缓存删除
    @Override
    public boolean updateRememberDayById(KitchenRememberDay kitchenRememberDay) {
        int i = kitchenRememberDayMapper.updateById(kitchenRememberDay);
        if (i > 0) {//
            String methodName = "listByUid";
            Object[] args = {kitchenRememberDay.getUid()};
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            String key = PREFIX + ":" + loginRequiredCacheKeyGenerator.getGenerateKey(methodName,args,username);
            redisTemplate.delete(key);
        }
        return i == 1;
    }
}
