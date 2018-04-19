package com.netease.nim.musiceducation.app;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.netease.nim.musiceducation.AuthPreferences;
import com.netease.nim.musiceducation.app.crash.AppCrashHandler;
import com.netease.nim.musiceducation.common.LogUtil;
import com.netease.nim.musiceducation.common.http.NimHttpClient;
import com.netease.nim.musiceducation.common.utils.StorageUtil;
import com.netease.nim.musiceducation.protocol.Servers;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.util.NIMUtil;

/**
 * Created by winnie on 2018/1/9.
 */

public class MusicApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCache.setContext(this);
        NIMClient.init(this, getLoginInfo(), getSDKOptions());

        if (NIMUtil.isMainProcess(this)) {
            initMainProcess();
            NimHttpClient.getInstance().init(getApplicationContext()); // 初始化HttpClient
        }
    }

    private LoginInfo getLoginInfo() {
        String account = AuthPreferences.getUserAccount();
        String token = AuthPreferences.getUserToken();

        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
            AppCache.setAccount(account.toLowerCase());
            return new LoginInfo(account, token);
        } else {
            return null;
        }
    }

    private SDKOptions getSDKOptions() {
        SDKOptions options = new SDKOptions();
        options.appKey = Servers.getAppKey();
        options.reducedIM = true;
        options.asyncInitSDK = true;
        options.sdkStorageRootPath = StorageUtil.getAppCacheDir(getApplicationContext()) + "/music"; // 可以不设置，那么将采用默认路径

        return options;
    }

    private void initMainProcess() {
        AppCache.setContext(getApplicationContext());

        final String logDir = StorageUtil.getAppCacheDir(getApplicationContext()) + "/app/log";
        AppCrashHandler.init(this, logDir);
        LogUtil.init(logDir, Log.DEBUG);
    }

}
