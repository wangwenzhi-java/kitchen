package com.wwz.kitchen.business.enums;

/**
 * Created by wenzhi.wang.
 * on 2024/11/23.
 */
public enum FriendshipStatusEnum {

    TO_BE_CONFIRMED(0,"待确认"),
    CONFIRMED(1,"已添加"),
    REFUSE(2,"已拒绝"),
    DELETE(3,"已删除");

    private int value;
    private String desc;

    FriendshipStatusEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public int getValue() {
        return value;
    }

    public static FriendshipStatusEnum fromValue(int value) {
        for (FriendshipStatusEnum statusValue : values()) {
            if (statusValue.getValue() == value) {
                return statusValue;
            }
        }
        return null;
    }
}
