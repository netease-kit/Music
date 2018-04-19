package com.netease.mmc.demo.common.enums;

/**
 * 课程房间状态.
 *
 * @author hzwanglin1
 * @date 2018/4/3
 * @since 1.0
 */
public enum RoomStatusEnum {
    /**
     * 开启
     */
    OPEN(0),
    /**
     * 已下课
     */
    CLOSE(1),
    ;
    private int value;

    RoomStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static RoomStatusEnum getEnum(Integer value) {
        if (value == null) {
            return null;
        }
        for (RoomStatusEnum typeEnum : RoomStatusEnum.values()) {
            if (typeEnum.getValue() == value) {
                return typeEnum;
            }
        }
        return null;
    }
}
