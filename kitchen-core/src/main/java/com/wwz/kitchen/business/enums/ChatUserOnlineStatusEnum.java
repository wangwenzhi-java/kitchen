package com.wwz.kitchen.business.enums;

/**
 * Created by wenzhi.wang.
 * on 2024/11/23.
 */
public enum ChatUserOnlineStatusEnum {
    OFFINE(0,"离线"),
    ONLINE(1,"在线");
    private int code;
    private String desc;

    ChatUserOnlineStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
