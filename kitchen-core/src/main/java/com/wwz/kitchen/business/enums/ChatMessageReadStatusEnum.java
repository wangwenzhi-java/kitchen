package com.wwz.kitchen.business.enums;

/**
 * Created by wenzhi.wang.
 * on 2024/11/25.
 */
public enum ChatMessageReadStatusEnum {
    NOT_READ(0,"未读"),
    READ(1,"已读");

    private int code;
    private String desc;

    ChatMessageReadStatusEnum(int code,String desc) {
        this.desc = desc;
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }
    public int getCode() {
        return code;
    }
}
