package com.wwz.kitchen.business.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 注册数据传输
 * Created by wenzhi.wang.
 * on 2024/11/15.
 */
@Data
public class KitchenUsersRegistryDTO {
    private Integer id;

    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    @NotBlank(message = "二次密码不能为空")
    private String rePassword;
    @NotBlank(message = "email不能为空")
    private String email;

    private String nickname;
    private String avatar;
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String code; //邮箱验证码

}
