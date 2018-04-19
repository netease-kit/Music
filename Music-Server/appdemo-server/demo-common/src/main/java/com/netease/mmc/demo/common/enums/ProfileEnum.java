package com.netease.mmc.demo.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 配置环境相关枚举.
 *
 * @author hzwanglin1
 * @date 2017/7/11
 * @since 1.0
 */
public enum ProfileEnum {
    /**
     * 开发环境
     */
    DEV,
    /**
     * 测试环境
     */
    TEST,
    /**
     * 预发布环境
     */
    PRE,
    /**
     * 线上环境
     */
    PROD;

    public static ProfileEnum getEnum(String value) {
        if (value == null) {
            return null;
        }
        for (ProfileEnum typeEnum : ProfileEnum.values()) {
            if (StringUtils.equalsIgnoreCase(typeEnum.name(), value)) {
                return typeEnum;
            }
        }
        return null;
    }
}
