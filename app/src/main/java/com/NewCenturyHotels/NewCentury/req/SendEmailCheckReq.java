package com.NewCenturyHotels.NewCentury.req;

public class SendEmailCheckReq {
    private String checkCodeType;
    private String moduleCode;
    private String email;
    private String validateToken;
    private String blackBox;

    public String getCheckCodeType() {
        return checkCodeType;
    }

    public void setCheckCodeType(String checkCodeType) {
        this.checkCodeType = checkCodeType;
    }

    public String getMessageContentType() {
        return moduleCode;
    }

    public void setMessageContentType(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
