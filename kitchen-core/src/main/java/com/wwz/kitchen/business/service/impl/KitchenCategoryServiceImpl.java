package com.wwz.kitchen.business.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wwz.kitchen.business.enums.CacheKeyType;
import com.wwz.kitchen.business.service.KitchenCategoryService;
import com.wwz.kitchen.framework.annotation.CacheKeyStrategy;
import com.wwz.kitchen.framework.annotation.RedisCache;
import com.wwz.kitchen.persistence.beans.KitchenCategory;
import com.wwz.kitchen.persistence.mapper.KitchenCategoryMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-18
 */
@Service
public class KitchenCategoryServiceImpl extends ServiceImpl<KitchenCategoryMapper, KitchenCategory> implements KitchenCategoryService {

    private static final String PREFIX = "kitchen_category";
    @CacheKeyStrategy(prefix = PREFIX , value = CacheKeyType.LOGIN_NOT_REQUIRED)
    @RedisCache
    @Override
    public List<KitchenCategory> listByTid(Integer tid) {
        QueryWrapper<KitchenCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(KitchenCategory::getTid, tid);
        List<KitchenCategory> list = this.list(queryWrapper);
        return list;
    }
}
