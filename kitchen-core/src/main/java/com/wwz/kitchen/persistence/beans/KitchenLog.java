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
@TableName("kitchen_log")
public class KitchenLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 已登录用户ID
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 日志类型（系统操作日志，用户访问日志，异常记录日志）
     */
    @TableField("type")
    private String type;

    /**
     * 日志级别
     */
    @TableField("log_level")
    private String logLevel;

    /**
     * 日志内容（业务操作）
     */
    @TableField("content")
    private String content;

    /**
     * 请求参数（业务操作）
     */
    @TableField("params")
    private String params;

    /**
     * 操作用户的ip
     */
    @TableField("ip")
    private String ip;

    /**
     * 评论时的浏览器类型
     */
    @TableField("browser")
    private String browser;

    /**
     * 请求的路径
     */
    @TableField("request_url")
    private String requestUrl;

    /**
     * 请求来源地址
     */
    @TableField("referer")
    private String referer;

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
