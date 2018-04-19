package com.netease.mmc.demo.dao;

import org.apache.ibatis.annotations.Param;

import com.netease.mmc.demo.dao.domain.TeacherDO;

/**
 * TeacherDao table demo_music_teacher's dao.
 *
 * @author hzwanglin1
 * @date 2018-04-01
 * @since 1.0
 */
public interface TeacherDao {
    int insertSelective(TeacherDO record);

    TeacherDO findByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TeacherDO record);

    TeacherDO findByAccid(@Param("accid") String accid);
}