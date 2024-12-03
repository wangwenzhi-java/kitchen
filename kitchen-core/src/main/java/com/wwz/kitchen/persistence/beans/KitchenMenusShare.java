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
 * @since 2024-11-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("kitchen_menus_share")
public class KitchenMenusShare implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 分享人id
     */
    @TableField("share_from_uid")
    private Integer shareFromUid;

    /**
     * 被分享人id
     */
    @TableField("share_to_uid")
    private Integer shareToUid;

    /**
     * 0.已发起还未确认 1、已确认 
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
