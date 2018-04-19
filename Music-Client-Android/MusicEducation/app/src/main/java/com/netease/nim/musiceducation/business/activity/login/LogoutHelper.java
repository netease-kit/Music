package com.netease.nim.musiceducation.business.activity.login;


import com.netease.nim.musiceducation.AuthPreferences;
import com.netease.nim.musiceducation.app.AppCache;
import com.netease.nimlib.sdk.NIMSDK;

/**
 * 注销帮助类
 * Created by huangjun on 2015/10/8.
 */
public class LogoutHelper {
    public static void logout() {
        NIMSDK.getAuthService().logout();
        AuthPreferences.saveUserToken("");
        AuthPreferences.saveUserType(-1);
        clearRoomInfo();
        AppCache.clear();
    }

    public static void clearRoomInfo() {
        AuthPreferences.saveRoomInfo(null);
    }
}
