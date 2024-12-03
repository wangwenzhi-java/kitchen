package com.wwz.kitchen.business.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Created by wenzhi.wang.
 * on 2024/11/19.
 */
@Data
public class KitchenPickDTO {

    private Integer id;

    // 非空校验：确保 title 不为空且不能为空字符串
    @NotBlank(message = "Title不能为空")
    private String title;

    // 非空校验：确保 menus 不为空且至少包含一个元素
    @NotEmpty(message = "Menus不能为空y")
    private int[] menus;

    private String dateStr;
    @NotNull(message = "type不能为空")
    private Integer type;//类型 PickTypeEnum
}
