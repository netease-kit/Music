package com.netease.mmc.demo.web.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netease.mmc.demo.common.enums.HttpCodeEnum;
import com.netease.mmc.demo.common.util.DataPack;
import com.netease.mmc.demo.service.RoomService;
import com.netease.mmc.demo.service.UserService;
import com.netease.mmc.demo.service.model.RoomModel;
import com.netease.mmc.demo.service.model.UserModel;
import com.netease.mmc.demo.web.util.VOUtil;

/**
 * 老师课程相关.
 *
 * @author hzwanglin1
 * @date 2018/4/2
 * @since 1.0
 */
@Controller
@RequestMapping("music/teacher/room")
public class TeacherRoomController {

    @Resource
    private RoomService roomService;
    @Resource
    private UserService userService;

    /**
     * 查询当前课程房间
     *
     * @return
     */
    @RequestMapping(value = "query", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ModelMap queryRoom(@RequestParam(value = "sid") String accid) {
        UserModel userModel = userService.queryTeacherByAccid(accid);
        if (userModel == null) {
            return DataPack.packFailure(HttpCodeEnum.UNAUTHORIZED);
        }
        List<RoomModel> roomModels = roomService.queryRoomByTeacher(accid);

        return DataPack.packOkList(VOUtil.INSTANCE.roomModelList2TeacherRoomVOList(roomModels));
    }

    /**
     * 关闭房间（下课）
     *
     * @param accid
     * @param roomId
     * @return
     */
    @RequestMapping(value = "close", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ModelMap closeRoom(@RequestParam(value = "sid") String accid, @RequestParam(value = "roomId") Long roomId) {
        UserModel userModel = userService.queryTeacherByAccid(accid);
        if (userModel == null) {
            return DataPack.packFailure(HttpCodeEnum.UNAUTHORIZED);
        }
        if (!roomService.existsRoomOfTeacher(roomId, accid)) {
            return DataPack.packFailure(HttpCodeEnum.CHATROOM_NOT_FOUND);
        }
        if (roomService.closeRoom(roomId)) {
            return DataPack.packOk();
        } else {
            return DataPack.packInternalError();
        }
    }

}