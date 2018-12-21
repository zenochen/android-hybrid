package com.NewCenturyHotels.NewCentury.req;

public class ChangeMobileEmailReq {
    private String checkCodeToken;
    private String checkCode;
    private String newUserName;
    private String newCheckCode;
    private String newCheckCodeToken;

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

    public String getNewUserName() {
        return newUserName;
    }

    public void setNewUserName(String newUserName) {
        this.newUserName = newUserName;
    }

    public String getNewCheckCode() {
        return newCheckCode;
    }

    public void setNewCheckCode(String newCheckCode) {
        this.newCheckCode = newCheckCode;
    }

    public String getNewCheckCodeToken() {
        return newCheckCodeToken;
    }

    public void setNewCheckCodeToken(String newCheckCodeToken) {
        this.newCheckCodeToken = newCheckCodeToken;
    }
}
