package com.netease.nim.musiceducation.business.helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.nim.musiceducation.business.activity.login.LogoutHelper;
import com.netease.nimlib.sdk.msg.model.CustomNotification;

public class CommandHelper {

    public interface CommandCallback {
        void onCommandReceived();
    }

    public static void showCommand(String roomId, CustomNotification message, CommandCallback callback) {
        String content = message.getContent();
        try {
            JSONObject json = JSON.parseObject(content);
            int id = json.getIntValue("command");
            if (id == 1) {
                JSONObject roomJson = json.getJSONObject("data");
                String roomIdData = roomJson.getString("roomId");
                if (roomId.equals(roomIdData)) {
                    LogoutHelper.clearRoomInfo();
                    callback.onCommandReceived();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
