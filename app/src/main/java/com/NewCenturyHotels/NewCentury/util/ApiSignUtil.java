package com.NewCenturyHotels.NewCentury.util;

public class ApiSignUtil {

    final static String appkey = "200004";//400001
    final static String appSecret = "hd4kjamvq373fl457c1burwk22c3je";//abcdefg
    private static String v = "";

    public static void init(){
        for(int i = 0;i < 10;i++){
            v += RandomUtil.getRandomIndex(0,9);
        }
    }

    public static String getAppkey() {
        return appkey;
    }

    public static String getAppSecret() {
        return appSecret;
    }

    public static String getV() {
        return v;
    }

    public static void setV(String v) {
        ApiSignUtil.v = v;
    }

    public static String getUrl(String url, String requestBody){

        long timestamp = System.currentTimeMillis();

        String token = appSecret + "&" + appkey + "&" + requestBody + "&" + timestamp;

        token = MD5Util.encryp(token);

        url = url + "?" + "appkey=" + appkey + "&timestamp=" + timestamp + "&token=" + token + "&v=" + v;

        return url;
    }
}
