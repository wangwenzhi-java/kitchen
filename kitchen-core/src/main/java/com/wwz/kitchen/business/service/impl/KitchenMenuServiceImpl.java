package com.wwz.kitchen.business.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wwz.kitchen.business.enums.CacheKeyType;
import com.wwz.kitchen.business.enums.StatusEnum;
import com.wwz.kitchen.business.service.KitchenMenuService;
import com.wwz.kitchen.business.service.KitchenMenusShareService;
import com.wwz.kitchen.business.service.KitchenUsersService;
import com.wwz.kitchen.framework.annotation.CacheKeyStrategy;
import com.wwz.kitchen.framework.annotation.RedisCache;
import com.wwz.kitchen.framework.annotation.RequiresLogin;
import com.wwz.kitchen.framework.cachekey.LoginRequiredCacheKeyGenerator;
import com.wwz.kitchen.persistence.beans.KitchenMenu;
import com.wwz.kitchen.persistence.beans.KitchenMenusShare;
import com.wwz.kitchen.persistence.mapper.KitchenMenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-18
 */
@Service
public class KitchenMenuServiceImpl extends ServiceImpl<KitchenMenuMapper, KitchenMenu> implements KitchenMenuService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private LoginRequiredCacheKeyGenerator loginRequiredCacheKeyGenerator;
    @Autowired
    private KitchenMenusShareService kitchenMenusShareService;
    @Autowired
    private KitchenUsersService kitchenUsersService;

    private static final String PREFIX = "kitchen_menu";


    @RequiresLogin
    @Override
    public boolean saveMenu(KitchenMenu kitchenMenu) {
        boolean save = this.save(kitchenMenu);
        if (save) {
            String methodName = "listByUidAndTid";
            Object[] args = {kitchenMenu.getUid(), kitchenMenu.getTid(),StatusEnum.RELEASE.toString()};
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            String key = PREFIX + ":" + loginRequiredCacheKeyGenerator.getGenerateKey(methodName,args,username);
            redisTemplate.delete(key);

            String method2 = "listByUidsAndCid";
            Integer uid = kitchenUsersService.getUserByUserName(username).getId();
            Object[] args2 = {getUniqueUids(uid),kitchenMenu.getCid()};
            String key2 = PREFIX + ":" + loginRequiredCacheKeyGenerator.getGenerateKey(method2,args2,username);
            redisTemplate.delete(key2);
        }
        return save;
    }
    @RequiresLogin
    @Override
    public boolean updateMenu(KitchenMenu kitchenMenu) {
        boolean b = this.updateById(kitchenMenu);
        if (b) {//更新相对应tid的缓存
            String methodName = "listByUidAndTid";
            Object[] args = {kitchenMenu.getUid(), kitchenMenu.getTid(),StatusEnum.RELEASE.toString()};
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            String key = PREFIX + ":" + loginRequiredCacheKeyGenerator.getGenerateKey(methodName,args,username);
            redisTemplate.delete(key);

            String method2 = "listByUidsAndCid";
            Integer uid = kitchenUsersService.getUserByUserName(username).getId();
            Object[] args2 = {getUniqueUids(uid),kitchenMenu.getCid()};
            String key2 = PREFIX + ":" + loginRequiredCacheKeyGenerator.getGenerateKey(method2,args2,username);
            redisTemplate.delete(key2);
        }
        return b;
    }

    @RequiresLogin
    @CacheKeyStrategy(prefix = PREFIX,value = CacheKeyType.LOGIN_REQUIRED)
    @RedisCache
    @Override
    public List<KitchenMenu> listByUidAndTid(Integer uid, Integer tid, String status) {
        QueryWrapper<KitchenMenu> queryWrapperForMenu = new QueryWrapper<>();
        queryWrapperForMenu.eq("uid",uid);
        queryWrapperForMenu.eq("tid",tid);
        queryWrapperForMenu.eq("status", StatusEnum.RELEASE.toString());
        queryWrapperForMenu.orderByDesc("update_time");
        return this.list(queryWrapperForMenu);
    }

    @RequiresLogin
    @CacheKeyStrategy(prefix = PREFIX,value = CacheKeyType.LOGIN_REQUIRED)
    @RedisCache
    @Override
    public List<KitchenMenu> listByUidsAndCid(Set<Integer> uniqueUids, Integer cid) {
        QueryWrapper<KitchenMenu> queryWrapperForMenu = new QueryWrapper<>();
        queryWrapperForMenu.lambda().
                eq(KitchenMenu::getStatus,StatusEnum.RELEASE.toString())
                .eq(KitchenMenu::getCid,cid)
                .in(KitchenMenu::getUid,uniqueUids);
        List<KitchenMenu> list = this.list(queryWrapperForMenu);
        return list;
    }

    //获取指定uid的共享列表
    private Set<Integer> getUniqueUids(Integer uid) {
        List<KitchenMenusShare> sharelist = kitchenMenusShareService.listByUid(uid);
        // 提取所有的 ShareFromUid 和 ShareToUid，并去重
        Set<Integer> uniqueUids = sharelist.stream()
                .flatMap(kitchenMenusShare -> {
                    // 先获取 ShareFromUid 和 ShareToUid，然后将它们转换为流并合并
                    return Stream.of(kitchenMenusShare.getShareFromUid(), kitchenMenusShare.getShareToUid());
                })
                .collect(Collectors.toSet()); // 将它们收集到一个 Set 中，Set 自动去重
        return uniqueUids;
    }
}
