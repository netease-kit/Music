package com.netease.mmc.demo.service.model;

import java.util.Objects;

import org.springframework.http.HttpStatus;

/**
 * 业务处理返回值Model.
 *
 * @author hzwanglin1
 * @date 2018/3/14
 * @since 8.3.1
 */
public class BizResultModel<T> {
    private Integer code;

    private String message;

    private T data;

    /**
     * 封装处理成功返回值
     *
     * @param data
     */
    public BizResultModel(T data) {
        this.code = HttpStatus.OK.value();
        this.data = data;
    }

    /**
     * 封装处理失败返回值
     *
     * @param code
     * @param message
     */
    public BizResultModel(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public boolean isSuccess() {
        return Objects.equals(HttpStatus.OK.value(), code);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BizResultModel{");
        sb.append("code=").append(code);
        sb.append(", message='").append(message).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}