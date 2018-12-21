package com.NewCenturyHotels.NewCentury.bean;

import com.NewCenturyHotels.NewCentury.util.ApiSignUtil;
import com.NewCenturyHotels.NewCentury.util.HttpHelper;
import com.NewCenturyHotels.NewCentury.util.PropertyUtil;
import com.google.gson.Gson;

import java.util.Properties;

public class AppInitJs {
    private String appkey;
    private String appSecret;
    private String v;
    private String channel;
    private String token;

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
