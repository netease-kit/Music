package com.netease.nim.musiceducation.business.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.musiceducation.AuthPreferences;
import com.netease.nim.musiceducation.R;
import com.netease.nim.musiceducation.app.AppCache;
import com.netease.nim.musiceducation.business.activity.login.LoginActivity;
import com.netease.nim.musiceducation.business.activity.login.LogoutHelper;
import com.netease.nim.musiceducation.business.constant.NetStateType;
import com.netease.nim.musiceducation.business.constant.UserType;
import com.netease.nim.musiceducation.business.helper.CommandHelper;
import com.netease.nim.musiceducation.business.helper.NetDetectHelpter;
import com.netease.nim.musiceducation.common.LogUtil;
import com.netease.nim.musiceducation.common.net.NetworkUtil;
import com.netease.nim.musiceducation.common.ui.ToolBarOptions;
import com.netease.nim.musiceducation.common.ui.UI;
import com.netease.nim.musiceducation.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.musiceducation.protocol.DemoServerController;
import com.netease.nim.musiceducation.protocol.model.ClassInfo;
import com.netease.nim.musiceducation.protocol.model.RoomInfo;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.model.CustomNotification;

public class MainActivity extends UI implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ViewGroup studentTipLayout;
    private ViewGroup bookedLayout;
    private Button bookClassBtn;
    private TextView howToLogin;
    private ImageView bookLogoImage;
    private TextView bookSuccessText;
    private TextView teacherTipText;
    private ImageView netDetectImage;
    private TextView netDetectText;

    // data
    private RoomInfo roomInfo;
    private boolean isFirstBlood = true;

    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, Intent extras) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        setTitle();

        findViews();
        registerObservers(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        startNetDetect();
        queryInfo();
    }

    @Override
    protected void onDestroy() {
        registerObservers(false);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_menu_btn:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            case R.id.logout_menu_btn:
                logout();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setTitle() {
        ToolBarOptions options = new ToolBarOptions();
        if (AuthPreferences.getKeyUserType() == UserType.TEACHER) {
            options.titleString = getString(R.string.app_name_teacher);
        } else {
            options.titleString = getString(R.string.app_name_student);
        }

        options.isNeedNavigate = false;
        setToolBar(R.id.toolbar, options);
    }

    private void findViews() {
        studentTipLayout = findView(R.id.student_tip_layout);
        bookedLayout = findView(R.id.booked_layout);
        howToLogin = findView(R.id.how_to_login);
        bookClassBtn = findView(R.id.book_class_btn);
        bookLogoImage = findView(R.id.book_logo);
        bookSuccessText = findView(R.id.book_success);
        teacherTipText = findView(R.id.teacher_tip);
        netDetectImage = findView(R.id.net_detect_image);
        netDetectText = findView(R.id.net_detect_text);

        howToLogin.setOnClickListener(this);
        bookClassBtn.setOnClickListener(this);
    }

    private void registerObservers(boolean register) {
        NetDetectHelpter.getInstance().observeNetDetectStatus(netDetectObserver, register);
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(customNotificationObserver, register);
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, register);
    }

    NetDetectHelpter.NetDetectObserver netDetectObserver = new NetDetectHelpter.NetDetectObserver() {
        @Override
        public void onNetDetectResult(NetDetectHelpter.NetDetectResult result) {
            updateNetDetectComplete(result);
        }
    };

    private Observer<CustomNotification> customNotificationObserver = new Observer<CustomNotification>() {
        @Override
        public void onEvent(CustomNotification customNotification) {
            if (AuthPreferences.getRoomInfo() == null) {
                return;
            }
            CommandHelper.showCommand(AuthPreferences.getRoomInfo().getRoomId(), customNotification, new CommandHelper.CommandCallback() {
                @Override
                public void onCommandReceived() {
                    queryInfo();
                }
            });
        }
    };

    /**
     * 用户状态变化
     */
    Observer<StatusCode> userStatusObserver = new Observer<StatusCode>() {

        @Override
        public void onEvent(StatusCode code) {
            if (code.wontAutoLogin()) {
                doLogout();
                LoginActivity.start(MainActivity.this, true);
            }
        }
    };

    private void startNetDetect() {
        netDetectText.setText("网络状况：检测中...");
        NetDetectHelpter.getInstance().startNetDetect();
    }

    private void updateNetDetectComplete(NetDetectHelpter.NetDetectResult result) {
        if (result.getCode() == 200) {
            NetStateType netGrade = calculateGrade(result);
            switch (netGrade) {
                case SMOOTH:
                    netDetectText.setText(R.string.net_detect_very_good);
                    netDetectImage.setImageResource(R.drawable.ic_net_detect_wifi_enable_3);
                    break;
                case COMMON:
                case POOR:
                    netDetectText.setText(R.string.net_detect_poor);
                    netDetectImage.setImageResource(R.drawable.ic_net_detect_wifi_enable_2);
                    break;
                case BAD:
                    netDetectText.setText(R.string.net_detect_bad);
                    netDetectImage.setImageResource(R.drawable.ic_net_detect_wifi_enable_1);
                    break;
            }
        }
    }

    private NetStateType calculateGrade(NetDetectHelpter.NetDetectResult result) {
        double netStateIndex = ((double) result.getLoss() / 20) * 0.5
                + ((double) result.getRttAvg() / 1200) * 0.25 + ((double) result.getMdev() / 150) * 0.25;
        if (netStateIndex < 0.2625) {
            return NetStateType.SMOOTH;
        } else if (netStateIndex < 0.55) {
            return NetStateType.COMMON;
        } else if (netStateIndex < 1) {
            return NetStateType.POOR;
        } else {
            return NetStateType.BAD;
        }
    }

    private void queryInfo() {
        if (AuthPreferences.getKeyUserType() == UserType.TEACHER) {
            queryTeacherInfo();
        } else {
            queryClassInfo();
        }
    }

    // 学生查询预约课程
    private void queryClassInfo() {
        DemoServerController.getInstance().studentQueryClass(AppCache.getAccount(), new DemoServerController.IHttpCallback<ClassInfo>() {
            @Override
            public void onSuccess(ClassInfo classInfo) {
                if (classInfo != null && classInfo.getList() != null && classInfo.getList().size() > 0) {
                    showClassInfo(classInfo.getList().get(0));
                } else {
                    showClassInfo(null);
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                Toast.makeText(MainActivity.this, "student query class failed, code:" + code, Toast.LENGTH_SHORT).show();
                LogUtil.i(TAG, "student query class failed, code:" + code);
                bookClassBtn.setText("student query class failed");
            }
        });
    }

    private void bookClass() {
        DemoServerController.getInstance().bookingRoom(AppCache.getAccount(), new DemoServerController.IHttpCallback<RoomInfo>() {
            @Override
            public void onSuccess(RoomInfo roomInfo) {
                if (roomInfo != null) {
                    showClassInfo(roomInfo);
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                LogUtil.i(TAG, "book class failed, code:" + code + ", errorMsg:" + errorMsg.toString());
                bookClassBtn.setText("student book class failed");
            }
        });
    }

    // 老师查询预约课程
    private void queryTeacherInfo() {
        DemoServerController.getInstance().teacherQueryClass(AppCache.getAccount(), new DemoServerController.IHttpCallback<ClassInfo>() {
            @Override
            public void onSuccess(ClassInfo classInfo) {
                if (classInfo != null && classInfo.getList() != null && classInfo.getList().size() > 0) {
                    showClassInfo(classInfo.getList().get(0));
                } else {
                    showClassInfo(null);
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                Toast.makeText(MainActivity.this, "student query class failed, code:" + code, Toast.LENGTH_SHORT).show();
                LogUtil.i(TAG, "student query class failed, code:" + code);
                bookClassBtn.setText("teacher query class failed");
            }
        });
    }

    private void showClassInfo(RoomInfo roomInfo) {
        this.roomInfo = roomInfo;
        AuthPreferences.saveRoomInfo(roomInfo);
        showMainUI();
    }

    private void showMainUI() {
        resetAll();
        if (AuthPreferences.getKeyUserType() == UserType.TEACHER) {
            // 老师
            if (roomInfo != null) {
                // 有学生申请课程
                bookLogoImage.setVisibility(View.VISIBLE);
                bookLogoImage.setBackgroundResource(R.drawable.ic_book_logo);
                teacherTipText.setVisibility(View.VISIBLE);
                teacherTipText.setText(getString(R.string.book_name_tip, roomInfo.getStudentName()));
                bookClassBtn.setVisibility(View.VISIBLE);
                bookClassBtn.setText(R.string.enter_room);
            } else {
                // 没有学生申请课程
                bookLogoImage.setVisibility(View.VISIBLE);
                bookLogoImage.setBackgroundResource(R.drawable.ic_no_book_logo);
                teacherTipText.setVisibility(View.VISIBLE);
                teacherTipText.setText(R.string.no_book_tip);
                bookClassBtn.setVisibility(View.GONE);
            }
        } else {
            // 学生
            if (roomInfo != null) {
                // 学生已经申请了课程
                bookedLayout.setVisibility(View.VISIBLE);
                bookSuccessText.setVisibility(View.VISIBLE);
                bookSuccessText.setText(getString(R.string.book_class_success, roomInfo.getTeacherName()));
                bookClassBtn.setText(R.string.enter_room);
            } else {
                // 学生还没申请课程
                studentTipLayout.setVisibility(View.VISIBLE);
                bookClassBtn.setText(R.string.book_class);
            }
        }
    }

    private void resetAll() {
        bookLogoImage.setVisibility(View.GONE);
        teacherTipText.setVisibility(View.GONE);
        bookClassBtn.setVisibility(View.VISIBLE);
        bookClassBtn.setText("loading");
        bookedLayout.setVisibility(View.GONE);
        bookSuccessText.setVisibility(View.GONE);
        studentTipLayout.setVisibility(View.GONE);
    }

    private void showLoginTip() {
        EasyAlertDialogHelper.showOneButtonDiolag(this, getString(R.string.please_login_tip),
                getString(R.string.teacher_info_tip, roomInfo.getTeacherAccount(), roomInfo.getTeacherPassword()),
                getString(R.string.iknow), false, null);
    }

    private void enterRoom() {
        if (!NetworkUtil.isNetAvailable(this)) {
            Toast.makeText(this, R.string.net_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        RoomActivity.startActivity(MainActivity.this, roomInfo.getTeacherAccount(), roomInfo.getRoomId());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.book_class_btn:
                if (bookClassBtn.getText().equals(getString(R.string.book_class))) {
                    bookClass();
                } else if (bookClassBtn.getText().equals(getString(R.string.enter_room))) {
                    enterRoom();
                }
                break;
            case R.id.how_to_login:
                showLoginTip();
                break;
        }
    }

    private void logout() {
        EasyAlertDialogHelper.createOkCancelDiolag(this, null,
                "确认要注销吗", getString(R.string.logout), getString(R.string.cancel),
                true, new EasyAlertDialogHelper.OnDialogActionListener() {
                    @Override
                    public void doCancelAction() {

                    }

                    @Override
                    public void doOkAction() {
                        doLogout();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }
                }).show();
    }

    private void doLogout() {
        LogoutHelper.logout();
        finish();
    }
}
