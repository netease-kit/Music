package com.netease.nim.musiceducation.protocol.model;

import com.netease.nim.musiceducation.common.annotation.KeepMemberNames;

import java.io.Serializable;
import java.util.List;

@KeepMemberNames
public class ClassInfo implements Serializable {
    private int total;
    private List<RoomInfo> list;

    public int getTotal() {
        return total;
    }

    public List<RoomInfo> getList() {
        return list;
    }


    public static ClassInfo createFakeClassInfo(int total) {
        ClassInfo c = new ClassInfo();
        c.total = total;
        return c;
    }
}
