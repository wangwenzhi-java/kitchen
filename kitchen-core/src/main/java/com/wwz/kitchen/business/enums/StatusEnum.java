package com.wwz.kitchen.business.enums;

/**
 * Created by wenzhi.wang.
 * on 2024/11/18.
 */
public enum StatusEnum {
    RELEASE("已发布"),
    NOT_RELEASE("未发布");
    private String desc;

    StatusEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
