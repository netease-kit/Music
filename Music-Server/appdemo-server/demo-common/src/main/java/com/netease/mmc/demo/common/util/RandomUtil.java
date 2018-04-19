package com.netease.mmc.demo.common.util;

import java.util.Random;

/**
 * 随机数工具类.
 *
 * @author hzwanglin1
 * @date 2018/4/4
 * @since 1.0
 */
public class RandomUtil {
    private RandomUtil() {
        throw new UnsupportedOperationException("RandomUtil.class can not be construct to a instance");
    }

    /**
     * 生成随机密码
     *
     * 由大小写英文字母，数字组成
     * 为了不容易混淆，排除l、I、O三个字符
     *
     * @param length 生产的密码长度
     * @return
     */
    public static String randomPassword(int length) {
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int numOrChar = new Random().nextInt(2);
            if (numOrChar == 0) {
                // 生成数字
                password.append(new Random().nextInt(10));
            } else {
                // 生成字母
                // 大写英文字母ascii码范围为 65-90
                // 小写英文字母 97-122
                int upperOrLower = new Random().nextInt(2);
                char letter;
                if (upperOrLower == 0) {
                    letter = (char) (new Random().nextInt(26) + 65);
                } else {
                    letter = (char) (new Random().nextInt(26) + 97);
                }
                switch (letter) {
                    case 'l':
                        letter = 'L';
                        break;
                    case 'I':
                        letter = 'i';
                        break;
                    case 'O':
                        letter = 'o';
                        break;
                    default:
                }
                password.append(letter);
            }
        }
        return password.toString();
    }
}