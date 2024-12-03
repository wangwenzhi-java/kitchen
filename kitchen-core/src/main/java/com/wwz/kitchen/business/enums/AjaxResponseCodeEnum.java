package com.wwz.kitchen.business.enums;

/**
 * Created by wenzhi.wang.
 * on 2024/11/15.
 */
public enum AjaxResponseCodeEnum {
    // 成功
    SUCCESS(200, "操作成功"),
    // 客户端错误
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请登录"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源未找到"),
    // 服务器错误
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    // 自定义业务错误
    VALIDATION_ERROR(1001, "参数校验失败"),
    DATA_NOT_EXIST(1002, "数据不存在"),
    OPERATION_FAILED(1003, "操作失败"),
    EMAIL_FORMAT_ERROR(1004,"邮箱格式错误"),
    EMAIL_CODE_ERROR(1005,"邮箱验证码错误"),
    PASSWORD_VALIDATION_ERROR(1006,"注册二次校验密码不一致"),
    USERNAME_EXIST_ERROR(1007,"用户名已存在"),
    EMAIL_EXIST_ERROR(1008,"邮箱已注册"),
    EMAIL_LIMIT_ERROR(1009,"邮箱发送次数已达日上限"),
    USERNAME_NOT_EXIST_ERROR(10010,"用户名不存在"),
    USER_TOKEN_ERROR(10011,"token错误"),
    USER_AVATAR_ERROR(10012,"头像上传错误"),
    HAS_SHARED(10013,"已分享");
    private final int code;
    private final String message;

    AjaxResponseCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
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
    public static AjaxResponseCodeEnum fromCode(int code) {
        for (AjaxResponseCodeEnum responseCode : values()) {
            if (responseCode.getCode() == code) {
                return responseCode;
            }
        }
        return null;
    }
}
