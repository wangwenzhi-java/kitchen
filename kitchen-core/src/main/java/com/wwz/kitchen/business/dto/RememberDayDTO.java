package com.wwz.kitchen.business.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wenzhi.wang.
 * on 2024/11/20.
 */
@Data
public class RememberDayDTO {
    private Integer id;

    @NotEmpty(message = "Remember Day cannot be empty")
    private String rememberDay; // 接收前端传递的日期字符串

    @NotEmpty(message = "Title cannot be empty")
    private String title;

    // 转换日期的辅助方法
    public Date getFormattedRememberDay() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 根据前端传递的日期格式
        return rememberDay != null ? sdf.parse(rememberDay) : null;
    }
}
