package com.netease.mmc.demo.web.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netease.mmc.demo.common.context.WebContextHolder;
import com.netease.mmc.demo.common.enums.HttpCodeEnum;
import com.netease.mmc.demo.common.session.SessionUserModel;
import com.netease.mmc.demo.common.util.DataPack;
import com.netease.mmc.demo.service.RoomService;
import com.netease.mmc.demo.service.model.BizResultModel;
import com.netease.mmc.demo.service.model.RoomModel;

/**
 * 学生课程相关.
 *
 * @author hzwanglin1
 * @date 2018/4/2
 * @since 1.0
 */
@Controller
@RequestMapping("music/room")
public class StudentRoomController {

    @Resource
    private RoomService roomService;

    /**
     * 学生创建课程房间
     *
     * @return
     */
    @RequestMapping(value = "create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ModelMap createRoom() {
        SessionUserModel user = (SessionUserModel) WebContextHolder.getCurrentUser();
        if (roomService.countRoomByStudent(user.getAccid()) > 0) {
            return DataPack.packFailure(HttpCodeEnum.ROOM_COUNT_LIMIT);
        }
        BizResultModel<RoomModel> resultModel = roomService.createRoom(user.getAccid(), user.getNickname());
        if (resultModel.isSuccess()) {
            return DataPack.packOk(resultModel.getData());
        } else {
            return DataPack.packFailure(resultModel.getCode(), resultModel.getMessage());
        }
    }

    /**
     * 查询当前课程房间
     *
     * @return
     */
    @RequestMapping(value = "query", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ModelMap queryRoom() {
        SessionUserModel user = (SessionUserModel) WebContextHolder.getCurrentUser();
        List<RoomModel> roomModels = roomService.queryRoomByStudent(user.getAccid());

        return DataPack.packOkList(roomModels);
    }

}