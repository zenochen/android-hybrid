package com.NewCenturyHotels.NewCentury.bean;

public class AppVersion {
    private String appVersion;//版本号
    private String appVersionName;//版本详情
    private String ifForcedUpdate;//是否强制更新
    private String versionTypeEnum;
    private String description;//更新描述
    private String downloadUrl;//下载地址

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppVersionName() {
        return appVersionName;
    }

    public void setAppVersionName(String appVersionName) {
        this.appVersionName = appVersionName;
    }

    public String getIfForcedUpdate() {
        return ifForcedUpdate;
    }

    public void setIfForcedUpdate(String ifForcedUpdate) {
        this.ifForcedUpdate = ifForcedUpdate;
    }

    public String getVersionTypeEnum() {
        return versionTypeEnum;
    }

    public void setVersionTypeEnum(String versionTypeEnum) {
        this.versionTypeEnum = versionTypeEnum;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
