package com.netease.nim.musiceducation.doodle;

import android.text.TextUtils;
import android.util.Log;

import com.netease.nim.musiceducation.common.LogUtil;
import com.netease.nimlib.sdk.rts.RTSManager2;
import com.netease.nimlib.sdk.rts.model.RTSTunData;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 白板数据收发中心
 * <p/>
 * Created by huangjun on 2015/6/29.
 */
public class TransactionCenter {

    private int index = 0;

    private final String TAG = "TransactionCenter";

    // sessionId to TransactionObserver
    private Map<String, TransactionObserver> observers = new HashMap<>(2);

    private Map<String, OnlineStatusObserver> onlineStatusObservers = new HashMap<>(2);

    private boolean isDoodleViewInited = false;

    public static TransactionCenter getInstance() {
        return TransactionCenterHolder.instance;
    }

    private static class TransactionCenterHolder {
        public static final TransactionCenter instance = new TransactionCenter();
    }

    public void registerObserver(String sessionId, TransactionObserver o) {
        this.observers.put(sessionId, o);
    }

    public void registerOnlineStatusObserver(String sessionId, OnlineStatusObserver o) {
        this.onlineStatusObservers.put(sessionId, o);
    }

    /**
     * 网络变化
     */
    public boolean onNetWorkChange(String sessionId, boolean isCreator) {
        if (onlineStatusObservers.containsKey(sessionId)) {
            return onlineStatusObservers.get(sessionId).onNetWorkChange(isCreator);
        }
        return false;
    }

    /**
     * 数据发送
     */
    public void sendToRemote(String sessionId, String toAccount, List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return;
        }

        String data = pack(transactions);
        try {
            RTSTunData channelData = new RTSTunData(sessionId, toAccount, data.getBytes
                    ("UTF-8"), data.getBytes().length);
            boolean isSend = RTSManager2.getInstance().sendData(channelData);
            Log.i(TAG, "SEND DATA = " + index + ", BYTES = " + data.getBytes().length + ", isSend=" + isSend);
        } catch (UnsupportedEncodingException e) {
            LogUtil.e("Transaction", "send to remote, getBytes exception : " + data);
        }
    }

    private String pack(List<Transaction> transactions) {
        StringBuilder sb = new StringBuilder();

        List<Transaction> tempList = new ArrayList<>();
        tempList.addAll(transactions);
        for (Transaction t : tempList) {
            sb.append(Transaction.pack(t));
        }

        // 打入序号
        sb.append(Transaction.packIndex(++index));

        return sb.toString();
    }

    /**
     * 数据接收
     */
    public void onReceive(String sessionId, String account, String data) {
        List<Transaction> transactions = unpack(data);
        if ((transactions != null ? transactions.size() : 0) <= 0) {
            return;
        }
        int step = transactions.get(0).getStep();

        if (observers.containsKey(sessionId)) {
            // 断网重连数据同步
            if (step == Transaction.ActionStep.SYNC) {
                // 同步数据的第一个包时同步包
                observers.get(sessionId).onTransaction(transactions.get(0).getUid(), transactions);
            } else {
                // 接收数据
                observers.get(sessionId).onTransaction(account, transactions);
            }
        }

        if (step == Transaction.ActionStep.LASER_PEN || step == Transaction.ActionStep.LASER_PEN_END) {
            return;
        }
    }

    private List<Transaction> unpack(String data) {
        if (TextUtils.isEmpty(data)) {
            return null;
        }

        List<Transaction> transactions = new ArrayList<>();
        String[] pieces = data.split(";");
        for (String p : pieces) {
            Transaction t = Transaction.unpack(p);
            if (t != null) {
                transactions.add(t);
            }
        }

        return transactions;
    }

    public boolean isDoodleViewInited() {
        return isDoodleViewInited;
    }

    public void setDoodleViewInited(boolean doodleViewInited) {
        isDoodleViewInited = doodleViewInited;
    }
}
