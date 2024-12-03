package com.wwz.kitchen.util;

import java.util.Random;

/**
 * Created by wenzhi.wang.
 * on 2024/11/15.
 */
public class RandomNicknameGeneratorUtil {
    private static final String[] NICKNAME_PREFIXES = {"小", "大", "酷", "萌", "星", "神", "王", "飞"};
    private static final String[] NICKNAME_SUFFIXES = {"子", "宝", "妹", "哥", "大人", "神", "仔"};

    public static String generateRandomNickname() {
        Random random = new Random();
        String prefix = NICKNAME_PREFIXES[random.nextInt(NICKNAME_PREFIXES.length)];
        String suffix = NICKNAME_SUFFIXES[random.nextInt(NICKNAME_SUFFIXES.length)];
        return prefix + generateRandomNickname(3) + suffix; // 生成一个带前后缀的随机昵称
    }

    // 生成随机的字母和数字组合
    private static String generateRandomNickname(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder nickname = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            nickname.append(chars.charAt(index));
        }
        return nickname.toString();
    }

}
