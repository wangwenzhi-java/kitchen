package com.wwz.kitchen.business.enums;

/**
 * Created by wenzhi.wang.
 * on 2024/11/28.
 */
public enum CacheKeyType {
    LOGIN_REQUIRED,           // 必须登录才能获取的普通缓存
    LOGIN_NOT_REQUIRED       // 无需登录的普通缓存
}
