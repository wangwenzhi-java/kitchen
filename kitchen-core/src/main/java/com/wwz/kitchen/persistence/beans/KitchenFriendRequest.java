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
@TableName("kitchen_friend_request")
public class KitchenFriendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 发起请求的用户 ID
     */
    @TableField("sender_id")
    private Integer senderId;

    /**
     * 接收请求的用户 ID
     */
    @TableField("receiver_id")
    private Integer receiverId;

    /**
     * 请求附带消息
     */
    @TableField("message")
    private String message;

    /**
     * 0待处理、1已同意、2已拒绝
     */
    @TableField("status")
    private Integer status;

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
