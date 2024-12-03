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
 * @since 2024-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("kitchen_users")
public class KitchenUsers implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户名 唯一
     */
    @TableField("username")
    private String username;

    /**
     * 用户昵称 可重复
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 加密密码
     */
    @TableField("password")
    private String password;

    /**
     * 头像
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 用户邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 用户手机
     */
    @TableField("phone")
    private String phone;

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
