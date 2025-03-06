package com.wwz.kitchen.business.enums;

/**
 *
 * 消息类型
 * Created by wenzhi.wang.
 * on 2024/11/23.
 */
public enum MessageTypeEnum {

    SEND_MESSAGE(1,"消息"),
    REQUEST_FRIEND(2,"好友请求"),
    DO_REQUEST_FRIEND(3,"处理好友请求"),
    SHARE_MENU(4,"分享单个菜单或服务"),
    SHARE_PICK(5,"分享甄选或订单"),
    REQUEST_SHARE_MENU(6,"请求共享菜单"),
    DO_REQUEST_SHARE_MENU(7,"处理共享菜单"),
    FRIEND_OPEN(8,"好友上线"),
    FRIEND_CLOSE(9,"好友下线"),
    DELETE_FRIEND(10,"删除好友"),

   TOPIC_PUBLISH(11,"发帖"),
   COMMENTS_PUBLISH(12,"发评论"),
   REPLY_PUBLISH(13,"回复评论");

    private int code;
    private String msg;

    MessageTypeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public static MessageTypeEnum fromValue(int code) {
        for (MessageTypeEnum messageTypeEnum : values()) {
            if (messageTypeEnum.getCode() == code) {
                return messageTypeEnum;
            }
        }
        return null;
    }
}
