package com.wwz.kitchen.framework.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wwz.kitchen.business.service.KitchenUsersService;
import com.wwz.kitchen.persistence.beans.KitchenUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;

import java.sql.Wrapper;
import java.util.Collections;

/**
 * 自定义的 UserDetailsService
 *
 * Created by wenzhi.wang.
 * on 2024/11/15.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private KitchenUsersService kitchenUsersService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库中查询用户
        KitchenUsers userEntity = kitchenUsersService.getUserByUserName(username);

        if (userEntity == null) {
            throw new UsernameNotFoundException("用户名未找到: " + username);
        }

        // 返回一个 UserDetails 实现类，包含用户的基本信息和权限
        return User.builder()
                .username(userEntity.getUsername())
                .password(userEntity.getPassword()) // 密码应该经过加密
                .authorities(Collections.emptyList()) //用户的权限列表，必须是一个非空的集合，通常是 GrantedAuthority 类型的集合。在创建 UsernamePasswordAuthenticationToken 时，authorities 参数不能为 null，即使你不使用权限，也应该传递一个空的权限集合。
                .build();
    }
}
