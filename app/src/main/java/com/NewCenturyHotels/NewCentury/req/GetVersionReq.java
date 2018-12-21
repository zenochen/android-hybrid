package com.NewCenturyHotels.NewCentury.req;

public class GetVersionReq {
    private String appVersion;
    private String versionTypeEnum;

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getVersionTypeEnum() {
        return versionTypeEnum;
    }

    public void setVersionTypeEnum(String versionTypeEnum) {
        this.versionTypeEnum = versionTypeEnum;
    }
}
