package com.wwz.kitchen.business.enums;

/**
 * 用户模式枚举
 * code需对应数据库中值
 * Created by wenzhi.wang.
 * on 2024/11/16.
 */
public enum KitchenUserModeEnum {
    DEFAULT("默认模式",0),
    USER_DEFINED("自定义模式",1);

    private final String message;
    private final int code;

    KitchenUserModeEnum( String message,int code) {
        this.message = message;
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据状态码查找枚举实例
     *
     * @param code 状态码
     * @return 对应的枚举实例，如果找不到则返回 null
     */
    public static KitchenUserModeEnum fromCode(int code) {
        for (KitchenUserModeEnum kitchenUserModeEnum : values()) {
            if (kitchenUserModeEnum.getCode() == code) {
                return kitchenUserModeEnum;
            }
        }
        return null;
    }
}
