package com.wwz.kitchen.business.enums;

/**
 * Created by wenzhi.wang.
 * on 2024/11/27.
 */
public enum ShareStatusEnum {
    NOT_CONFIRM(0,"未处理"),
    CONFIRM(1,"已确认"),
    REFUSED(2,"已拒绝");


    private int code;
    private String desc;

    ShareStatusEnum(int code,String desc) {
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
