package com.netease.nim.musiceducation.doodle;

import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Transaction发包管理器
 * <p/>
 * Created by huangjun on 2015/6/24.
 */
class TransactionManager {

    private final int TIMER_TASK_PERIOD = 30;

    private String sessionId;

    private String toAccount;

    private Handler handler;

    private List<Transaction> cache = new ArrayList<>(1000);

    public TransactionManager(String sessionId, String toAccount, Context context) {
        this.sessionId = sessionId;
        this.toAccount = toAccount;
        this.handler = new Handler(context.getMainLooper());
        this.handler.postDelayed(timerTask, TIMER_TASK_PERIOD); // 立即开启定时器
    }

    public void end() {
        this.handler.removeCallbacks(timerTask);
    }

    public void registerTransactionObserver(TransactionObserver o) {
        TransactionCenter.getInstance().registerObserver(sessionId, o);
    }

    public void sendStartTransaction(float x, float y, int rgb, int pageIndex) {
        cache.add(new Transaction().makeStartTransaction(x, y, rgb, pageIndex));
    }

    public void sendMoveTransaction(float x, float y, int rgb, int pageIndex) {
        cache.add(new Transaction().makeMoveTransaction(x, y, rgb, pageIndex));
    }

    public void sendEndTransaction(float x, float y, int rgb, int pageIndex) {
        cache.add(new Transaction().makeEndTransaction(x, y, rgb, pageIndex));
    }

    public void sendRevokeTransaction() {
        cache.add(new Transaction().makeRevokeTransaction());
    }

    public void sendClearSelfTransaction() {
        cache.add(new Transaction().makeClearSelfTransaction());
    }

    public void sendClearAckTransaction() {
        cache.add(new Transaction().makeClearAckTransaction());
    }

    public void sendSyncTransaction(String toAccount, String paintAccount, int end, List<Transaction> transactions) {
        List<Transaction> syncCache = new ArrayList<>(1000);
        // 封装transaction中的uid，是画笔数据的owner
        syncCache.add(new Transaction().makeSyncTransaction(paintAccount, end));
        syncCache.addAll(transactions);
        // account 是发送的对象
        TransactionCenter.getInstance().sendToRemote(sessionId, toAccount, syncCache);
    }

    public void sendSyncPrepareTransaction() {
        cache.add(new Transaction().makeSyncPrepareTransaction());
    }

    public void sendSyncPrepareAckTransaction() {
        cache.add(new Transaction().makeSyncPrepareAckTransaction());
    }

    public void sendFlipTransaction(String docId, int currentPageNum, int pageCount, int type) {
        List<Transaction> flipCache = new ArrayList<>();
        flipCache.add(new Transaction().makeFlipTransaction(docId, currentPageNum, pageCount, type));
        TransactionCenter.getInstance().sendToRemote(sessionId, toAccount, flipCache);
    }

    private Runnable timerTask = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(timerTask);
            {
                if (cache.size() > 0) {
                    sendCacheTransaction();
                }
            }
            handler.postDelayed(timerTask, TIMER_TASK_PERIOD);
        }
    };

    private void sendCacheTransaction() {
        TransactionCenter.getInstance().sendToRemote(sessionId, toAccount, this.cache);
        cache.clear();
    }
}
