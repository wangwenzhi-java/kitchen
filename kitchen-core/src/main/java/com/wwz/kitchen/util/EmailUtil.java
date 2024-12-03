package com.wwz.kitchen.util;

import java.util.regex.Pattern;

/**
 * Created by wenzhi.wang.
 * on 2024/11/15.
 */
public class EmailUtil {
    /**
     * 验证邮箱格式
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        if (email == null || email.length() < 1 || email.length() > 256) {
            return false;
        }
        Pattern pattern = Pattern.compile("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");
        return pattern.matcher(email).matches();
    }
}
