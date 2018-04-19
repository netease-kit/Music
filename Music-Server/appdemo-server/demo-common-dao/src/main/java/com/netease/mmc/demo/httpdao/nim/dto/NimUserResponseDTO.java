package com.netease.mmc.demo.httpdao.nim.dto;

import java.util.Objects;

import com.netease.mmc.demo.common.enums.HttpCodeEnum;

/**
 * 用户信息接口返回值DTO.
 *
 * @author hzwanglin1
 * @date 17-6-25
 * @since 1.0
 */
public class NimUserResponseDTO {
    /**
     * 状态码
     */
    private Integer code;

    /**
     * 错误描述
     */
    private String desc;

    /**
     * 用户账号相关信息
     */
    private NIMUserDTO info;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public NIMUserDTO getInfo() {
        return info;
    }

    public void setInfo(NIMUserDTO info) {
        this.info = info;
    }

    public boolean isSuccess() {
        return Objects.equals(code, HttpCodeEnum.OK.value());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NimUserResponseDTO{");
        sb.append("code=").append(code);
        sb.append(", desc='").append(desc).append('\'');
        sb.append(", info=").append(info);
        sb.append('}');
        return sb.toString();
    }
}
