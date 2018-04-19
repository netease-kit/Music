package com.netease.mmc.demo.web.util;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.netease.mmc.demo.dao.domain.StudentDO;
import com.netease.mmc.demo.service.model.RoomModel;
import com.netease.mmc.demo.service.model.TouristModel;
import com.netease.mmc.demo.web.vo.StudentVO;
import com.netease.mmc.demo.web.vo.TeacherRoomVO;
import com.netease.mmc.demo.web.vo.TouristVO;


/**
 * Model转换工具类.
 *
 * @author hzwanglin1
 * @date 17-6-26
 * @since 1.0
 */
@Mapper
public interface VOUtil {
    VOUtil INSTANCE = Mappers.getMapper(VOUtil.class);

    TouristVO touristModel2VO(TouristModel touristModel);

    StudentVO studentDO2VO(StudentDO studentDO);

    TeacherRoomVO roomModel2TeacherRoomVO(RoomModel roomModel);

    List<TeacherRoomVO> roomModelList2TeacherRoomVOList(List<RoomModel> roomModelList);
}
