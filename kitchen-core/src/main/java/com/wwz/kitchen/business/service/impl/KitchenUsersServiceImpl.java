package com.wwz.kitchen.business.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wwz.kitchen.business.dto.KitchenUsersRegistryDTO;
import com.wwz.kitchen.business.enums.CacheKeyType;
import com.wwz.kitchen.business.service.KitchenUsersService;
import com.wwz.kitchen.framework.annotation.CacheKeyStrategy;
import com.wwz.kitchen.framework.annotation.RedisCache;
import com.wwz.kitchen.framework.cachekey.LoginNotRequiredCacheKeyGenerator;
import com.wwz.kitchen.persistence.beans.KitchenUsers;
import com.wwz.kitchen.persistence.mapper.KitchenUsersMapper;
import com.wwz.kitchen.util.RandomNicknameGeneratorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-14
 */
@Service
public class KitchenUsersServiceImpl extends ServiceImpl<KitchenUsersMapper, KitchenUsers> implements KitchenUsersService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private LoginNotRequiredCacheKeyGenerator loginNotRequiredCacheKeyGenerator;

    private static final String PREFIX = "kitchen_user";

    /**
     * 注册使用！！
     * @param kitchenUsersRegistryDTO
     */
    @Override
    public int registry(KitchenUsersRegistryDTO kitchenUsersRegistryDTO) {
        KitchenUsers kitchenUsers = new KitchenUsers();
        String username = kitchenUsersRegistryDTO.getUsername();
        String password = kitchenUsersRegistryDTO.getPassword();
        String email = kitchenUsersRegistryDTO.getEmail();
        String nickname = RandomNicknameGeneratorUtil.generateRandomNickname();
        kitchenUsers.setUsername(username);
        kitchenUsers.setPassword(password);
        kitchenUsers.setEmail(email);
        kitchenUsers.setNickname(nickname);
        boolean save = this.save(kitchenUsers);//此方法会填充主键 如果主键配置了自增
        if (save) {
            return kitchenUsers.getId();
        }
        return 0;
    }

    /**
     * 根据邮箱查询用户
     * 由于邮箱是unique field
     * 所以适用于缓存 过期时间应该是不过期
     * @return
     */
    @CacheKeyStrategy(prefix = PREFIX, value = CacheKeyType.LOGIN_NOT_REQUIRED)//无需登录的key生成策略
    @RedisCache
    @Override
    public KitchenUsers getUserByEmail(String email) {
        QueryWrapper<KitchenUsers> queryWrapper = new QueryWrapper();
        queryWrapper.lambda().eq(KitchenUsers::getEmail,email);
        KitchenUsers one = this.getOne(queryWrapper);
        return one;
    }

    /**
     *
     * 根据用户名查询用户
     * 由于用户名是unique field
     * 所以适用于缓存 过期时间应该是不过期
     * @return
     */
    @CacheKeyStrategy(prefix = PREFIX, value = CacheKeyType.LOGIN_NOT_REQUIRED)//无需登录的key生成策略
    @RedisCache
    @Override
    public KitchenUsers getUserByUserName(String username) {
        QueryWrapper<KitchenUsers> queryWrapper = new QueryWrapper();
        queryWrapper.lambda().eq(KitchenUsers::getUsername,username);
        KitchenUsers one = this.getOne(queryWrapper);
        return one;
    }

    @Override
    public boolean updateByUser(KitchenUsers user) {
        boolean b = this.updateById(user);
        if (b) {//更新redis缓存
            String methodName = "getUserByEmail";
            Object[] args = {user.getEmail()};
            String key1 = PREFIX + ":" + loginNotRequiredCacheKeyGenerator.getGenerateKey(methodName,args);
            String methodName1 = "getUserByUserName";
            Object[] args1 = {user.getUsername()};
            String key2 = PREFIX + ":" + loginNotRequiredCacheKeyGenerator.getGenerateKey(methodName1,args1);
            redisTemplate.delete(key1);
            redisTemplate.delete(key2);
        }
        return b;
    }
}
