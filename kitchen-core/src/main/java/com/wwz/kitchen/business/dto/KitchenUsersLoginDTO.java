package com.wwz.kitchen.business.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录DTO
 * Created by wenzhi.wang.
 * on 2024/11/15.
 */
@Data
public class KitchenUsersLoginDTO {
    private Integer id;

    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
}
