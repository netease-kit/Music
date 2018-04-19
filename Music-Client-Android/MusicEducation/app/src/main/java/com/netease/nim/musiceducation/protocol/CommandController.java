package com.netease.nim.musiceducation.protocol;

import com.alibaba.fastjson.JSONObject;
import com.netease.nim.musiceducation.AuthPreferences;
import com.netease.nim.musiceducation.common.LogUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;

public class CommandController {

    private static final String TAG = CommandController.class.getSimpleName();
    private static CommandController instance;

    public static synchronized CommandController getInstance() {
        if (instance == null) {
            instance = new CommandController();
        }

        return instance;
    }

    /**
     * 发送下课协议
     * @param roomId 房间id
     */
    public void sendCloseCommand(String roomId) {
        CustomNotification notification = new CustomNotification();
        notification.setSessionId(AuthPreferences.getRoomInfo().getStudentAccount());
        notification.setSessionType(SessionTypeEnum.P2P);

        JSONObject json = new JSONObject();
        json.put("command", "1");
        JSONObject roomJson = new JSONObject();
        roomJson.put("roomId", roomId);
        json.put("data", roomJson);
        notification.setContent(json.toString());
        notification.setSendToOnlineUserOnly(false);

        // 发送自定义通知
        NIMClient.getService(MsgService.class).sendCustomNotification(notification).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                LogUtil.i(TAG, "send class is over success");
            }

            @Override
            public void onFailed(int code) {
                LogUtil.i(TAG, "send class is over failed, code:" + code);
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }
}
