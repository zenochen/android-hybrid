package com.NewCenturyHotels.NewCentury.req;

public class SignByCodeReq {
    private String mobile;
    private String checkCodeToken;
    private String checkCode;
    private String validateToken;
    private String blackBox;
    private String source;
    private String deviceId;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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

    public String getValidateToken() {
        return validateToken;
    }

    public void setValidateToken(String validateToken) {
        this.validateToken = validateToken;
    }

    public String getBlackBox() {
        return blackBox;
    }

    public void setBlackBox(String blackBox) {
        this.blackBox = blackBox;
    }
}
