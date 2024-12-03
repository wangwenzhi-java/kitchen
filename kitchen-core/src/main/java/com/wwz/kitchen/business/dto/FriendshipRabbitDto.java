package com.wwz.kitchen.business.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by wenzhi.wang.
 * on 2024/11/23.
 */
@Data
public class FriendshipRabbitDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private int type;//MessageTypeEnum

    private int sendUid;
    private int receiveUid;//friend

    private boolean accept;//申请专用 如好友申请、请求分享菜单
}
