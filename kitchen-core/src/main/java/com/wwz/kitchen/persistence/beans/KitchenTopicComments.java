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
 * 喵圈帖子评论/回复
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-12-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("kitchen_topic_comments")
public class KitchenTopicComments implements Serializable {

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
     * 所属帖子ID
     */
    @TableField("topic_id")
    private Integer topicId;

    /**
     * 如果是回复，指向父评论/回复的ID；根评论为0
     */
    @TableField("parent_id")
    private Integer parentId;

    /**
     * 评论/回复的内容
     */
    @TableField("content")
    private String content;

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
