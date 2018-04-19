package com.netease.mmc.demo.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.netease.mmc.demo.common.enums.HttpCodeEnum;
import com.netease.mmc.demo.common.enums.RoomStatusEnum;
import com.netease.mmc.demo.dao.RoomDao;
import com.netease.mmc.demo.dao.StudentDao;
import com.netease.mmc.demo.dao.TeacherDao;
import com.netease.mmc.demo.dao.domain.RoomDO;
import com.netease.mmc.demo.dao.domain.StudentDO;
import com.netease.mmc.demo.dao.domain.TeacherDO;
import com.netease.mmc.demo.httpdao.nim.util.NIMErrorCode;
import com.netease.mmc.demo.service.model.BizResultModel;
import com.netease.mmc.demo.service.model.RoomModel;
import com.netease.mmc.demo.service.util.ModelUtil;

/**
 * 课程房间相关Service.
 *
 * @author hzwanglin1
 * @date 2018/4/2
 * @since 1.0
 */
@Service
public class RoomService {
    /**
     * 课程最长持续时间1天
     */
    @Value("${lesson.time.max}")
    private long maxTimeOfRoom;

    @Resource
    private UserService userService;
    @Resource
    private RoomDao roomDao;
    @Resource
    private StudentDao studentDao;
    @Resource
    private TeacherDao teacherDao;

    /**
     * 创建课程房间
     *
     * @param studentAccid 学生账号
     * @param studentName 学生名称
     * @return
     */
    public BizResultModel<RoomModel> createRoom(String studentAccid, String studentName) {
        // 分配老师
        BizResultModel<TeacherDO> allocResult = userService.allocateTeacher();
        if (!allocResult.isSuccess()) {
            // 如果老师账号冲突，尝试重新分配一次
            if (Objects.equals(allocResult.getCode(), NIMErrorCode.ILLEGAL_PARAM.value())) {
                allocResult = userService.allocateTeacher();
                if (!allocResult.isSuccess()) {
                    return new BizResultModel<>(HttpCodeEnum.INTERNAL_SERVER_ERROR.value(), "分配老师失败");
                }
            } else {
                return new BizResultModel<>(HttpCodeEnum.INTERNAL_SERVER_ERROR.value(), "分配老师失败");
            }
        }
        TeacherDO teacherDO = allocResult.getData();
        // 新增课程房间
        RoomDO roomDO = new RoomDO();
        roomDO.setStatus(RoomStatusEnum.OPEN.getValue());
        roomDO.setStudentAccid(studentAccid);
        roomDO.setTeacherAccid(teacherDO.getAccid());
        roomDO.setExpiredAt(System.currentTimeMillis() + maxTimeOfRoom);
        if (roomDao.insertSelective(roomDO) < 1) {
            return new BizResultModel<>(HttpCodeEnum.INTERNAL_SERVER_ERROR.value(), "创建房间失败");
        }
        // 封装返回值
        RoomModel roomModel = ModelUtil.INSTANCE.roomDO2Model(roomDO);
        roomModel.setStudentName(studentName);
        roomModel.setTeacherName(teacherDO.getNickname());
        roomModel.setTeacherPassword(teacherDO.getPassword());
        return new BizResultModel<>(roomModel);
    }

    /**
     * 查询课程房间信息
     *
     * @param studentAccid
     * @return
     */
    public List<RoomModel> queryRoomByStudent(String studentAccid) {
        List<RoomDO> roomDOS = roomDao.listByStudentAccid(studentAccid, System.currentTimeMillis());
        if (CollectionUtils.isEmpty(roomDOS)) {
            return Collections.emptyList();
        }
        return roomDOList2ModelList(roomDOS);
    }

    /**
     * 查询学生当前正预约的课程数量
     *
     * @param studentAccid
     * @return
     */
    public int countRoomByStudent(String studentAccid) {
        return roomDao.countByStudentAccid(studentAccid, System.currentTimeMillis());
    }

    /**
     * 查询课程房间信息
     *
     * @param teacherAccid
     *
     * @return
     */
    public List<RoomModel> queryRoomByTeacher(String teacherAccid) {
        List<RoomDO> roomDOS = roomDao.listByTeacherAccid(teacherAccid, System.currentTimeMillis());
        if (CollectionUtils.isEmpty(roomDOS)) {
            return Collections.emptyList();
        }
        return roomDOList2ModelList(roomDOS);
    }

    /**
     * 是否存在指定老师的指定房间
     *
     * @param roomId
     * @param teacherAccid
     * @return
     */
    public boolean existsRoomOfTeacher(long roomId, String teacherAccid) {
        return roomDao.existsByRoomIdAndTeacherAccid(roomId, teacherAccid);
    }

    /**
     * 关闭课程房间，下课
     *
     * @param roomId
     * @return
     */
    public boolean closeRoom(long roomId) {
        return roomDao.updateRoomStatus(roomId, RoomStatusEnum.CLOSE.getValue());
    }

    /**
     * 课程房间模型转换
     *
     * @param roomDOS
     * @return
     */
    private List<RoomModel> roomDOList2ModelList(@Nonnull List<RoomDO> roomDOS) {
        List<RoomModel> resultList = Lists.newArrayListWithCapacity(roomDOS.size());
        for (RoomDO roomDO : roomDOS) {
            RoomModel roomModel = ModelUtil.INSTANCE.roomDO2Model(roomDO);
            // 查询学生信息
            StudentDO studentDO = studentDao.findByAccid(roomDO.getStudentAccid());
            roomModel.setStudentName(studentDO.getNickname());
            // 查询老师信息
            TeacherDO teacherDO = teacherDao.findByAccid(roomDO.getTeacherAccid());
            roomModel.setTeacherPassword(teacherDO.getPassword());
            roomModel.setTeacherName(teacherDO.getNickname());
            resultList.add(roomModel);
        }
        return resultList;
    }
}