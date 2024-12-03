package com.wwz.kitchen.business.enums;

/**
 *
 * PickType
 * Created by wenzhi.wang.
 * on 2024/11/24.
 */
public enum PickTypeEnum {
    PICK(1,"PICK"),
    ORDER(2,"ORDER");

    private int code;
    private String desc;

    PickTypeEnum(int code,String desc) {
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
