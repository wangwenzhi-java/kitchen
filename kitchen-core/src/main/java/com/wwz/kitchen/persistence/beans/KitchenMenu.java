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
 * @since 2024-11-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("kitchen_menu")
public class KitchenMenu implements Serializable {

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
     * 类型id
     */
    @TableField("cid")
    private Integer cid;
    /**
     * 大类id
     */
    @TableField("tid")
    private Integer tid;

    /**
     * 菜名或服务名
     */
    @TableField("title")
    private String title;

    /**
     * 照片
     */
    @TableField("pic")
    private String pic;

    /**
     * 做法
     */
    @TableField("cookbook")
    private String cookbook;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 权重
     */
    @TableField("sort")
    private Integer sort;

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
