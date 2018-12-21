package com.NewCenturyHotels.NewCentury.req;

public class VerifyCheckLoginedReq {
    private String checkCodeType;
    private String checkCodeToken;
    private String checkCode;
    private String messageType;

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getCheckCodeType() {
        return checkCodeType;
    }

    public void setCheckCodeType(String checkCodeType) {
        this.checkCodeType = checkCodeType;
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
