package com.wwz.kitchen.persistence.beans;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("kitchen_message")
public class KitchenMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 发送消息的用户 ID
     */
    @TableField("sender_id")
    private Integer senderId;

    /**
     * 接收消息的用户 ID
     */
    @TableField("receiver_id")
    private Integer receiverId;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 1.文本 2图片 3甄选 4菜单或服务 5.订单
     */
    @TableField("message_type")
    private Integer messageType;

    /**
     * 'RELEASE', 'NOT_RELEASE'
     */
    @TableField("status")
    private String status;
    /**
     * 0.未读 1.已读
     */
    @TableField("is_read")
    private Integer isRead;

    /**
     * 添加时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;


}
