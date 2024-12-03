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
 * 纪念日
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("kitchen_remember_day")
public class KitchenRememberDay implements Serializable {

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
     * 纪念日名称
     */
    @TableField("title")
    private String title;
    /**
     * 是否可用1--可用  0--不可用
     */
    @TableField("status")
    private String status;

    /**
     * 纪念日
     */
    @TableField("remember_day")
    private Date rememberDay;

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
