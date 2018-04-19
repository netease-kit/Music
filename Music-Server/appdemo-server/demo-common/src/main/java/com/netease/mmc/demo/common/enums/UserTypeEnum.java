package com.netease.mmc.demo.common.enums;

/**
 * 用户账号类型枚举.
 *
 * @author hzwanglin1
 * @date 2018/4/4
 * @since 1.0
 */
public enum UserTypeEnum {
    /**
     * 学生
     */
    STUDENT(0),
    /**
     * 老师
     */
    TEACHER(1),
    ;
    private int value;

    UserTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static UserTypeEnum getEnum(Integer value) {
        if (value == null) {
            return null;
        }
        for (UserTypeEnum typeEnum : UserTypeEnum.values()) {
            if (typeEnum.getValue() == value) {
                return typeEnum;
            }
        }
        return null;
    }
}
