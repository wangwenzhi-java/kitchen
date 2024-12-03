package com.wwz.kitchen.persistence.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by wenzhi.wang.
 * on 2024/11/14.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class BaseBean implements Serializable {
    /**
     * @fieldName: serialVersionUID
     * @fieldType: long
     */
    private static final long serialVersionUID = 5088697673359856350L;


    @Id
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Date createTime;
    private Date updateTime;
}
