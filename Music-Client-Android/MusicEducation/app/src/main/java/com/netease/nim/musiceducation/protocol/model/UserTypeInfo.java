package com.netease.nim.musiceducation.protocol.model;

import com.netease.nim.musiceducation.common.annotation.KeepMemberNames;

@KeepMemberNames
public class UserTypeInfo {

    private String accid;
    private int userType;

    public String getAccount() {
        return accid;
    }

    public int getUserType() {
        return userType;
    }

    public static UserTypeInfo createFakeUserTypeInfo(String accid, int userType) {
        UserTypeInfo userTypeInfo = new UserTypeInfo();
        userTypeInfo.accid = accid;
        userTypeInfo.userType = userType;
        return userTypeInfo;
    }
}
