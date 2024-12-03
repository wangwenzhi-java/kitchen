package com.wwz.kitchen.business.dto;

/**
 * Created by wenzhi.wang.
 * on 2024/11/15.
 */
public class JwtResponseDto {

    private String token;

    public JwtResponseDto(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
