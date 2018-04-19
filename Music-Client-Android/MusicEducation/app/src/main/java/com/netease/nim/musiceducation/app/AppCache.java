package com.netease.nim.musiceducation.app;

import android.content.Context;


/**
 * Created by huangjun on 2017/11/19.
 */

public class AppCache {

    private static String account;

    private static Context context;

    public static void clear() {
        account = null;
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        AppCache.context = context;
    }

    public static String getAccount() {
        return account;
    }

    public static void setAccount(String account) {
        AppCache.account = account;
    }
}
