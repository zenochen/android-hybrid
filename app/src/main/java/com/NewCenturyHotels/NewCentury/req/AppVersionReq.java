package com.NewCenturyHotels.NewCentury.req;

public class AppVersionReq {
    private String appVersion;//app版本
    private String deviceNumber;//设备号
    private String location;//位置
    private String token;//登录token
    private String systemVersion;//systemVersion
    private String channel;//渠道
    private String versionTypeEnum;//app类型

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getVersionTypeEnum() {
        return versionTypeEnum;
    }

    public void setVersionTypeEnum(String versionTypeEnum) {
        this.versionTypeEnum = versionTypeEnum;
    }
}
