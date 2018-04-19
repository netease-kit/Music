package com.netease.nim.musiceducation.protocol.model;


import com.netease.nim.musiceducation.common.annotation.KeepMemberNames;

import java.io.Serializable;

/**
 * Created by huangjun on 2017/11/20.
 */
@KeepMemberNames
public class RoomInfo implements Serializable {
    private String roomId;
    private String teacherAccid;
    private String teacherName;
    private String teacherPassword;
    private String studentAccid;
    private String studentName;

    public String getRoomId() {
        return roomId;
    }

    public String getTeacherName() {
        return teacherName;
    }


    public String getTeacherAccount() {
        return teacherAccid;
    }

    public String getTeacherPassword() {
        return teacherPassword;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentAccount() {
        return studentAccid;
    }

    public static RoomInfo createFakeRoomInfo(String roomId, String name) {
        RoomInfo r = new RoomInfo();
        r.roomId = roomId;
        r.teacherName = name;
        return r;
    }
}
