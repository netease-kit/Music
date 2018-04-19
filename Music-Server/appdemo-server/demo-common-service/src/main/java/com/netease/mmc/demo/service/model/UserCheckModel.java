package com.netease.mmc.demo.service.model;

/**
 * 用户账号校验结果.
 *
 * @author hzwanglin1
 * @date 2018/4/4
 * @since 1.0
 */
public class UserCheckModel {
    private String accid;

    private Integer userType;

    public UserCheckModel() {
    }

    public UserCheckModel(String accid, Integer userType) {
        this.accid = accid;
        this.userType = userType;
    }

    public String getAccid() {
        return accid;
    }

    public void setAccid(String accid) {
        this.accid = accid;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserCheckModel{");
        sb.append("accid='").append(accid).append('\'');
        sb.append(", userType=").append(userType);
        sb.append('}');
        return sb.toString();
    }
}