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
 * 甄选订单
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("kitchen_pick")
public class KitchenPick implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 用户id
     */
    @TableField("uid")
    private Integer uid;

    /**
     * 类型 1.pick 2.order
     */
    @TableField("type")
    private Integer type;

    /**
     * 是否可用1--可用  0--不可用
     */
    @TableField("status")
    private String status;

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
