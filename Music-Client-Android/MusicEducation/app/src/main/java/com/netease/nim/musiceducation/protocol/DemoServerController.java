package com.netease.nim.musiceducation.protocol;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.netease.nim.musiceducation.common.LogUtil;
import com.netease.nim.musiceducation.common.http.NimHttpClient;
import com.netease.nim.musiceducation.common.utils.MD5;
import com.netease.nim.musiceducation.protocol.model.ClassInfo;
import com.netease.nim.musiceducation.protocol.model.JsonObject2Model;
import com.netease.nim.musiceducation.protocol.model.RoomInfo;
import com.netease.nim.musiceducation.protocol.model.UserTypeInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 网易云信Demo Http客户端。第三方开发者请连接自己的应用服务器。
 * <p/>
 * Created by huangjun on 2017/11/18.
 */
public class DemoServerController {

    private static final String TAG = "DemoServerController";

    // code
    private static final int RESULT_CODE_SUCCESS = 200;

    // api
    private static final String SERVICE_NAME = "music";
    private static final String API_NAME_REG = "user/reg";
    private static final String API_NAME_CREATE = "room/create";
    private static final String API_NAME_ROOM_QUERY = "room/query";
    private static final String API_NAME_TEACHER_ROOM_QUERY = "teacher/room/query";
    private static final String API_NAME_TEACHER_ROOM_CLOSE = "teacher/room/close";
    private static final String API_NAME_USER_CHECK = "user/check";

    // header
    private static final String HEADER_KEY_DEMO_ID = "Demo-Id";
    private static final String HEADER_KEY_APP_KEY = "appkey";

    // request
    private static final String REQUEST_USER_NAME = "accid";
    private static final String REQUEST_NICK_NAME = "nickname";
    private static final String REQUEST_PASSWORD = "password";
    private static final String REQUEST_SID = "sid";
    private static final String REQUEST_ROOMID = "roomId";

    // result
    private static final String RESULT_KEY_CODE = "code";
    private static final String RESULT_KEY_DATA = "data";
    private static final String RESULT_KEY_ERROR_MSG = "msg";

    public interface IHttpCallback<T> {
        void onSuccess(T t);

        void onFailed(int code, String errorMsg);
    }

    /**
     * 向应用服务器创建账号（注册账号）
     * 由应用服务器调用WEB SDK接口将新注册的用户数据同步到云信服务器
     */
    public void register(String account, String nickName, String password, final IHttpCallback<Void> callback) {
        String url = Servers.getServerAddress() + "/" + SERVICE_NAME + "/" + API_NAME_REG;
        password = MD5.getStringMD5(password);
        try {
            nickName = URLEncoder.encode(nickName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Map<String, String> headers = getCommonHeaders();

        StringBuilder body = new StringBuilder();
        body.append(REQUEST_USER_NAME).append("=").append(account.toLowerCase()).append("&")
                .append(REQUEST_NICK_NAME).append("=").append(nickName).append("&")
                .append(REQUEST_PASSWORD).append("=").append(password);
        String bodyString = body.toString();

        NimHttpClient.getInstance().execute(url, headers, bodyString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable exception) {
                if (code != 200 || exception != null) {
                    LogUtil.e(TAG, "http register failed, code=" + code + ", error=" + (exception != null ? exception.getMessage() : "null"));
                    if (callback != null) {
                        callback.onFailed(code, exception != null ? exception.getMessage() : null);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    int resCode = resObj.getIntValue(RESULT_KEY_CODE);
                    if (resCode == RESULT_CODE_SUCCESS) {
                        callback.onSuccess(null);
                    } else {
                        String error = resObj.getString(RESULT_KEY_ERROR_MSG);
                        callback.onFailed(resCode, error);
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }

        });
    }

    /**
     * 预约课程
     * @param account 学生帐号
     * @param callback 回调
     */
    public void bookingRoom(String account, IHttpCallback<RoomInfo> callback) {
        String url = Servers.getServerAddress() + "/" + SERVICE_NAME + "/" + API_NAME_CREATE;

        Map<String, String> headers = getCommonHeaders();

        StringBuilder body = new StringBuilder();
        body.append(REQUEST_SID).append("=").append(account.toLowerCase());
        String bodyString = body.toString();

        NimHttpClient.getInstance().execute(url, headers, bodyString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable exception) {
                if (code != 200 || exception != null) {
                    LogUtil.e(TAG, "booking room failed, code=" + code + ", error=" + (exception != null ? exception.getMessage() : "null"));
                    if (callback != null) {
                        callback.onFailed(code, exception != null ? exception.getMessage() : null);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    int resCode = resObj.getIntValue(RESULT_KEY_CODE);
                    if (resCode == RESULT_CODE_SUCCESS) {
                        RoomInfo roomInfo = (RoomInfo) JsonObject2Model.parseJsonObjectToModule(resObj.getJSONObject(RESULT_KEY_DATA), RoomInfo.class);
                        callback.onSuccess(roomInfo);
                    } else {
                        String error = resObj.getString(RESULT_KEY_ERROR_MSG);
                        callback.onFailed(resCode, error);
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }

        });
    }

    /**
     * 学生查询课程信息
     * @param account 学生账号
     * @param callback 回调
     */
    public void studentQueryClass(String account, IHttpCallback<ClassInfo> callback) {
        String url = Servers.getServerAddress() + "/" + SERVICE_NAME + "/" + API_NAME_ROOM_QUERY;

        Map<String, String> headers = getCommonHeaders();

        StringBuilder body = new StringBuilder();
        body.append(REQUEST_SID).append("=").append(account.toLowerCase());
        String bodyString = body.toString();

        NimHttpClient.getInstance().execute(url, headers, bodyString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable exception) {
                if (code != 200 || exception != null) {
                    LogUtil.e(TAG, "student query class failed, code=" + code + ", error=" + (exception != null ? exception.getMessage() : "null"));
                    if (callback != null) {
                        callback.onFailed(code, exception != null ? exception.getMessage() : null);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    int resCode = resObj.getIntValue(RESULT_KEY_CODE);
                    if (resCode == RESULT_CODE_SUCCESS) {
                        ClassInfo classInfo = (ClassInfo) JsonObject2Model.parseJsonObjectToModule(resObj.getJSONObject(RESULT_KEY_DATA), ClassInfo.class);
                        callback.onSuccess(classInfo);
                    } else {
                        String error = resObj.getString(RESULT_KEY_ERROR_MSG);
                        callback.onFailed(resCode, error);
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }

        });
    }

    /**
     * 老师查询课程信息
     * @param account 老师账号
     * @param callback 回调
     */
    public void teacherQueryClass(String account, IHttpCallback<ClassInfo> callback) {
        String url = Servers.getServerAddress() + "/" + SERVICE_NAME + "/" + API_NAME_TEACHER_ROOM_QUERY;

        Map<String, String> headers = getCommonHeaders();

        StringBuilder body = new StringBuilder();
        body.append(REQUEST_SID).append("=").append(account.toLowerCase());
        String bodyString = body.toString();

        NimHttpClient.getInstance().execute(url, headers, bodyString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable exception) {
                if (code != 200 || exception != null) {
                    LogUtil.e(TAG, "teacher query class failed, code=" + code + ", error=" + (exception != null ? exception.getMessage() : "null"));
                    if (callback != null) {
                        callback.onFailed(code, exception != null ? exception.getMessage() : null);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    int resCode = resObj.getIntValue(RESULT_KEY_CODE);
                    if (resCode == RESULT_CODE_SUCCESS) {
                        ClassInfo classInfo = (ClassInfo) JsonObject2Model.parseJsonObjectToModule(resObj.getJSONObject(RESULT_KEY_DATA), ClassInfo.class);
                        callback.onSuccess(classInfo);
                    } else {
                        String error = resObj.getString(RESULT_KEY_ERROR_MSG);
                        callback.onFailed(resCode, error);
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }

        });
    }


    /**
     * 下课
     * @param account 老师账号
     * @param roomId    房间id
     * @param callback  回调
     */
    public void closeClass(String account, String roomId, IHttpCallback<Void> callback) {
        String url = Servers.getServerAddress() + "/" + SERVICE_NAME + "/" + API_NAME_TEACHER_ROOM_CLOSE;

        Map<String, String> headers = getCommonHeaders();

        StringBuilder body = new StringBuilder();
        body.append(REQUEST_SID).append("=").append(account.toLowerCase()).append("&")
                .append(REQUEST_ROOMID).append("=").append(roomId);
        String bodyString = body.toString();

        NimHttpClient.getInstance().execute(url, headers, bodyString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable exception) {
                if (code != 200 || exception != null) {
                    LogUtil.e(TAG, "close class failed, code=" + code + ", error=" + (exception != null ? exception.getMessage() : "null"));
                    if (callback != null) {
                        callback.onFailed(code, exception != null ? exception.getMessage() : null);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    int resCode = resObj.getIntValue(RESULT_KEY_CODE);
                    if (resCode == RESULT_CODE_SUCCESS) {
                        callback.onSuccess(null);
                    } else {
                        String error = resObj.getString(RESULT_KEY_ERROR_MSG);
                        callback.onFailed(resCode, error);
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }

        });
    }

    /**
     * 账号校验
     * @param account 账号
     * @param callback 回调
     */
    public void checkUser(String account, IHttpCallback<UserTypeInfo> callback) {
        String url = Servers.getServerAddress() + "/" + SERVICE_NAME + "/" + API_NAME_USER_CHECK;

        Map<String, String> headers = getCommonHeaders();

        StringBuilder body = new StringBuilder();
        body.append(REQUEST_USER_NAME).append("=").append(account.toLowerCase());
        String bodyString = body.toString();

        NimHttpClient.getInstance().execute(url, headers, bodyString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable exception) {
                if (code != 200 || exception != null) {
                    LogUtil.e(TAG, "check user failed, code=" + code + ", error=" + (exception != null ? exception.getMessage() : "null"));
                    if (callback != null) {
                        callback.onFailed(code, exception != null ? exception.getMessage() : null);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    int resCode = resObj.getIntValue(RESULT_KEY_CODE);
                    if (resCode == RESULT_CODE_SUCCESS) {
                        UserTypeInfo classInfo = (UserTypeInfo) JsonObject2Model.parseJsonObjectToModule(resObj.getJSONObject(RESULT_KEY_DATA), UserTypeInfo.class);
                        callback.onSuccess(classInfo);
                    } else {
                        String error = resObj.getString(RESULT_KEY_ERROR_MSG);
                        callback.onFailed(resCode, error);
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }

        });
    }

    /**
     * ******************************* api/header/params *******************************
     */

    private Map<String, String> getCommonHeaders() {
        Map<String, String> headers = new HashMap<>(3);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put(HEADER_KEY_APP_KEY, Servers.getAppKey());
        return headers;
    }

    /**
     * ******************************* single instance *******************************
     */

    private static DemoServerController instance;

    public static synchronized DemoServerController getInstance() {
        if (instance == null) {
            instance = new DemoServerController();
        }

        return instance;
    }
}
