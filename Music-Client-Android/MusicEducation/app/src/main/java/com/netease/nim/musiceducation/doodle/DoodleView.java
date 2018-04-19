package com.netease.nim.musiceducation.doodle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.netease.nim.musiceducation.app.AppCache;
import com.netease.nim.musiceducation.common.LogUtil;
import com.netease.nim.musiceducation.doodle.action.Action;
import com.netease.nim.musiceducation.doodle.action.MyFillCircle;
import com.netease.nim.musiceducation.doodle.action.MyPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 涂鸦板控件（基类）
 * <p/>
 * Created by huangjun on 2015/6/24.
 */
public class DoodleView extends SurfaceView implements SurfaceHolder.Callback, TransactionObserver {

    public interface FlipListener {
        void onFlipPage(Transaction transaction);
    }

    public enum Mode {
        PAINT,
        PLAYBACK,
        BOTH
    }

    private final String TAG = "DoodleView";

    private SurfaceHolder surfaceHolder;

    private DoodleChannel paintChannel; // 绘图通道，自己本人使用

    private DoodleChannel laserChannel; // 激光笔绘图通道

    private Map<String, DoodleChannel> playbackChannelMap = new HashMap<>(); // 其余的人，一个人对应一个通道

    private TransactionManager transactionManager; // 数据发送管理器

    private int paintColor = Color.BLACK; // 默认画笔颜色

    private float xZoom = 4.0f; // 收发数据时缩放倍数（归一化）
    private float yZoom = 5.0f;
    private float lastX = 0.0f;
    private float lastY = 0.0f;

    private boolean enableView = true;
    private boolean isSurfaceViewCreated = false;
    private boolean isSyncAlready = false;

    private String sessionId;
    private volatile int pageIndex = 1; // 默认在首页

    private int paintSize;
    private int paintType;

    // <account, transactions>
    private Map<String, SparseArray<List<Transaction>>> userDataMap = new HashMap<>();

    private FlipListener flipListener;

    public DoodleView(Context context) {
        super(context);
        init();
    }

    public DoodleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public DoodleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.setZOrderOnTop(true);

        surfaceHolder = this.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        surfaceHolder.addCallback(this);
        this.setFocusable(true);
    }

    /**
     * 初始化（必须调用）
     *
     * @param mode 设置板书模式
     */
    public void init(String sessionId, String toAccount, Mode mode, int paintColor, Context context, FlipListener flipListener) {
        TransactionCenter.getInstance().setDoodleViewInited(true);
        this.sessionId = sessionId;
        this.flipListener = flipListener;
        this.transactionManager = new TransactionManager(sessionId, toAccount, context);
        this.paintColor = paintColor;

        if (mode == Mode.PAINT || mode == Mode.BOTH) {
            this.paintChannel = new DoodleChannel();
            this.laserChannel = new DoodleChannel();
            laserChannel.setSize(10);
            paintChannel.setColor(paintColor);
        }

        if (mode == Mode.PLAYBACK || mode == Mode.BOTH) {
            this.transactionManager.registerTransactionObserver(this);
        }

        clearAll();
    }

    public void onResume() {
        new Handler(getContext().getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Canvas canvas = surfaceHolder.lockCanvas();
                if (canvas == null) {
                    return;
                }
                drawHistoryActions(canvas);
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }, 50);
    }


    /**
     * 退出涂鸦板时调用
     */
    public void end() {
        if (transactionManager != null) {
            transactionManager.end();
        }

        TransactionCenter.getInstance().setDoodleViewInited(false);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isSurfaceViewCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceView created, width = " + width + ", height = " + height);
        xZoom = width;
        yZoom = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isSurfaceViewCreated = false;
    }

    /**
     * ******************************* 绘图板 ****************************
     */

    /**
     * 设置绘制时的画笔颜色
     *
     * @param color
     */
    public void setPaintColor(int color) {
        this.paintChannel.setColor(convertRGBToARGB(color));
    }

    /**
     * 设置回放时的画笔颜色
     *
     * @param color
     */
    public void setPlaybackColor(DoodleChannel playbackChannel, int color) {
        playbackChannel.setColor(convertRGBToARGB(color));
    }

    /**
     * rgb颜色值转换为argb颜色值
     *
     * @param rgb
     * @return
     */
    public int convertRGBToARGB(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = (rgb >> 0) & 0xFF;

        return 0xff000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * 设置画笔的粗细
     *
     * @param size
     */
    public void setPaintSize(int size) {
        if (size > 0) {
            if (paintChannel != null) {
                this.paintChannel.paintSize = size;
            }
            this.paintSize = size;
        }
    }

    /**
     * 设置当前画笔的形状
     *
     * @param type
     */
    public void setPaintType(int type) {
        if (paintChannel != null) {
            this.paintChannel.setType(type);
        }
        this.paintType = type;
    }

    /**
     * 撤销一步
     *
     * @return 撤销是否成功
     */
    public boolean paintBack() {
        if (paintChannel == null) {
            return false;
        }

        boolean res = back(AppCache.getAccount(), true);
        transactionManager.sendRevokeTransaction();
        return res;
    }

    /**
     * 清空
     */
    public void clear() {
        clearAllByPage();
        transactionManager.sendClearSelfTransaction();
    }

    /**
     * 发送同步准备指令
     */
    public void sendSyncPrepare() {
        transactionManager.sendSyncPrepareTransaction();
    }

    /**
     * 触摸绘图
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!enableView) {
            return true;
        }
        int action = event.getAction();
        if (action == MotionEvent.ACTION_CANCEL) {
            return false;
        }

        float touchX = event.getX();
        float touchY = event.getY();
        Log.i(TAG, "x=" + touchX + ", y=" + touchY);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                onPaintActionStart(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                onPaintActionMove(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                onPaintActionEnd(touchX, touchY);
                break;
            default:
                break;
        }

        return true;
    }

    private void onPaintActionStart(float x, float y) {
        if (paintChannel == null) {
            return;
        }

        onActionStart(x, y);
        transactionManager.sendStartTransaction(x / xZoom, y / yZoom, paintChannel.paintColor, pageIndex);
        saveUserData(AppCache.getAccount(), new Transaction(Transaction.ActionStep.START, x / xZoom, y / yZoom, paintChannel.paintColor, pageIndex),
                pageIndex, false, false, false);
    }

    private void onPaintActionMove(float x, float y) {
        if (paintChannel == null) {
            return;
        }

        if (!isNewPoint(x, y)) {
            return;
        }

        onActionMove(x, y);
        transactionManager.sendMoveTransaction(x / xZoom, y / yZoom, paintChannel.paintColor, pageIndex);
        saveUserData(AppCache.getAccount(), new Transaction(Transaction.ActionStep.MOVE, x / xZoom, y / yZoom, paintChannel.paintColor, pageIndex),
                pageIndex, false, false, false);
    }

    private void onPaintActionEnd(float x, float y) {
        if (paintChannel == null) {
            return;
        }

        onActionEnd(pageIndex);
        transactionManager.sendEndTransaction(lastX / xZoom, lastY / yZoom, paintChannel.paintColor, pageIndex);
        saveUserData(AppCache.getAccount(), new Transaction(Transaction.ActionStep.END, lastX / xZoom, lastY / yZoom, paintChannel.paintColor, pageIndex),
                pageIndex, false, false, false);
    }

    /**
     * ******************************* 回放板 ****************************
     */

    @Override
    public void onTransaction(String account, final List<Transaction> transactions) {
        if (transactions.size() > 0 && transactions.get(0).isSync()
                && transactions.get(0).getUid().equals(AppCache.getAccount())) {
            // 断网重连，主播同步数据，收到自己的数据
            // 多人模式下，观众请求主播所有的同步数据时候，包括其他人的数据，以及自己的数据
            // 当收到自己的数据时，应该放入自己的paintChannel，因为可以做撤回，清除等操作
            if (!isSurfaceViewCreated) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onSyncMyTransactionsDraw(transactions);
                    }
                }, 50);
            } else {
                onSyncMyTransactionsDraw(transactions);
            }
            isSyncAlready = true;
            return;
        }

        DoodleChannel playbackChannel = initPlaybackChannel(account);

        List<Transaction> cache = new ArrayList<>(transactions.size());
        for (Transaction t : transactions) {
            if (t == null) {
                continue;
            }
            if (t.isPaint()) {
                // 正常画笔
                cache.add(t);
            } else {
                onMultiTransactionsDraw(playbackChannel, account, cache);
                cache.clear();
                if (t.isRevoke()) {
                    back(account, false);
                } else if (t.isClearSelf()) {
                    clearAllByPage();
                    transactionManager.sendClearAckTransaction();
                } else if (t.isClearAck()) {
                    clearAllByPage();
                } else if (t.isSyncRequest()) {
                    sendSyncData(account);
                } else if (t.isSyncPrepare()) {
                    clearAll();
                    transactionManager.sendSyncPrepareAckTransaction();
                } else if (t.isFlip()) {
                    // 收到翻页消息。先清空白板，然后做翻页操作。
                    LogUtil.i(TAG, "receive flip msg");
                    onReceiveFlipData(t.getCurrentPageNum());
                    flipListener.onFlipPage(t);
                }
            }
        }

        if (cache.size() > 0) {
            onMultiTransactionsDraw(playbackChannel, account, cache);
            cache.clear();
        }
    }

    private DoodleChannel initPlaybackChannel(String account) {
        DoodleChannel playbackChannel;
        if (playbackChannelMap.get(account) == null) {
            playbackChannel = new DoodleChannel();
            playbackChannel.paintSize = this.paintSize;
            playbackChannel.setType(this.paintType);
            playbackChannelMap.put(account, playbackChannel);
        } else {
            playbackChannel = playbackChannelMap.get(account);
        }

        return playbackChannel;
    }

    /**
     * ******************************* 基础绘图封装 ****************************
     */

    private void onActionStart(float x, float y) {
        DoodleChannel channel = paintChannel;
        if (channel == null) {
            return;
        }

        lastX = x;
        lastY = y;
        Canvas canvas = surfaceHolder.lockCanvas();
        drawHistoryActions(canvas);
        channel.action = new MyPath(x, y, channel.paintColor, channel.paintSize);
        channel.action.onDraw(canvas);
        if (canvas != null) {
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void onActionMove(float x, float y) {
        DoodleChannel channel = paintChannel;
        if (channel == null) {
            return;
        }
        Canvas canvas = surfaceHolder.lockCanvas();
        drawHistoryActions(canvas);
        // 绘制当前Action
        if (channel.action == null) {
            // 有可能action被清空，此时收到move，重新补个start
            onPaintActionStart(x, y);
        }
        channel.action.onMove(x, y);
        channel.action.onDraw(canvas);
        if (canvas != null) {
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void onActionEnd(int transactionPage) {
        DoodleChannel channel = paintChannel;
        if (channel == null || channel.action == null) {
            return;
        }

        // 添加到数据所携带的页码中
        channel.addAction(channel.action, transactionPage);
        channel.action = null;
    }

    // 收到的同步来的，自己绘制的画笔数据
    private void onSyncMyTransactionsDraw(List<Transaction> transactions) {
        int tempColor = paintChannel.paintColor;
        Canvas canvas = surfaceHolder.lockCanvas();

        for (Transaction t : transactions) {
            switch (t.getStep()) {
                case Transaction.ActionStep.START:
                    paintChannel.paintColor = convertRGBToARGB(t.getRgb());
                    lastX = t.getX() * xZoom;
                    lastY = t.getY() * yZoom;

                    paintChannel.action = new MyPath(lastX, lastY, paintChannel.paintColor, paintChannel.paintSize);
                    paintChannel.action.onDraw(canvas);
                    break;
                case Transaction.ActionStep.MOVE:
                    // 绘制当前Action
                    if (paintChannel.action == null) {
                        // 有可能action被清空，此时收到move，重新补个start
                        onPaintActionStart(t.getX() * xZoom, t.getY() * yZoom);
                    }
                    paintChannel.action.onMove(t.getX() * xZoom, t.getY() * yZoom);
                    paintChannel.action.onDraw(canvas);
                    break;
                case Transaction.ActionStep.END:
                    onActionEnd(t.getCurrentPageNum());
                    break;
                default:
                    break;
            }
        }

        drawHistoryActions(canvas);

        paintChannel.paintColor = tempColor;
        if (canvas != null) {
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    // 收到的回放版应该绘制的其他人的数据
    private void onMultiTransactionsDraw(DoodleChannel playbackChannel, String account, List<Transaction> transactions) {
        if (transactions == null || transactions.size() == 0 || playbackChannel == null) {
            return;
        }
        Canvas canvas = surfaceHolder.lockCanvas();
        for (Transaction t : transactions) {
            switch (t.getStep()) {
                case Transaction.ActionStep.LASER_PEN:
                    // 绘制背景
                    // canvas.drawColor(bgColor); TODO:XUWEN 为什么要放在这???背景已经被清空
                    laserChannel.action = null;
                    laserChannel.action = new MyFillCircle(t.getX() * xZoom, t.getY() * yZoom, Color.RED,
                            laserChannel.paintSize, 10);
                    laserChannel.action.onDraw(canvas);
                    break;
                case Transaction.ActionStep.LASER_PEN_END:
                    laserChannel.action = null;
                    break;
            }
        }
        // 重绘历史数据
        drawHistoryActions(canvas);

        // 绘制新的数据
        drawCurrentData(playbackChannel, account, transactions, canvas);

        if (canvas != null) {
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    // 绘制新数据
    private void drawCurrentData(DoodleChannel playbackChannel, String account, List<Transaction> transactions, Canvas canvas) {
        for (Transaction t : transactions) {
            switch (t.getStep()) {
                case Transaction.ActionStep.START:
                    if (playbackChannel.action != null) {
                        // 如果没有收到end包，在这里补提交，
                        // 添加action的时候，要添加到当前这个transaction的页面，有可能是同步过来的数据，不在pageIndex的页面
                        playbackChannel.addAction(playbackChannel.action, t.getCurrentPageNum());
                    }

                    saveUserData(account, t, t.getCurrentPageNum(), false, false, false);
                    setPlaybackColor(playbackChannel, t.getRgb());

                    playbackChannel.action = new MyPath(t.getX() * xZoom, t.getY() * yZoom, playbackChannel
                            .paintColor, playbackChannel.paintSize);
                    playbackChannel.action.onStart(canvas);
                    playbackChannel.action.onDraw(canvas);
                    break;
                case Transaction.ActionStep.MOVE:
                    saveUserData(account, t, t.getCurrentPageNum(), false, false, false);
                    if (playbackChannel.action != null) {
                        playbackChannel.action.onMove(t.getX() * xZoom, t.getY() * yZoom);
                        playbackChannel.action.onDraw(canvas);
                    }
                    break;
                case Transaction.ActionStep.END:
                    saveUserData(account, t, t.getCurrentPageNum(), false, false, false);
                    if (playbackChannel.action != null) {
                        // 添加action的时候，要添加到当前这个transaction的页面，有可能是同步过来的数据，不在pageIndex的页面
                        playbackChannel.addAction(playbackChannel.action, t.getCurrentPageNum());
                        playbackChannel.action = null;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void saveUserData(String account, Transaction t, int pageIndex, boolean isBack, boolean isClear, boolean isFlip) {
        SparseArray<List<Transaction>> userDataSparse = userDataMap.get(account);
        if (userDataSparse == null) {
            userDataSparse = new SparseArray<>();
        }
        List<Transaction> list = userDataSparse.get(pageIndex);
        if (isBack) {
            while (list != null && list.size() > 0 && list.get(list.size() - 1).getStep() != Transaction.ActionStep.START) {
                list.remove(list.size() - 1);
            }
            if (list != null && list.size() > 0) {
                list.remove(list.size() - 1);
            }
            userDataSparse.put(pageIndex, list);
            userDataMap.put(account, userDataSparse);
        } else if (isClear) {
            if (pageIndex == -1) {
                // 清空所有
                userDataMap.clear();
            } else {
                // 缓存只清空当前页码的点
                if (list == null) {
                    return;
                }
                list.clear();
                userDataSparse.put(pageIndex, list);
                userDataMap.put(account, userDataSparse);
            }
        } else if (isFlip) {
            if (list == null) {
                list = new ArrayList<>();
                list.add(t);
            } else {
                for (Transaction transaction : list) {
                    if (transaction.getStep() == Transaction.ActionStep.Flip) {
                        list.remove(transaction);
                        break;
                    }
                }
                list.add(t);
            }
            userDataSparse.put(0, list);
            userDataMap.put(account, userDataSparse);
        } else {
            if (list == null) {
                list = new ArrayList<>();
                list.add(t);
            } else {
                list.add(t);
            }
            userDataSparse.put(pageIndex, list);
            userDataMap.put(account, userDataSparse);
        }
    }

    private void drawHistoryActions(Canvas canvas) {
        if (canvas == null) {
            return;
        }

        // 擦除背景
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        Map<String, DoodleChannel> tempMap = new HashMap<>();
        tempMap.putAll(playbackChannelMap);
        Iterator<Map.Entry<String, DoodleChannel>> entries = tempMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, DoodleChannel> entry = entries.next();
            DoodleChannel playbackChannel = entry.getValue();

            if (playbackChannel != null && playbackChannel.hasActionsByPage(pageIndex)) {
                CopyOnWriteArrayList<Action> tempActions = playbackChannel.getActionsByPage(pageIndex);
                for (Iterator<Action> it = tempActions.iterator(); it.hasNext(); ) {
                    Action a = it.next();
                    a.onDraw(canvas);
                }

                // 绘制当前
                if (playbackChannel.action != null) {
                    playbackChannel.action.onDraw(canvas);
                }
            }
        }

        // 绘制所有历史Action
        if (paintChannel != null && paintChannel.hasActionsByPage(pageIndex)) {
            for (Action a : paintChannel.getActionsByPage(pageIndex)) {
                a.onDraw(canvas);
            }
            // 绘制当前
            if (paintChannel.action != null) {
                paintChannel.action.onDraw(canvas);
            }
        }

        if (laserChannel != null && laserChannel.action != null) {
            laserChannel.action.onDraw(canvas);
        }
    }

    private boolean back(String account, boolean isPaintView) {
        DoodleChannel channel = isPaintView ? paintChannel : playbackChannelMap.get(account);
        if (channel == null) {
            return false;
        }

        if (channel.hasActionsDataByPage(pageIndex)) {
            channel.backAction(pageIndex);
            saveUserData(account, null, pageIndex, true, false, false);
            Canvas canvas = surfaceHolder.lockCanvas();
            if (canvas == null) {
                return false;
            }
            drawHistoryActions(canvas);
            surfaceHolder.unlockCanvasAndPost(canvas);
            return true;
        }
        return false;
    }

    public void clearAll() {
        saveUserData(AppCache.getAccount(), null, -1, false, true, false);
        // clear 回放的所有频道
        for (Map.Entry<String, DoodleChannel> entry : playbackChannelMap.entrySet()) {
            clear(entry.getValue(), false);
        }
        // clear 自己画的频道
        clear(paintChannel, true);
    }

    public void clearAllByPage() {
        saveUserData(AppCache.getAccount(), null, pageIndex, false, true, false);
        // clear 回放的所有频道
        for (Map.Entry<String, DoodleChannel> entry : playbackChannelMap.entrySet()) {
            clearByPage(entry.getValue(), false, pageIndex);
        }
        // clear 自己画的频道
        clearByPage(paintChannel, true, pageIndex);
    }

    private void clearByPage(DoodleChannel playbackChannel, boolean isPaintView, int pageIndex) {
        DoodleChannel channel = isPaintView ? paintChannel : playbackChannel;
        if (channel == null) {
            return;
        }

        channel.clearActionsByPage(pageIndex);
        channel.action = null;
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas == null) {
            return;
        }
        drawHistoryActions(canvas);
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    private void clear(DoodleChannel playbackChannel, boolean isPaintView) {
        DoodleChannel channel = isPaintView ? paintChannel : playbackChannel;
        if (channel == null) {
            return;
        }

        channel.clearAll();
        channel.action = null;
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas == null) {
            return;
        }
        drawHistoryActions(canvas);
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    private boolean isNewPoint(float x, float y) {
        if (Math.abs(x - lastX) <= 0.1f && Math.abs(y - lastY) <= 0.1f) {
            return false;
        }

        lastX = x;
        lastY = y;

        return true;
    }

    public void sendSyncData(String account) {
        List<Transaction> syncList = new ArrayList<>();
        for (String key : userDataMap.keySet()) {
            for (int i = 1; i <= userDataMap.get(key).size(); i++) {
                if (userDataMap.get(key).valueAt(i) == null) {
                    continue;
                }
                syncList.addAll(userDataMap.get(key).valueAt(i));
            }
            syncList.addAll(userDataMap.get(key).valueAt(0));
            transactionManager.sendSyncTransaction(account, key, 1, syncList);
        }
    }

    // 主动翻页
    public void sendFlipData(String docId, int currentPageNum, int pageCount, int type) {
        this.pageIndex = currentPageNum;
        transactionManager.sendFlipTransaction(docId, currentPageNum, pageCount, type);
        saveUserData(AppCache.getAccount(), new Transaction().makeFlipTransaction(docId, currentPageNum, pageCount, type),
                0, false, false, true);

        refreshCurrentPageActions();
    }

    // 收到翻页指令
    public void onReceiveFlipData(int currentPageIndex) {
        this.pageIndex = currentPageIndex;

        refreshCurrentPageActions();
    }

    private void refreshCurrentPageActions() {
        Canvas canvas = surfaceHolder.lockCanvas();
        drawHistoryActions(canvas);
        if (canvas != null) {
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }
}
