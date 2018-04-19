package com.netease.nim.musiceducation.business.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.musiceducation.AuthPreferences;
import com.netease.nim.musiceducation.R;
import com.netease.nim.musiceducation.app.AppCache;
import com.netease.nim.musiceducation.business.AVChatStateObserverLight;
import com.netease.nim.musiceducation.business.activity.login.LogoutHelper;
import com.netease.nim.musiceducation.business.constant.UserType;
import com.netease.nim.musiceducation.business.helper.CommandHelper;
import com.netease.nim.musiceducation.common.LogUtil;
import com.netease.nim.musiceducation.common.permission.MPermission;
import com.netease.nim.musiceducation.common.permission.annotation.OnMPermissionDenied;
import com.netease.nim.musiceducation.common.permission.annotation.OnMPermissionGranted;
import com.netease.nim.musiceducation.common.permission.annotation.OnMPermissionNeverAskAgain;
import com.netease.nim.musiceducation.common.ui.UI;
import com.netease.nim.musiceducation.common.ui.dialog.DialogMaker;
import com.netease.nim.musiceducation.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.musiceducation.common.utils.ScreenUtil;
import com.netease.nim.musiceducation.doodle.ActionTypeEnum;
import com.netease.nim.musiceducation.doodle.DoodleView;
import com.netease.nim.musiceducation.doodle.OnlineStatusObserver;
import com.netease.nim.musiceducation.doodle.SupportActionType;
import com.netease.nim.musiceducation.doodle.Transaction;
import com.netease.nim.musiceducation.doodle.TransactionCenter;
import com.netease.nim.musiceducation.doodle.action.MyPath;
import com.netease.nim.musiceducation.protocol.CommandController;
import com.netease.nim.musiceducation.protocol.DemoServerController;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatChannelProfile;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.constant.AVChatUserRole;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoScalingType;
import com.netease.nimlib.sdk.avchat.model.AVChatCameraCapturer;
import com.netease.nimlib.sdk.avchat.model.AVChatChannelInfo;
import com.netease.nimlib.sdk.avchat.model.AVChatControlEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatNetworkStats;
import com.netease.nimlib.sdk.avchat.model.AVChatParameters;
import com.netease.nimlib.sdk.avchat.model.AVChatSurfaceViewRenderer;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoCapturerFactory;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.rts.RTSCallback;
import com.netease.nimlib.sdk.rts.RTSChannelStateObserver;
import com.netease.nimlib.sdk.rts.RTSManager2;
import com.netease.nimlib.sdk.rts.constant.RTSTunnelType;
import com.netease.nimlib.sdk.rts.model.RTSData;
import com.netease.nimlib.sdk.rts.model.RTSTunData;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand.NOTIFY_VIDEO_OFF;
import static com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand.NOTIFY_VIDEO_ON;

public class RoomActivity extends UI implements DoodleView.FlipListener {

    private static final String TAG = RoomActivity.class.getSimpleName();
    private static final int BASIC_PERMISSION_REQUEST_CODE = 1000;
    private static final int TOUCH_SLOP = 10;
    private static final String EXTRA_ROOM_ID = "roomId";
    private static final String EXTRA_TEACHER_ACCOUNT = "teacher_account";

    // 3种语音模式
    private static final int VOICE_MODE = 0;
    private static final int STUDENT_MODE = 1;
    private static final int TWO_SIDE_MODE = 2;

    private static final int TOTAL_PAGE_COUNT = 5;
    private final int IMAGES[] = new int[]{R.drawable.ic_music_book_1,
            R.drawable.ic_music_book_2, R.drawable.ic_music_book_3,
            R.drawable.ic_music_book_4, R.drawable.ic_music_book_5};

    // context
    private String roomId;
    private String teacherAccount;
    private int currentPageIndex = 1;
    private String userJoinAccount;
    private long chatId;

    // view
    private View toolBarView;
    private DoodleView doodleView;
    private ImageView doodleBgView;
    private AVChatSurfaceViewRenderer largeRender;
    private AVChatSurfaceViewRenderer smallRender;

    // toolbar
    private ViewGroup studentNetLayout;
    private ImageView studentNetImage;
    private TextView studentNetText;
    private ImageView localNetImage;
    private TextView localNetText;

    // 菜单栏按钮
    private View videoMenuBtn;
    private View paintMenuBtn;
    private View paintMenuImage;
    private TextView paintMenuText;
    private View voiceMenuBtn;
    private View voiceMenuImage;
    private View flipRightBtn;
    private View flipLeftBtn;
    private ImageView leftMenuImage;
    private TextView leftMenuText;
    private ImageView rightMenuImage;
    private TextView rightMenuText;

    // 各菜单布局
    private ViewGroup roomMenuLayout;
    private ViewGroup videoModeLayout;
    private ViewGroup paintMenuLayout;

    // 颜色按钮
    private View redBtn;
    private View yellowBtn;
    private View greenBtn;
    private View purpleBtn;
    private View blueBtn;

    private TextView pageText; // 页码

    // 视频模式按钮
    private View voiceModeBtn;
    private View studentModeBtn;
    private View twoSideModeBtn;
    private ImageView voiceModeImage;
    private ImageView studentModeImage;
    private ImageView twoSideModeImage;

    // 大小视频布局
    private LinearLayout smallVideoLayout;
    private LinearLayout largeVideoLayout;
    private ImageView smallVideoPreview;

    // state
    private boolean isFirstComing = true; // 主播是否首次进入房间
    private boolean canSwitch = true;

    // data
    private HashMap<View, Integer> colorChoosedMap = new HashMap<>();
    private HashMap<View, Integer> colorMap = new HashMap<>();
    private HashMap<View, Integer> colorPaintMap = new HashMap<>();
    private String smallAccount;
    private String largeAccount;
    private int currentVideoMode = VOICE_MODE;
    private SparseArray<String> renderMap = new SparseArray<>();
    // move
    private int lastX, lastY;
    private int inX, inY;
    private Rect paddingRect;
    private int deltaYOffset = 0;

    public static void startActivity(Context context, String teacherAccount, String roomId) {
        Intent intent = new Intent();
        intent.setClass(context, RoomActivity.class);
        intent.putExtra(EXTRA_ROOM_ID, roomId);
        intent.putExtra(EXTRA_TEACHER_ACCOUNT, teacherAccount);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_layout);

        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        teacherAccount = getIntent().getStringExtra(EXTRA_TEACHER_ACCOUNT);

        requestBasicPermission();

        registerAVChatObserver(true);
        registerRTSObserver(true);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (doodleView != null) {
            doodleView.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        registerObservers(false);

        finishAVChatRoom();
        finishRTSSession();

        if (doodleView != null) {
            doodleView.end();
        }

        super.onDestroy();
    }

    /**************************
     * 音视频权限控制
     ******************************/

    /**
     * 基本权限管理
     */
    private final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO
    };

    private void requestBasicPermission() {
        MPermission.printMPermissionResult(true, this, BASIC_PERMISSIONS);
        MPermission.with(RoomActivity.this)
                .setRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(BASIC_PERMISSIONS)
                .request();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {
        try {
            initViews();

            DialogMaker.showProgressDialog(this, "初始化...", false);
            // 初始化音视频房间
            initAVChatRoom();
            // 初始化白板通道
            initRTSSession();
            // 初始化白板doodle view
            initDoodleView();

            registerObservers(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS);
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    @OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
        try {
            Toast.makeText(this, "未全部授权，部分功能可能无法正常运行！", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }

        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS);
    }

    private void initData() {
        colorChoosedMap.put(redBtn, R.drawable.choose_red_circle_shape);
        colorChoosedMap.put(yellowBtn, R.drawable.choose_yellow_circle_shape);
        colorChoosedMap.put(greenBtn, R.drawable.choose_green_circle_shape);
        colorChoosedMap.put(blueBtn, R.drawable.choose_blue_circle_shape);
        colorChoosedMap.put(purpleBtn, R.drawable.choose_purple_circle_shape);

        colorMap.put(redBtn, R.drawable.red_circle_shape);
        colorMap.put(yellowBtn, R.drawable.yellow_circle_shape);
        colorMap.put(greenBtn, R.drawable.green_circle_shape);
        colorMap.put(blueBtn, R.drawable.blue_circle_shape);
        colorMap.put(purpleBtn, R.drawable.purple_circle_shape);

        colorPaintMap.put(redBtn, getResources().getColor(R.color.color_red_d1021c));
        colorPaintMap.put(yellowBtn, getResources().getColor(R.color.color_yellow_fddc01));
        colorPaintMap.put(greenBtn, getResources().getColor(R.color.color_green_7dd21f));
        colorPaintMap.put(blueBtn, getResources().getColor(R.color.color_blue_228bf7));
        colorPaintMap.put(purpleBtn, getResources().getColor(R.color.color_purple_9b0df5));
    }

    private void initViews() {
        initToolbar();

        toolBarView = findView(R.id.toolbar);
        doodleView = findView(R.id.doodle_view);
        doodleBgView = findView(R.id.doodle_view_bg_view);
        flipRightBtn = findView(R.id.right_menu);
        rightMenuImage = findView(R.id.right_menu_image);
        rightMenuText = findView(R.id.right_menu_text);
        flipRightBtn.setOnClickListener(onMenuClickListener);
        flipLeftBtn = findView(R.id.left_menu);
        leftMenuImage = findView(R.id.left_menu_image);
        leftMenuText = findView(R.id.left_menu_text);
        flipLeftBtn.setOnClickListener(onMenuClickListener);

        videoMenuBtn = findView(R.id.video_menu);
        paintMenuBtn = findView(R.id.paint_menu);
        voiceMenuBtn = findView(R.id.voice_menu);
        voiceMenuImage = findView(R.id.voice_menu_image);
        paintMenuImage = findView(R.id.paint_menu_image);
        paintMenuText = findView(R.id.paint_menu_text);

        roomMenuLayout = findView(R.id.room_menu_layout);
        roomMenuLayout.setVisibility(AuthPreferences.getKeyUserType() == UserType.STUDENT ? View.GONE : View.VISIBLE);
        videoModeLayout = findView(R.id.video_mode_layout);
        paintMenuLayout = findView(R.id.paint_menu_layout);

        pageText = findView(R.id.page_text);
        smallVideoLayout = findView(R.id.small_video_layout);
        smallVideoPreview = findView(R.id.small_video_preview);
        smallVideoLayout.setOnTouchListener(smallTouchListener);
        largeVideoLayout = findView(R.id.large_video_layout);

        videoMenuBtn.setOnClickListener(onMenuClickListener);
        paintMenuBtn.setOnClickListener(onMenuClickListener);
        voiceMenuBtn.setOnClickListener(onMenuClickListener);
        videoModeLayout.setOnClickListener(onMenuClickListener);
        paintMenuLayout.setOnClickListener(onMenuClickListener);

        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                flipToImage(1);
            }
        }, 200);

        initPaintViews();

        initModeViews();

        initData();
    }

    private void initToolbar() {
        studentNetLayout = findView(R.id.student_net_layout);
        studentNetImage = findView(R.id.student_net_image);
        studentNetText = findView(R.id.student_net_text);
        localNetImage = findView(R.id.local_net_image);
        localNetText = findView(R.id.local_net_text);

        TextView title = findView(R.id.title);
        TextView toolbarRightBtn = findView(R.id.toolbar_right_btn);
        if (AuthPreferences.getKeyUserType() == UserType.TEACHER) {
            toolbarRightBtn.setText(R.string.class_is_over);
            title.setVisibility(View.GONE);
        } else {
            toolbarRightBtn.setText(R.string.get_out);
            title.setVisibility(View.VISIBLE);
        }
        toolbarRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AuthPreferences.getKeyUserType() == UserType.TEACHER) {
                    closeClass();
                } else {
                    finish();
                }
            }
        });
    }

    private void initPaintViews() {
        View backBtn = findView(R.id.paint_back);
        View revokeBtn = findView(R.id.paint_revoke);
        View clearBtn = findView(R.id.paint_clear);

        redBtn = findView(R.id.red_color_image);
        yellowBtn = findView(R.id.yellow_color_image);
        greenBtn = findView(R.id.green_color_image);
        purpleBtn = findView(R.id.purple_color_image);
        blueBtn = findView(R.id.blue_color_image);

        backBtn.setOnClickListener(onPaintClickListener);
        redBtn.setOnClickListener(onPaintClickListener);
        yellowBtn.setOnClickListener(onPaintClickListener);
        greenBtn.setOnClickListener(onPaintClickListener);
        purpleBtn.setOnClickListener(onPaintClickListener);
        blueBtn.setOnClickListener(onPaintClickListener);
        revokeBtn.setOnClickListener(onPaintClickListener);
        clearBtn.setOnClickListener(onPaintClickListener);
    }

    private void initModeViews() {
        voiceModeBtn = findView(R.id.voice_mode);
        voiceModeImage = findView(R.id.voice_mode_image);
        studentModeBtn = findView(R.id.student_mode);
        studentModeImage = findView(R.id.student_mode_image);
        twoSideModeBtn = findView(R.id.two_side_mode);
        twoSideModeImage = findView(R.id.two_side_mode_image);

        voiceModeBtn.setOnClickListener(onModeClickListener);
        studentModeBtn.setOnClickListener(onModeClickListener);
        twoSideModeBtn.setOnClickListener(onModeClickListener);
    }

    private View.OnClickListener onMenuClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.left_menu:
                    flipLeft();
                    break;
                case R.id.right_menu:
                    flipRight();
                    break;
                case R.id.video_menu:
                    roomMenuLayout.setVisibility(View.GONE);
                    videoModeLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.voice_menu:
                    switchVoice();
                    break;
                case R.id.video_mode_layout:
                    roomMenuLayout.setVisibility(View.VISIBLE);
                    videoModeLayout.setVisibility(View.GONE);
                    break;
                case R.id.paint_menu:
                    roomMenuLayout.setVisibility(View.GONE);
                    paintMenuLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.paint_menu_layout:
                    roomMenuLayout.setVisibility(View.VISIBLE);
                    paintMenuLayout.setVisibility(View.GONE);
                    break;
            }
        }
    };

    private View.OnClickListener onPaintClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.paint_back) {
                roomMenuLayout.setVisibility(View.VISIBLE);
                paintMenuLayout.setVisibility(View.GONE);
            } else if (v.getId() == R.id.paint_clear) {
                doodleView.clear();
            } else if (v.getId() == R.id.paint_revoke) {
                doodleView.paintBack();
            } else {
                roomMenuLayout.setVisibility(View.VISIBLE);
                paintMenuLayout.setVisibility(View.GONE);
                for (Map.Entry<View, Integer> entry : colorMap.entrySet()) {
                    if (entry.getKey().getId() == v.getId()) {
                        // 选中的颜色
                        v.setBackgroundResource(colorChoosedMap.get(entry.getKey()));
                    } else {
                        // 其他颜色置成原始色]
                        entry.getKey().setBackgroundResource(colorMap.get(entry.getKey()));
                    }
                }
                doodleView.setPaintColor(colorPaintMap.get(v));
            }
        }
    };

    private View.OnClickListener onModeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            videoModeLayout.setVisibility(View.GONE);
            roomMenuLayout.setVisibility(View.VISIBLE);

            switch (v.getId()) {
                case R.id.voice_mode:
                    watchVoiceMode();
                    break;
                case R.id.student_mode:
                    watchStudentMode();
                    break;
                case R.id.two_side_mode:
                    watchTwoSideMode();
                    break;
            }
        }
    };

    // 下课
    private void closeClass() {
        CommandController.getInstance().sendCloseCommand(roomId);

        DemoServerController.getInstance().closeClass(AppCache.getAccount(), roomId, new DemoServerController.IHttpCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(RoomActivity.this, "下课啦", Toast.LENGTH_SHORT).show();
                LogoutHelper.clearRoomInfo();
                finish();
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                LogUtil.i("RoomActivity", "close room failed, code:" + code);
                Toast.makeText(RoomActivity.this, "close room failed, code:" + code, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerObservers(boolean register) {
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(customNotificationObserver, register);
        TransactionCenter.getInstance().registerOnlineStatusObserver(roomId, rtsOnlineStatusObserver);
    }

    private Observer<CustomNotification> customNotificationObserver = new Observer<CustomNotification>() {
        @Override
        public void onEvent(CustomNotification customNotification) {
            CommandHelper.showCommand(roomId, customNotification, new CommandHelper.CommandCallback() {
                @Override
                public void onCommandReceived() {
                    EasyAlertDialogHelper.showOneButtonDiolag(RoomActivity.this, null,
                            AppCache.getContext().getString(R.string.class_is_over_notify)
                            , AppCache.getContext().getString(R.string.iknow), false, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    finish();
                                }
                            });
                }
            });
        }
    };

    private OnlineStatusObserver rtsOnlineStatusObserver = new OnlineStatusObserver() {
        @Override
        public boolean onNetWorkChange(boolean isCreator) {
            // 断网重连。主播断网重连上来，给观众发自己的同步数据
            // 观众先清空本地
            if (isCreator) {
                doodleView.sendSyncPrepare();
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doodleView.sendSyncData(null);
                    }
                }, 50);
            } else {
                doodleView.clearAll();
            }
            return true;
        }
    };

    /**
     * ****************************** 音视频通道 ****************************
     */

    private void initAVChatRoom() {
        if (AuthPreferences.getKeyUserType() == UserType.TEACHER) {
            createAVChatRoom();
        } else {
            joinAVChatRoom();
        }
    }

    private void createAVChatRoom() {
        AVChatManager.getInstance().createRoom(roomId, "avchat room", new AVChatCallback<AVChatChannelInfo>() {
            @Override
            public void onSuccess(AVChatChannelInfo avChatChannelInfo) {
                LogUtil.i(TAG, "创建音视频房间成功");
                joinAVChatRoom();
            }

            @Override
            public void onFailed(int code) {
                if (code == 417) {
                    joinAVChatRoom();
                } else {
                    finish();
                }

                LogUtil.i(TAG, "创建音视频房间失败, code:" + code);
            }

            @Override
            public void onException(Throwable exception) {
                LogUtil.i(TAG, "创建音视频房间失败, exception:" + exception.toString());
                finish();
            }
        });
    }

    private void joinAVChatRoom() {
        //开启音视频引擎
        AVChatManager.getInstance().enableRtc();
        //设置场景, 高清音乐场景
        AVChatManager.getInstance().setChannelProfile(AVChatChannelProfile.CHANNEL_PROFILE_HIGH_QUALITY_MUSIC);
        //设置通话可选参数
        AVChatParameters parameters = new AVChatParameters();
        parameters.set(AVChatParameters.KEY_AUDIO_HIGH_QUALITY, true);
        AVChatManager.getInstance().setParameters(parameters);
        //视频通话设置
        AVChatManager.getInstance().enableVideo();
        //设置视频采集模块
        AVChatCameraCapturer videoCapturer = AVChatVideoCapturerFactory.createCameraCapturer();
        AVChatManager.getInstance().setupVideoCapturer(videoCapturer);

        AVChatManager.getInstance().setupLocalVideoRender(largeRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        AVChatManager.getInstance().startVideoPreview();

        AVChatManager.getInstance().setParameter(AVChatParameters.KEY_SESSION_MULTI_MODE_USER_ROLE, AVChatUserRole.NORMAL);
        AVChatManager.getInstance().joinRoom2(roomId, AVChatType.VIDEO, new AVChatCallback<AVChatData>() {
            @Override
            public void onSuccess(AVChatData avChatData) {
                LogUtil.i(TAG, "join avchat room success");
                chatId = avChatData.getChatId();
                // 设置音量信号监听, 通过AVChatStateObserver的onReportSpeaker回调音量大小
                AVChatParameters avChatParameters = new AVChatParameters();
                avChatParameters.setBoolean(AVChatParameters.KEY_AUDIO_REPORT_SPEAKER, true);
                AVChatManager.getInstance().setParameters(avChatParameters);
            }

            @Override
            public void onFailed(int code) {
                LogUtil.i(TAG, "join avchat room failed, code:" + code);
                finish();
            }

            @Override
            public void onException(Throwable exception) {
                LogUtil.i(TAG, "join avchat room exception:" + exception.toString());
                finish();
            }
        });
    }

    public void finish() {
        DialogMaker.dismissProgressDialog();
        super.finish();
    }

    /**
     * ********** 音视频监听 ************
     */

    private void registerAVChatObserver(boolean register) {
        AVChatManager.getInstance().observeAVChatState(avChatStateObserverLite, register);
        AVChatManager.getInstance().observeControlNotification(avChatControlNotification, register);
    }

    AVChatStateObserverLight avChatStateObserverLite = new AVChatStateObserverLight() {

        @Override
        public void onUserJoined(String account) {
            canSwitch = true;
            // 自己是老师，并且学生加入进来了
            if (AuthPreferences.getKeyUserType() == UserType.TEACHER && !AppCache.getAccount().equals(account)) {
                userJoinAccount = account;
                smallVideoPreview.setVisibility(View.GONE);
            }

            // 自己是学生，老师进来了
            if (AuthPreferences.getKeyUserType() == UserType.STUDENT) {
                teacherAccount = account;
            }

            if (currentVideoMode == STUDENT_MODE) {
                watchStudentMode();
            } else if (currentVideoMode == TWO_SIDE_MODE) {
                watchTwoSideMode();
            } else {
                watchVoiceMode();
            }
        }

        @Override
        public void onUserLeave(String account, int event) {
            canSwitch = false;
            // 学生掉线离开房间
            // 1. 把 userJoinAccount 数据清空
            // 2. 网络置位掉线
            if (!TextUtils.isEmpty(userJoinAccount) && userJoinAccount.equals(account)) {
                userJoinAccount = null;
                smallVideoPreview.setVisibility(currentVideoMode != VOICE_MODE ? View.VISIBLE : View.GONE);
            }

            if (AuthPreferences.getKeyUserType() == UserType.TEACHER) {
                studentNetImage.setVisibility(View.GONE);
                studentNetText.setText(R.string.net_broken);
            }

            for (int i = 0; i < renderMap.size(); i++) {
                if (!TextUtils.isEmpty(renderMap.valueAt(i)) && renderMap.valueAt(i).equals(account)) {
                    renderMap.put(i, null);
                }
            }

            if (!TextUtils.isEmpty(teacherAccount) && teacherAccount.equals(account)) {
                //老师掉线了
                teacherAccount = null;
            }
        }

        @Override
        public void onNetworkQuality(String account, int quality, AVChatNetworkStats stats) {
            updateNetworkQuality(account, quality);
        }

        @Override
        public void onCallEstablished() {
        }

        @Override
        public void onDisconnectServer(int code) {
            Toast.makeText(RoomActivity.this, "断网太久了，重来吧", Toast.LENGTH_SHORT).show();
            finish();
        }
    };

    private Observer<AVChatControlEvent> avChatControlNotification = new Observer<AVChatControlEvent>() {
        @Override
        public void onEvent(AVChatControlEvent avChatControlEvent) {
            if (avChatControlEvent.getControlCommand() == NOTIFY_VIDEO_ON) {
                // 收到老师双向视频通知
                watchTwoSideMode();
            } else if (avChatControlEvent.getControlCommand() == NOTIFY_VIDEO_OFF) {
                // 收到老师关闭双向视频通知
                watchVoiceMode();
            }
        }
    };

    private void updateNetworkQuality(String account, int quality) {
        // 如果只是学生，显示自己网络就可以
        if (account.equals(AppCache.getAccount())) {
            // 更新自己网络状态
            updateNet(localNetImage, localNetText, quality);
        }
        // 如果自己是老师
        if (AuthPreferences.getKeyUserType() == UserType.TEACHER) {
            studentNetLayout.setVisibility(View.VISIBLE);
            // 学生已经加入房间，则显示学生网络和自己网络
            if (!TextUtils.isEmpty(userJoinAccount) && userJoinAccount.equals(account)) {
                studentNetImage.setVisibility(View.VISIBLE);
                updateNet(studentNetImage, studentNetText, quality);
            } else {
                // 学生未加入房间, 显示学生离线
                studentNetImage.setVisibility(View.GONE);
                studentNetText.setText(R.string.net_broken);
            }
        }

    }

    private void updateNet(ImageView imageView, TextView textView, int quality) {
        switch (quality) {
            case 0:
                imageView.setBackgroundResource(R.drawable.ic_net_detect_wifi_enable_3);
                textView.setText(R.string.netting_good);
                break;
            case 1:
            case 2:
                imageView.setBackgroundResource(R.drawable.ic_net_detect_wifi_enable_2);
                textView.setText(R.string.netting_poor);
                break;
            case 3:
                imageView.setBackgroundResource(R.drawable.ic_net_detect_wifi_enable_1);
                textView.setText(R.string.netting_bad);
                break;
        }
    }

    private void finishAVChatRoom() {
        registerAVChatObserver(false);

        //关闭视频预览
        AVChatManager.getInstance().stopVideoPreview();
        // 如果是视频通话，关闭视频模块
        AVChatManager.getInstance().disableVideo();
        //离开房间
        AVChatManager.getInstance().leaveRoom2(roomId, new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.i(TAG, "leave avchat room success");
            }

            @Override
            public void onFailed(int code) {
                LogUtil.i(TAG, "leave avchat room failed, code:" + code);
            }

            @Override
            public void onException(Throwable exception) {
                LogUtil.i(TAG, "leave avchat room exception:" + exception.toString());
            }
        });
        //关闭音视频引擎
        AVChatManager.getInstance().disableRtc();
    }

    /**
     * ******************************** 白板UI ****************************
     */

    private void initDoodleView() {
        LogUtil.i(TAG, "init doodle success");
        // add support ActionType
        SupportActionType.getInstance().addSupportActionType(ActionTypeEnum.Path.getValue(), MyPath.class);

        doodleView.init(roomId, null, AuthPreferences.getKeyUserType() == UserType.TEACHER ? DoodleView.Mode.BOTH : DoodleView.Mode.PLAYBACK,
                getResources().getColor(R.color.color_red_d1021c), RoomActivity.this, this);

        doodleView.setPaintSize(3);
        doodleView.setPaintType(ActionTypeEnum.Path.getValue());
    }

    /**
     * ******************************* 白板通道 ****************************
     */

    private void initRTSSession() {
        if (AuthPreferences.getKeyUserType() == UserType.TEACHER) {
            createRTSSession();
        } else {
            joinRTSSession();
        }
    }

    private void createRTSSession() {
        RTSManager2.getInstance().createSession(roomId, "music", new RTSCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.d(TAG, "创建白板房间成功");
                joinRTSSession();
            }

            @Override
            public void onFailed(int i) {
                if (i == 417) {
                    joinRTSSession();
                } else {
                    finish();
                }
                LogUtil.d(TAG, "create rts session failed, code:" + i);
            }

            @Override
            public void onException(Throwable throwable) {
                finish();
            }
        });
    }

    private void joinRTSSession() {
        RTSManager2.getInstance().joinSession(roomId, false, new RTSCallback<RTSData>() {
            @Override
            public void onSuccess(RTSData rtsData) {
                LogUtil.i(TAG, "join session success");
            }

            @Override
            public void onFailed(int code) {
                Toast.makeText(RoomActivity.this, "请等老师先进入教室哦", Toast.LENGTH_SHORT).show();
                LogUtil.i(TAG, "join session failed, code:" + code);
                finish();
            }

            @Override
            public void onException(Throwable exception) {
                LogUtil.i(TAG, "join session, exception:" + exception.toString());
                finish();
            }
        });
    }

    /**
     * ************** 白板监听 ****************
     */

    private void registerRTSObserver(boolean register) {
        RTSManager2.getInstance().observeChannelState(roomId, channelStateObserver, register);
        RTSManager2.getInstance().observeReceiveData(roomId, receiveDataObserver, register);
    }

    /**
     * 监听当前会话的状态
     */
    private RTSChannelStateObserver channelStateObserver = new RTSChannelStateObserver() {

        @Override
        public void onConnectResult(String localSessionId, RTSTunnelType tunType, long channelId, int code, String recordFile) {
            LogUtil.i(TAG, "onConnectResult, tunType=" + tunType.toString() +
                    ", channelId=" + channelId +
                    ", code=" + code);
            if (code != 200) {
                Toast.makeText(RoomActivity.this, "rts连接失败, code:" + code, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            DialogMaker.dismissProgressDialog();

            // 主播进入，
            // 1、第一次进入，或者异常退出发送白板同步准备指令，让观众清空。
            // 2、网络变化，发送主播的同步数据。
            List<Transaction> cache = new ArrayList<>(1);
            if (AuthPreferences.getKeyUserType() == UserType.TEACHER) {
                if (isFirstComing) {
                    isFirstComing = false;
                    doodleView.sendSyncPrepare();
                    cache.add(new Transaction().makeFlipTransaction("", 1, 5, 1));
                    TransactionCenter.getInstance().sendToRemote(roomId, null, cache);
                } else {
                    TransactionCenter.getInstance().onNetWorkChange(roomId, true);
                }
            } else {
                // 非主播进入房间，发送同步请求，请求主播向他同步之前的白板笔记
                Toast.makeText(RoomActivity.this, "send sync request", Toast.LENGTH_SHORT).show();
                TransactionCenter.getInstance().onNetWorkChange(roomId, false);
                cache.add(new Transaction().makeSyncRequestTransaction());
                TransactionCenter.getInstance().sendToRemote(roomId, teacherAccount, cache);
            }
        }

        @Override
        public void onChannelEstablished(String sessionId, RTSTunnelType tunType) {
        }

        @Override
        public void onUserJoin(String sessionId, RTSTunnelType tunType, String account) {
            LogUtil.i(TAG, "On User Join, account:" + account);
        }

        @Override
        public void onUserLeave(String sessionId, RTSTunnelType tunType, String account, int event) {
            LogUtil.i(TAG, "On User Leave, account:" + account);
        }

        @Override
        public void onDisconnectServer(String sessionId, RTSTunnelType tunType) {
            LogUtil.i(TAG, "onDisconnectServer, tunType=" + tunType.toString());
            if (tunType == RTSTunnelType.DATA) {
                Toast.makeText(RoomActivity.this, "断网太久了，重来吧", Toast.LENGTH_SHORT).show();
                finish();
            } else if (tunType == RTSTunnelType.AUDIO) {
            }
        }

        @Override
        public void onError(String sessionId, RTSTunnelType tunType, int code) {
            Toast.makeText(RoomActivity.this, "onError, tunType=" + tunType.toString() + ", error=" + code,
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onNetworkStatusChange(String sessionId, RTSTunnelType tunType, int value) {
            // 网络信号强弱
            LogUtil.i(TAG, "network status:" + value);
        }
    };

    /**
     * 监听收到对方发送的通道数据
     */
    private Observer<RTSTunData> receiveDataObserver = new Observer<RTSTunData>() {
        @Override
        public void onEvent(RTSTunData rtsTunData) {
            LogUtil.i(TAG, "receive data");
            String data = "[parse bytes error]";
            try {
                data = new String(rtsTunData.getData(), 0, rtsTunData.getLength(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            TransactionCenter.getInstance().onReceive(roomId, rtsTunData.getAccount(), data);
        }
    };

    private void finishRTSSession() {
        registerRTSObserver(false);

        RTSManager2.getInstance().leaveSession(roomId, new RTSCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.i(TAG, "leave rts session success");
            }

            @Override
            public void onFailed(int code) {
                LogUtil.i(TAG, "leave rts session failed, code:" + code);
            }

            @Override
            public void onException(Throwable exception) {
                LogUtil.i(TAG, "leave rts session exception:" + exception.toString());
            }
        });
    }

    /**
     * ******************************* 翻页 *********************************
     */

    private void flipRight() {
        if (doodleView != null) {
            if (currentPageIndex == TOTAL_PAGE_COUNT) {
                Toast.makeText(this, "已经到最后一页了", Toast.LENGTH_SHORT).show();
            } else {
                doodleView.sendFlipData("0", ++currentPageIndex, TOTAL_PAGE_COUNT, 1);
                flipToImage(currentPageIndex);

            }
        }
    }

    private void flipLeft() {
        if (doodleView != null) {
            if (currentPageIndex <= 1) {
                currentPageIndex = 1;
                Toast.makeText(this, "已经到首页了", Toast.LENGTH_SHORT).show();
            } else {
                doodleView.sendFlipData("0", --currentPageIndex, TOTAL_PAGE_COUNT, 1);
                flipToImage(currentPageIndex);
            }
        }
    }

    @Override
    public void onFlipPage(Transaction transaction) {
        int index = transaction.getCurrentPageNum();

        flipToImage(index);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updatePageText(index, transaction.getPageCount());
            }
        });
    }

    private void flipToImage(int index) {
        if (index < 1 || index > TOTAL_PAGE_COUNT) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                doodleBgView.setBackgroundResource(IMAGES[index - 1]);
                updatePageText(index, TOTAL_PAGE_COUNT);
            }
        });
    }

    private void updatePageText(int currentPageIndex, int totalPageCount) {
        pageText.setText(String.format("%s 页", currentPageIndex + "/" + totalPageCount));
    }

    /**
     * ************************ onMenuClickListener *********************
     */

    private void switchVoice() {
        if (AVChatManager.getInstance().isLocalAudioMuted()) {
            // 打开声音
            voiceMenuImage.setBackgroundResource(R.drawable.ic_voice_on);
            AVChatManager.getInstance().muteLocalAudio(false);
        } else {
            voiceMenuImage.setBackgroundResource(R.drawable.ic_voice_off);
            AVChatManager.getInstance().muteLocalAudio(true);
        }
    }

    /**
     * ************************ onModeClickListener *********************
     */

    // 语音模式
    private void watchVoiceMode() {
        updateModeImage(VOICE_MODE);

        // 老师需要发通知给学生
        if (AuthPreferences.getKeyUserType() == UserType.TEACHER) {
            sendWatchTwoSideMode(NOTIFY_VIDEO_OFF);
            enableMenu(true);
        }

        doodleBgView.setVisibility(View.VISIBLE);
        doodleView.setVisibility(View.VISIBLE);
        doodleView.onResume();
        smallVideoLayout.setVisibility(View.GONE);
        largeVideoLayout.setVisibility(View.GONE);
    }

    // 单向视频
    private void watchStudentMode() {
        updateModeImage(STUDENT_MODE);

        if (AuthPreferences.getKeyUserType() == UserType.TEACHER) {
            sendWatchTwoSideMode(NOTIFY_VIDEO_OFF);
            enableMenu(true);
        }

        doodleView.setVisibility(View.VISIBLE);
        doodleView.onResume();
        doodleBgView.setVisibility(View.VISIBLE);
        largeVideoLayout.setVisibility(View.GONE);

        if (smallRender == null) {
            smallRender = new AVChatSurfaceViewRenderer(this);
        }

        // 设置的时候，需要清空画布上绑定的用户
        // 当remote是老师的时候，才需要解绑
        resetRenderMap();

        // 大窗口是学生，小窗口是老师。清空remote
        AVChatManager.getInstance().setupRemoteVideoRender(userJoinAccount, smallRender,
                false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        addIntoVideoLayout(smallVideoLayout, smallRender, true);
        smallAccount = userJoinAccount;
    }

    // 双向视频
    private void watchTwoSideMode() {
        // 0. 发送双向视频数据给学生
        // 1. 隐藏画板 2. 显示本地画面 3. 显示远端画面
        updateModeImage(TWO_SIDE_MODE);

        if (AuthPreferences.getKeyUserType() == UserType.TEACHER) {
            sendWatchTwoSideMode(NOTIFY_VIDEO_ON);
            enableMenu(false);
        }

        doodleView.setVisibility(View.GONE);
        doodleBgView.setVisibility(View.GONE);

        if (largeRender == null) {
            largeRender = new AVChatSurfaceViewRenderer(this);
        }

        resetRenderMap();

        if (AuthPreferences.getKeyUserType() == UserType.TEACHER) {
            // 自己是老师，大窗口是学生
            AVChatManager.getInstance().setupRemoteVideoRender(userJoinAccount, largeRender,
                    false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
            largeAccount = userJoinAccount;
        } else {
            if (!TextUtils.isEmpty(teacherAccount)) {
                // 自己是学生，大窗口就是老师
                AVChatManager.getInstance().setupRemoteVideoRender(teacherAccount, largeRender,
                        false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
                largeAccount = teacherAccount;
            } else {
                largeAccount = null;
            }
        }
        renderMap.put(0, largeAccount);

        addIntoVideoLayout(largeVideoLayout, largeRender, false);

        if (smallRender == null) {
            smallRender = new AVChatSurfaceViewRenderer(this);
        }

        AVChatManager.getInstance().setupLocalVideoRender(smallRender, false,
                AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        smallAccount = AppCache.getAccount();
        renderMap.put(1, smallAccount);

        addIntoVideoLayout(smallVideoLayout, smallRender, true);
        smallVideoLayout.bringToFront();
    }

    private void resetRenderMap() {
        // 先清空画布上绑定的用户
        // 0 代表远端, 1 代表近端
        if (!TextUtils.isEmpty(renderMap.get(0))) {
            // 绑定远端的画布给取消了先
            AVChatManager.getInstance().setupRemoteVideoRender(renderMap.get(0), null,
                    false, 0);
            renderMap.put(0, null);
        }

        if (!TextUtils.isEmpty(renderMap.get(1))) {
            AVChatManager.getInstance().setupLocalVideoRender(null, false, 0);
            renderMap.put(1, null);
        }
    }

    private void updateModeImage(int chooseIndex) {
        currentVideoMode = chooseIndex;

        if (AuthPreferences.getKeyUserType() == UserType.STUDENT) {
            smallVideoPreview.setVisibility(View.GONE);
        } else {
            // 单向语音模式,并且学生没有加入频道，才显示默认头像
            if (chooseIndex == STUDENT_MODE && TextUtils.isEmpty(userJoinAccount)) {
                smallVideoPreview.setVisibility(View.VISIBLE);
            } else {
                smallVideoPreview.setVisibility(View.GONE);
            }
        }

        voiceModeImage.setBackgroundResource(chooseIndex == VOICE_MODE
                ? R.drawable.ic_voice_mode_choosed : R.drawable.ic_voice_mode);
        studentModeImage.setBackgroundResource(chooseIndex == STUDENT_MODE
                ? R.drawable.ic_view_eye_choosed : R.drawable.ic_view_eye);
        twoSideModeImage.setBackgroundResource(chooseIndex == TWO_SIDE_MODE
                ? R.drawable.ic_twoside_video_choosed : R.drawable.ic_twoside_video);
    }

    private void sendWatchTwoSideMode(byte command) {
        AVChatManager.getInstance().sendControlCommand(chatId, command, new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.i(TAG, "send video on command:" + command);
            }

            @Override
            public void onFailed(int code) {
                LogUtil.i(TAG, "send video on command" + command + ", failed code:" + code);
            }

            @Override
            public void onException(Throwable exception) {
            }
        });
    }

    private void addIntoVideoLayout(ViewGroup videoLayout, SurfaceView surfaceView, boolean zOrder) {
        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }
        videoLayout.addView(surfaceView);
        surfaceView.setZOrderMediaOverlay(zOrder);
        videoLayout.setVisibility(View.VISIBLE);
    }

    private void enableMenu(boolean enable) {
        paintMenuBtn.setEnabled(enable);
        paintMenuImage.setBackgroundResource(enable ? R.drawable.ic_pencil_menu : R.drawable.ic_pencil_off);
        paintMenuText.setTextColor(enable ? getResources().getColor(R.color.color_gray_cccccc)
                : getResources().getColor(R.color.color_gray_717273));
        flipLeftBtn.setEnabled(enable);
        leftMenuImage.setBackgroundResource(enable ? R.drawable.ic_left_menu : R.drawable.ic_left_menu_disabled);
        leftMenuText.setTextColor(enable ? getResources().getColor(R.color.color_gray_cccccc)
                : getResources().getColor(R.color.color_gray_717273));
        flipRightBtn.setEnabled(enable);
        rightMenuImage.setBackgroundResource(enable ? R.drawable.ic_right_menu : R.drawable.ic_right_menu_disabled);
        rightMenuText.setTextColor(enable ? getResources().getColor(R.color.color_gray_cccccc)
                : getResources().getColor(R.color.color_gray_717273));
    }

    /**
     * ************************* 视频小窗口，移动和切换 *********************
     */
    private View.OnTouchListener smallTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = x;
                    lastY = y;
                    int[] p = new int[2];
                    smallVideoLayout.getLocationOnScreen(p);
                    inX = x - p[0];
                    inY = y - p[1];

                    break;
                case MotionEvent.ACTION_MOVE:
                    final int diff = Math.max(Math.abs(lastX - x), Math.abs(lastY - y));
                    if (diff < TOUCH_SLOP)
                        break;

                    if (paddingRect == null) {
                        deltaYOffset = ScreenUtil.getStatusBarHeight(RoomActivity.this) + RoomActivity.this.toolBarView.getHeight();
                        paddingRect = new Rect(ScreenUtil.dip2px(10), deltaYOffset, ScreenUtil.dip2px(10),
                                0);
                    }

                    int destX, destY;
                    if (x - inX <= paddingRect.left) {
                        destX = paddingRect.left;
                    } else if (x - inX + v.getWidth() >= ScreenUtil.screenWidth - paddingRect.right) {
                        destX = ScreenUtil.screenWidth - v.getWidth() - paddingRect.right;
                    } else {
                        destX = x - inX;
                    }

                    if (y - inY <= paddingRect.top) {
                        destY = paddingRect.top;
                    } else if (y - inY + v.getHeight() >= ScreenUtil.screenHeight - paddingRect.bottom) {
                        destY = ScreenUtil.screenHeight - v.getHeight() - paddingRect.bottom;
                    } else {
                        destY = y - inY;
                    }

                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
                    params.gravity = Gravity.NO_GRAVITY;
                    params.leftMargin = destX;
                    params.topMargin = destY - deltaYOffset; // 这里要减去ToolBar和StatusBar的高度
                    v.setLayoutParams(params);

                    break;
                case MotionEvent.ACTION_UP:
                    if (Math.max(Math.abs(lastX - x), Math.abs(lastY - y)) <= 5) {
                        if (largeAccount == null || smallAccount == null || !canSwitch) {
                            return true;
                        }
                        String temp;
                        switchRender(smallAccount, largeAccount);
                        temp = largeAccount;
                        largeAccount = smallAccount;
                        smallAccount = temp;
                    }
                    break;
            }

            return true;
        }
    };

    // 大小图像显示切换
    private void switchRender(String user1, String user2) {
        //先取消用户的画布
        resetUserRender(user1, user2);
        //交换画布
        //如果存在多个用户,建议用Map维护account,render关系.
        //目前只有两个用户,并且认为这两个account肯定是对的
        AVChatSurfaceViewRenderer render1;
        AVChatSurfaceViewRenderer render2;
        if (user1.equals(smallAccount)) {
            render1 = largeRender;
            render2 = smallRender;
        } else {
            render1 = smallRender;
            render2 = largeRender;
        }

        //重新设置上画布
        if (user1.equals(AppCache.getAccount())) {
            AVChatManager.getInstance().setupLocalVideoRender(render1, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
            renderMap.put(1, user1);
        } else {
            AVChatManager.getInstance().setupRemoteVideoRender(user1, render1, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
            renderMap.put(0, user1);
        }
        if (user2.equals(AppCache.getAccount())) {
            AVChatManager.getInstance().setupLocalVideoRender(render2, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
            renderMap.put(1, user2);
        } else {
            AVChatManager.getInstance().setupRemoteVideoRender(user2, render2, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
            renderMap.put(0, user2);
        }
    }

    private void resetUserRender(String user1, String user2) {
        renderMap.put(0, null);
        renderMap.put(1, null);
        if (AppCache.getAccount().equals(user1)) {
            AVChatManager.getInstance().setupLocalVideoRender(null, false, 0);
        } else {
            AVChatManager.getInstance().setupRemoteVideoRender(user1, null, false, 0);
        }
        if (AppCache.getAccount().equals(user2)) {
            AVChatManager.getInstance().setupLocalVideoRender(null, false, 0);
        } else {
            AVChatManager.getInstance().setupRemoteVideoRender(user2, null, false, 0);
        }
    }
}
