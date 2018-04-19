package com.netease.nim.musiceducation;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.netease.nim.musiceducation.app.AppCache;
import com.netease.nim.musiceducation.protocol.model.JsonObject2Model;
import com.netease.nim.musiceducation.protocol.model.RoomInfo;

/**
 * Created by hzxuwen on 2015/4/13.
 */
public class AuthPreferences {
    private static final String KEY_USER_ACCOUNT = "account";
    private static final String KEY_USER_TOKEN = "token";
    private static final String KEY_USER_TYPE = "type";
    private static final String KEY_ROOM_INFO = "roominfo";

    public static RoomInfo getRoomInfo() {
        String jsonString = getSharedPreferences().getString(KEY_ROOM_INFO, "");
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        RoomInfo roomInfo = (RoomInfo) JsonObject2Model.parseJsonObjectToModule(jsonObject, RoomInfo.class);
        return roomInfo;
    }

    public static void saveRoomInfo(RoomInfo roomInfo) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        JSONObject jsonObject = new JSONObject();
        if (roomInfo == null) {
            editor.putString(KEY_ROOM_INFO, null);
            editor.commit();
            return;
        }
        try {
            jsonObject.put("roomId", roomInfo.getRoomId());
            jsonObject.put("studentAccid", roomInfo.getStudentAccount());
            jsonObject.put("studentName", roomInfo.getStudentName());
            jsonObject.put("teacherAccid", roomInfo.getTeacherAccount());
            jsonObject.put("teacherName", roomInfo.getTeacherName());
            jsonObject.put("teacherPassword", roomInfo.getTeacherPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.putString(KEY_ROOM_INFO, jsonObject.toString());
        editor.commit();
    }

    public static void saveUserType(int type) {
        saveInt(KEY_USER_TYPE, type);
    }

    public static int getKeyUserType() {
        return getInt(KEY_USER_TYPE);
    }

    public static void saveUserAccount(String account) {
        saveString(KEY_USER_ACCOUNT, account);
    }

    public static String getUserAccount() {
        return getString(KEY_USER_ACCOUNT);
    }

    public static void saveUserToken(String token) {
        saveString(KEY_USER_TOKEN, token);
    }

    public static String getUserToken() {
        return getString(KEY_USER_TOKEN);
    }

    private static void saveInt(String key, int value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private static int getInt(String key) {
        return getSharedPreferences().getInt(key, -1);
    }

    private static void saveString(String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(key, value);
        editor.commit();
    }

    private static String getString(String key) {
        return getSharedPreferences().getString(key, null);
    }

    static SharedPreferences getSharedPreferences() {
        return AppCache.getContext().getSharedPreferences("music", Context.MODE_PRIVATE);
    }
}
