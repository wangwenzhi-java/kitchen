package com.wwz.kitchen.business.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by wenzhi.wang.
 * on 2024/11/17.
 */
@Data
public class KitchenUserModeDto {

    @NotNull(message = "用户模式不能为空")
    private Integer kitchenUserMode;
}
