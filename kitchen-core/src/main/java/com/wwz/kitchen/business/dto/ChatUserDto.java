package com.wwz.kitchen.business.dto;

import lombok.Data;

/**
 * Created by wenzhi.wang.
 * on 2024/11/23.
 */
@Data
public class ChatUserDto {

    private Integer id;
    private String username;
    private String avatar;

    private Integer isOnline;//0离线 1在线

    private Integer status;//添加好友搜索时返回的状态 如 已经是好友 已经添加未确认

    private Integer unreadMessages;//未读消息数量
}
