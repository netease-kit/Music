package com.netease.mmc.demo.dao;

import org.apache.ibatis.annotations.Param;

import com.netease.mmc.demo.dao.domain.StudentDO;

/**
 * StudentDao table demo_music_student's dao.
 *
 * @author hzwanglin1
 * @date 2018-04-01
 * @since 1.0
 */
public interface StudentDao {
    int insertSelective(StudentDO record);

    StudentDO findByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(StudentDO record);

    /**
     * 判断账号是否已注册
     *
     * @param accid 学生账号
     * @return
     */
    boolean existsStudent(@Param("accid") String accid);

    /**
     * 根据accid查找用户
     *
     * @param accid
     * @return
     */
    StudentDO findByAccid(@Param("accid") String accid);
}