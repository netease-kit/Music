package com.netease.mmc.demo.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.netease.mmc.demo.dao.domain.RoomDO;

/**
 * RoomDao table demo_music_room's dao.
 *
 * @author hzwanglin1
 * @date 2018-04-01
 * @since 1.0
 */
public interface RoomDao {
    int insertSelective(RoomDO record);

    RoomDO findByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RoomDO record);

    /**
     * 根据学生账号查询课程房间信息
     *
     * @param accid 学生账号
     * @param time 失效时间
     * @return
     */
    List<RoomDO> listByStudentAccid(@Param("accid") String accid, @Param("time") Long time);

    /**
     * 查询学生当前已有课程数量
     *
     * @param accid
     * @param time
     * @return
     */
    int countByStudentAccid(@Param("accid") String accid, @Param("time") Long time);

    /**
     * 根据老师账号查询课程房间信息
     *
     * @param accid 老师账号
     * @param time 失效时间
     * @return
     */
    List<RoomDO> listByTeacherAccid(@Param("accid") String accid, @Param("time") Long time);

    boolean existsByRoomIdAndTeacherAccid(@Param("roomId") long roomId, @Param("accid") String accid);

    boolean updateRoomStatus(@Param("roomId") long roomId, @Param("status") int status);
}