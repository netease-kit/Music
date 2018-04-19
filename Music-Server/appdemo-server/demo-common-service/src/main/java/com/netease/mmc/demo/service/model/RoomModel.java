package com.netease.mmc.demo.service.model;

/**
 * 课程房间信息Model.
 *
 * @author hzwanglin1
 * @date 2018/4/3
 * @since 1.0
 */
public class RoomModel {
    /**
     * 房间id
     */
    private Long roomId;

    /**
     * 老师账号
     */
    private String teacherAccid;

    /**
     * 老师名称
     */
    private String teacherName;

    /**
     * 老师账号密码
     */
    private String teacherPassword;

    /**
     * 学生账号
     */
    private String studentAccid;

    /**
     * 学生名称
     */
    private String studentName;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getTeacherAccid() {
        return teacherAccid;
    }

    public void setTeacherAccid(String teacherAccid) {
        this.teacherAccid = teacherAccid;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getTeacherPassword() {
        return teacherPassword;
    }

    public void setTeacherPassword(String teacherPassword) {
        this.teacherPassword = teacherPassword;
    }

    public String getStudentAccid() {
        return studentAccid;
    }

    public void setStudentAccid(String studentAccid) {
        this.studentAccid = studentAccid;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RoomModel{");
        sb.append("roomId=").append(roomId);
        sb.append(", teacherAccid='").append(teacherAccid).append('\'');
        sb.append(", teacherName='").append(teacherName).append('\'');
        sb.append(", teacherPassword='").append(teacherPassword).append('\'');
        sb.append(", studentAccid='").append(studentAccid).append('\'');
        sb.append(", studentName='").append(studentName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}