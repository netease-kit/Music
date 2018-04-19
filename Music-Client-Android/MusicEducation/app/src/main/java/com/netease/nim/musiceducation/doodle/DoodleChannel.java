package com.netease.nim.musiceducation.doodle;

import android.graphics.Color;
import android.util.SparseArray;

import com.netease.nim.musiceducation.doodle.action.Action;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 涂鸦板通道（输入通道，输出通道）
 * <p/>
 * Created by huangjun on 2015/6/29.
 */
class DoodleChannel {
    /**
     * 当前所选的画笔
     */
    public int type = 0; // 当前的形状类型

    public Action action; // 当前的形状对象

    int paintColor = Color.BLACK;

    int paintSize = 5;

    private int lastPaintColor = paintColor; // 上一次使用的画笔颜色（橡皮擦切换回形状时，恢复上次的颜色）

    private int lastPaintSize = paintSize; // 上一次使用的画笔粗细（橡皮擦切换回形状时，恢复上次的粗细）

    /**
     * 记录所有形状的列表
     */
    private SparseArray<CopyOnWriteArrayList<Action>> actionMap = new SparseArray<>(5);

    boolean hasActionsByPage(int pageIndex) {
        return getActionsByPage(pageIndex) != null;
    }

    boolean hasActionsDataByPage(int pageIndex) {
        CopyOnWriteArrayList<Action> actions = getActionsByPage(pageIndex);
        return actions != null && !actions.isEmpty();
    }

    CopyOnWriteArrayList<Action> getActionsByPage(int pageIndex) {
        return actionMap.get(pageIndex);
    }

    void addAction(Action action, int pageIndex) {
        CopyOnWriteArrayList<Action> actions = actionMap.get(pageIndex);
        if (actions == null) {
            actions = new CopyOnWriteArrayList<>();
            actionMap.put(pageIndex, actions);
        }

        actions.add(action);
    }

    void backAction(int pageIndex) {
        CopyOnWriteArrayList<Action> actions = getActionsByPage(pageIndex);
        if (actions != null && actions.size() > 0) {
            actions.remove(actions.size() - 1);
        }
    }

    void clearActionsByPage(int pageIndex) {
        CopyOnWriteArrayList<Action> actions = getActionsByPage(pageIndex);
        if (actions != null) {
            actions.clear();
        }
    }

    void clearAll() {
        actionMap.clear();
    }

    /**
     * 设置当前画笔的形状
     *
     * @param type
     */
    public void setType(int type) {
        if (this.type == SupportActionType.getInstance().getEraserType()) {
            // 从橡皮擦切换到某种形状，恢复画笔颜色，画笔粗细
            this.paintColor = this.lastPaintColor;
            this.paintSize = this.lastPaintSize;
        }

        this.type = type;
    }

    /**
     * 设置当前画笔为橡皮擦
     */
    public void setEraseType(int bgColor, int size) {
        this.type = SupportActionType.getInstance().getEraserType();
        this.lastPaintColor = this.paintColor; // 备份当前的画笔颜色
        this.lastPaintSize = this.paintSize; // 备份当前的画笔粗细
        this.paintColor = bgColor;
        if (size > 0) {
            paintSize = size;
        }
    }

    /**
     * 设置当前画笔的颜色
     *
     * @param color
     */
    public void setColor(int color) {
        if (this.type == SupportActionType.getInstance().getEraserType()) {
            // 如果正在使用橡皮擦，那么不能更改画笔颜色
            return;
        }

        this.paintColor = color;
    }

    /**
     * 设置画笔的粗细
     *
     * @param size
     */
    public void setSize(int size) {
        if (size > 0) {
            this.paintSize = size;
        }
    }
}
