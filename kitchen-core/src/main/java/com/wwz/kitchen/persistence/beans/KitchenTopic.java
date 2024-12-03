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
 *  喵圈帖子
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-12-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("kitchen_topic")
public class KitchenTopic implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id 暂不支持匿名发帖 必须字段
     */
    @TableField("uid")
    private Integer uid;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 帖子文本内容
     */
    @TableField("topic_text")
    private String topicText;

    /**
     * 帖子多媒体内容
     */
    @TableField("topic_media")
    private String topicMedia;

    /**
     * 帖子类型0.文本 1.多媒体
     */
    @TableField("topic_type")
    private Integer topicType;

    /**
     * 是否可用'RELEASE','NOT_RELEASE'
     */
    @TableField("status")
    private String status;

    /**
     * 发布时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;


}
