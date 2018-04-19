package com.netease.nim.musiceducation.business.helper;

import com.netease.nim.musiceducation.common.LogUtil;
import com.netease.nimlib.sdk.avchat.AVChatNetDetectCallback;
import com.netease.nimlib.sdk.avchat.AVChatNetDetector;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by weilv on 17/1/4.
 */

public class NetDetectHelpter {
    private static final String TAG = NetDetectHelpter.class.getSimpleName();

    public static final int STATUS_INIT = 0;
    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_CANCEL = 2;
    public static final int STATUS_COMPLETE = 3;

    private NetDetectResult netDetectResult = new NetDetectResult();
    private final List<NetDetectObserver> netDetectObserverList = new CopyOnWriteArrayList<>();
    private AtomicInteger netDetectStatus = new AtomicInteger(STATUS_INIT);
    private long timeStamp;

    public static NetDetectHelpter getInstance() {
        return InstanceHolder.instance;
    }


    public void reset() {
        netDetectStatus.set(STATUS_INIT);
        netDetectObserverList.clear();
    }

    public void observeNetDetectStatus(NetDetectObserver observer, boolean add) {
        if (add) {
            netDetectObserverList.add(observer);
        } else {
            netDetectObserverList.remove(observer);
        }
    }

    public void startNetDetect() {
        netDetectStatus.set(STATUS_RUNNING);
        String uuid = AVChatNetDetector.startNetDetect(new AVChatNetDetectCallback() {
            @Override
            public void onDetectResult(String id,
                                       int code,
                                       int loss,
                                       int rttMax,
                                       int rttMin,
                                       int rttAvg,
                                       int mdev,
                                       String info) {
                timeStamp = System.currentTimeMillis();
                netDetectStatus.set(STATUS_COMPLETE);
                netDetectResult.setId(id);
                netDetectResult.setCode(code);
                netDetectResult.setLoss(loss);
                netDetectResult.setRttMax(rttMax);
                netDetectResult.setRttMin(rttMin);
                netDetectResult.setRttAvg(rttAvg);
                netDetectResult.setMdev(mdev);
                netDetectResult.setInfo(info);
                LogUtil.d(TAG, "onDetectResult, timeStamp:" + timeStamp+",netDetectStatus:" + netDetectStatus.intValue()+",code:"+code);
                notifyNetDetectResult(netDetectResult);
            }
        });
        LogUtil.d(TAG, "startNetDetect, uuid:"+uuid+",netDetectStatus:" + netDetectStatus.intValue());
    }

    public void stopNetDetect(String id) {
        netDetectStatus.set(STATUS_CANCEL);
        AVChatNetDetector.stopNetDetect(id);
    }

    private void notifyNetDetectResult(NetDetectResult result) {
        for (NetDetectObserver observer : netDetectObserverList) {
            observer.onNetDetectResult(result);
        }
    }

    public AtomicInteger getNetDetectStatus() {
        return netDetectStatus;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public NetDetectResult getNetDetectResult() {
        return netDetectResult;
    }

    public interface NetDetectObserver {
        void onNetDetectResult(NetDetectResult result);
    }

    public static class InstanceHolder {
        public final static NetDetectHelpter instance = new NetDetectHelpter();
    }

    public class NetDetectResult {
        private String id;
        private int code;
        private int loss;
        private int rttMax;
        private int rttMin;
        private int rttAvg;
        private int mdev;
        private String info;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public int getLoss() {
            return loss;
        }

        public void setLoss(int loss) {
            this.loss = loss;
        }

        public int getRttMax() {
            return rttMax;
        }

        public void setRttMax(int rttMax) {
            this.rttMax = rttMax;
        }

        public int getRttMin() {
            return rttMin;
        }

        public void setRttMin(int rttMin) {
            this.rttMin = rttMin;
        }

        public int getRttAvg() {
            return rttAvg;
        }

        public void setRttAvg(int rttAvg) {
            this.rttAvg = rttAvg;
        }

        public int getMdev() {
            return mdev;
        }

        public void setMdev(int mdev) {
            this.mdev = mdev;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        @Override
        public String toString() {
            return "NetDetectResult{" +
                    "id='" + id + '\'' +
                    ", code=" + code +
                    ", loss=" + loss +
                    ", rttMax=" + rttMax +
                    ", rttMin=" + rttMin +
                    ", rttAvg=" + rttAvg +
                    ", mdev=" + mdev +
                    ", info='" + info + '\'' +
                    '}';
        }
    }


}
