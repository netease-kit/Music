package com.netease.mmc.demo.service.util;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.netease.mmc.demo.dao.domain.RoomDO;
import com.netease.mmc.demo.dao.domain.StudentDO;
import com.netease.mmc.demo.dao.domain.TeacherDO;
import com.netease.mmc.demo.dao.domain.TouristDO;
import com.netease.mmc.demo.service.model.RoomModel;
import com.netease.mmc.demo.service.model.TouristModel;
import com.netease.mmc.demo.service.model.UserModel;


/**
 * Model转换工具类.
 *
 * @author hzwanglin1
 * @date 17-6-26
 * @since 1.0
 */
@Mapper
public interface ModelUtil {
    ModelUtil INSTANCE = Mappers.getMapper(ModelUtil.class);

    /**
     * 将TouristDO转换为TouristModel
     *
     * @param touristDO
     * @return
     */
    TouristModel touristDO2Model(TouristDO touristDO);

    UserModel studentDo2UserModel(StudentDO studentDO);

    UserModel studentDo2UserModel(TeacherDO teacherDO);

    @Mapping(source = "id", target = "roomId")
    RoomModel roomDO2Model(RoomDO roomDO);

}
