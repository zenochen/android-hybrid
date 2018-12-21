package com.NewCenturyHotels.NewCentury.req;

public class SignUpReq {
    private String mobile;
    private String checkCodeToken;
    private String checkCode;
    private String validateToken;
    private String blackBox;
    private String nameCN;
    private String sex;
    private String memberSource;
    private String birthday;
    private String introducerCardNo;
    private String IDNo;
    private Boolean needLogin;
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

    public String getNameCN() {
        return nameCN;
    }

    public void setNameCN(String nameCN) {
        this.nameCN = nameCN;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getMemberSource() {
        return memberSource;
    }

    public void setMemberSource(String memberSource) {
        this.memberSource = memberSource;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getIntroducerCardNo() {
        return introducerCardNo;
    }

    public void setIntroducerCardNo(String introducerCardNo) {
        this.introducerCardNo = introducerCardNo;
    }

    public String getIDNo() {
        return IDNo;
    }

    public void setIDNo(String IDNo) {
        this.IDNo = IDNo;
    }

    public Boolean getNeedLogin() {
        return needLogin;
    }

    public void setNeedLogin(Boolean needLogin) {
        this.needLogin = needLogin;
    }
}
