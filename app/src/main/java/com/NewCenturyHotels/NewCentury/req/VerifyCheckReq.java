package com.NewCenturyHotels.NewCentury.req;

public class VerifyCheckReq {
    private String checkCodeType;
    private String email;
    private String mobile;
    private String checkCodeToken;
    private String checkCode;

    public String getCheckCodeType() {
        return checkCodeType;
    }

    public void setCheckCodeType(String checkCodeType) {
        this.checkCodeType = checkCodeType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCheckCodeToken() {
        return checkCodeToken;
    }

    public void setCheckCodeToken(String checkCodeToken) {
        this.checkCodeToken = checkCodeToken;
    }

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }
}
