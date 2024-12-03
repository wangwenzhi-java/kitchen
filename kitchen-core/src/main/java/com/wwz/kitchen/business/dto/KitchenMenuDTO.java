package com.wwz.kitchen.business.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Created by wenzhi.wang.
 * on 2024/11/18.
 */
@Data
public class KitchenMenuDTO {

    private Integer id;

    @NotNull(message = "分类id不能为空")
    private Integer cid;
    @NotNull(message = "大类id不能为空")
    private Integer tid;

    @NotBlank(message = "名称不能为空")
    private String title;
    @NotBlank(message = "照片不能为空")
    private String pic;
    @NotBlank(message = "描述不能为空")
    private String description;

    private String cookbook;

}
