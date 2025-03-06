package com.wwz.kitchen.business.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.wwz.kitchen.persistence.beans.KitchenUsers;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by wenzhi.wang.
 * on 2024/12/5.
 */
@Data
public class KitchenTopicDTO implements Serializable {

    private Integer id;

    private String topicText;
    private String topicMedia;

    private Integer topicType;
    private Date createTime;

    private Integer uid;
    private String username;
    private String avatar;

}
