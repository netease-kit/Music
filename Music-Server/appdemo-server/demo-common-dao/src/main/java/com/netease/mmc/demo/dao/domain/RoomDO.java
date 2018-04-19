package com.netease.mmc.demo.dao.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * This class corresponds to the database table demo_music_room
 *
 * @author hzwanglin1
 */
public class RoomDO implements Serializable {
    /**
     * Database Table : demo_music_room; 
     * Database Column : id; 
     * Database Column Remarks : 
     *   主键ID
     */
    private Long id;

    /**
     * Database Table : demo_music_room; 
     * Database Column : name; 
     * Database Column Remarks : 
     *   房间名称
     */
    private String name;

    /**
     * Database Table : demo_music_room; 
     * Database Column : status; 
     * Database Column Remarks : 
     *   房间状态，0-初始状态，1-已下课
     */
    private Integer status;

    /**
     * Database Table : demo_music_room; 
     * Database Column : teacher_accid; 
     * Database Column Remarks : 
     *   老师账号
     */
    private String teacherAccid;

    /**
     * Database Table : demo_music_room; 
     * Database Column : student_accid; 
     * Database Column Remarks : 
     *   学生账号
     */
    private String studentAccid;

    /**
     * Database Table : demo_music_room; 
     * Database Column : expired_at; 
     * Database Column Remarks : 
     *   课程失效时间
     */
    private Long expiredAt;

    /**
     * Database Table : demo_music_room; 
     * Database Column : created_at; 
     * Database Column Remarks : 
     *   创建时间
     */
    private Date createdAt;

    /**
     * Database Table : demo_music_room; 
     * Database Column : updated_at; 
     * Database Column Remarks : 
     *   更新时间
     */
    private Date updatedAt;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getTeacherAccid() {
        return teacherAccid;
    }

    public void setTeacherAccid(String teacherAccid) {
        this.teacherAccid = teacherAccid;
    }

    public String getStudentAccid() {
        return studentAccid;
    }

    public void setStudentAccid(String studentAccid) {
        this.studentAccid = studentAccid;
    }

    public Long getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Long expiredAt) {
        this.expiredAt = expiredAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("{");
        sb.append(", id=").append(id);
        sb.append(", name=").append(name);
        sb.append(", status=").append(status);
        sb.append(", teacherAccid=").append(teacherAccid);
        sb.append(", studentAccid=").append(studentAccid);
        sb.append(", expiredAt=").append(expiredAt);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("}");
        return sb.toString();
    }
}