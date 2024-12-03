package com.wwz.kitchen.business.dto;

import com.wwz.kitchen.persistence.beans.KitchenPick;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * Created by wenzhi.wang.
 * on 2024/11/23.
 */
@Data
public class ChatMessageDTO implements Serializable {
    private int type; //MessageTypeEnum

    private Integer id;//消息id

    private Integer sender;

    private Integer receiver;

    private String senderName;

    private String content;

    private String sendTime;

    private String avatar;

    private Integer messageContentType;//消息类型 1.文本 2.图片



}
