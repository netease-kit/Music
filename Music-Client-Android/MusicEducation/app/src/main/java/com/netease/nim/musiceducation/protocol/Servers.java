package com.netease.nim.musiceducation.protocol;

/**
 * Created by huangjun on 2017/11/19.
 */

public final class Servers {

    private final static boolean SERVER_ONLINE = true;

    private static final String APP_KEY_ONLINE = "c2b388726a789f58857501e9bafec3f5";
    private static final String APP_KEY_TEST = "32473a8745f13cb4ed2fd48b461fb089";

    private final static String SERVER_ADDRESS_ONLINE = "https://app.netease.im/appdemo";
    private final static String SERVER_ADDRESS_TEST = "http://apptest.netease.im:8080/appdemo";

    private static boolean isOnlineEnvironment() {
        return SERVER_ONLINE;
    }

    static String getServerAddress() {
        return isOnlineEnvironment() ? SERVER_ADDRESS_ONLINE : SERVER_ADDRESS_TEST;
    }

    public static String getAppKey() {
        return isOnlineEnvironment() ? APP_KEY_ONLINE : APP_KEY_TEST;
    }
}
