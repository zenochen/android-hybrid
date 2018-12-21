package com.NewCenturyHotels.NewCentury.bean;

public class AppVersionRes {
    private String appVersion;//app版本
    private String appVersionName;//app版本名
    private String description;//更新描述
    private String downloadUrl;//下载地址url
    private Integer id;
    private Integer ifForcedUpdate;//是否强制更新
    private ImgData imgData;//图片
    private String versionTypeEnum;
    private String versionTypeEnumName;

    public class ImgData{
        private ImgDataInfo[] personalImg;//APP个人中心图片
        private ImgDataInfo[] startUpImg;//APP启动广告图片
        private ImgDataInfo[] pageImg;//移动端首页BANNER

        public ImgDataInfo[] getPersonalImg() {
            return personalImg;
        }

        public void setPersonalImg(ImgDataInfo[] personalImg) {
            this.personalImg = personalImg;
        }

        public ImgDataInfo[] getStartUpImg() {
            return startUpImg;
        }

        public void setStartUpImg(ImgDataInfo[] startUpImg) {
            this.startUpImg = startUpImg;
        }

        public ImgDataInfo[] getPageImg() {
            return pageImg;
        }

        public void setPageImg(ImgDataInfo[] pageImg) {
            this.pageImg = pageImg;
        }
    }

    public class ImgDataInfo{
        private String advertisingImage;
        private String contentAdvertisingEnum;
        private String description;
        private String name;
        private String redirectUrl;

        public String getAdvertisingImage() {
            return advertisingImage;
        }

        public void setAdvertisingImage(String advertisingImage) {
            this.advertisingImage = advertisingImage;
        }

        public String getContentAdvertisingEnum() {
            return contentAdvertisingEnum;
        }

        public void setContentAdvertisingEnum(String contentAdvertisingEnum) {
            this.contentAdvertisingEnum = contentAdvertisingEnum;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRedirectUrl() {
            return redirectUrl;
        }

        public void setRedirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }
    }

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIfForcedUpdate() {
        return ifForcedUpdate;
    }

    public void setIfForcedUpdate(Integer ifForcedUpdate) {
        this.ifForcedUpdate = ifForcedUpdate;
    }

    public ImgData getImgData() {
        return imgData;
    }

    public void setImgData(ImgData imgData) {
        this.imgData = imgData;
    }

    public String getVersionTypeEnum() {
        return versionTypeEnum;
    }

    public void setVersionTypeEnum(String versionTypeEnum) {
        this.versionTypeEnum = versionTypeEnum;
    }

    public String getVersionTypeEnumName() {
        return versionTypeEnumName;
    }

    public void setVersionTypeEnumName(String versionTypeEnumName) {
        this.versionTypeEnumName = versionTypeEnumName;
    }
}
