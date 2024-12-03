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
@TableName("kitchen_friendship")
public class KitchenFriendship implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    @TableField("uid")
    private Integer uid;

    /**
     * 好友uid
     */
    @TableField("friend_id")
    private Integer friendId;
    /**
     * 排序 倒序
     */
    @TableField("sort")
    private Integer sort;
    /**
     * 0：待确认。
        1：已添加。
        2：已拒绝。
        3：已拉黑
     4.已删除
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
