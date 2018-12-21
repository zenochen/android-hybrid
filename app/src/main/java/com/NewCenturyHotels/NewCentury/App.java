package com.NewCenturyHotels.NewCentury;

import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.bean.AppVersionRes;
import com.NewCenturyHotels.NewCentury.cons.AppInfo;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;
import com.NewCenturyHotels.NewCentury.req.AppVersionReq;
import com.NewCenturyHotels.NewCentury.util.ApiSignUtil;
import com.NewCenturyHotels.NewCentury.util.AppUtils;
import com.NewCenturyHotels.NewCentury.util.DownloadUtil;
import com.NewCenturyHotels.NewCentury.util.HttpHelper;
import com.NewCenturyHotels.NewCentury.util.PropertyUtil;
import com.NewCenturyHotels.NewCentury.util.SharedPreferencesHelper;
import com.NewCenturyHotels.NewCentury.util.SystemUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.socialize.PlatformConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class App extends Application {

    private static final String TAG = App.class.getName();

    public static HashMap<String,Object> mInfo = new HashMap<>();

    SharedPreferencesHelper sharedPreferencesHelper;

    String fileDir = "";

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try{
                if(msg.what == 1){
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        JsonObject data = jo.getAsJsonObject("data");
                        Gson gson = new Gson();
                        AppVersionRes res = gson.fromJson(data,AppVersionRes.class);
                        mInfo.put(AppInfo.APP_VERSION,res);

                        //获取图片路径
                        AppVersionRes.ImgData imgData = res.getImgData();
                        String pageStrs = "";
                        String startupStrs = "";
                        String personalStrs = "";
                        List<String> picList = new ArrayList<>();
                        for(int i = 0;i < imgData.getPageImg().length; i++){
                            String url = imgData.getPageImg()[i].getAdvertisingImage();
                            pageStrs += url + ",";
                            String fileName = url.substring(url.lastIndexOf("/")+1);
                            picList.add(fileName);
                        }
                        for(int j = 0;j < imgData.getStartUpImg().length; j++){
                            String url = imgData.getStartUpImg()[j].getAdvertisingImage();
                            startupStrs += url + ",";
                            String fileName = url.substring(url.lastIndexOf("/")+1);
                            picList.add(fileName);
                        }
                        for(int k = 0;k < imgData.getPersonalImg().length; k++){
                            String url = imgData.getPersonalImg()[k].getAdvertisingImage();
                            personalStrs += url + ",";
                            String fileName = url.substring(url.lastIndexOf("/")+1);
                            picList.add(fileName);
                        }

                        //保存图片到本地
                        String pageLocal = "";
                        String startupLocal = "";
                        String personalLocal = "";

                        if(!pageStrs.isEmpty()){
                            pageStrs = pageStrs.substring(0,pageStrs.length() - 1);
                            if(!pageStrs.equals(sharedPreferencesHelper.get(SharedPref.HOME_IMAGES,""))){
                                sharedPreferencesHelper.put(SharedPref.HOME_IMAGES,pageStrs);
                                for(int x = 0;x < imgData.getPageImg().length; x++){
                                    String url = imgData.getPageImg()[x].getAdvertisingImage();
                                    String fileName = url.substring(url.lastIndexOf("/")+1);
                                    DownloadUtil.get().downloadPic(url,fileDir,fileName);
                                    pageLocal += fileDir + fileName + ",";
                                }

                                if(!pageLocal.isEmpty()){
                                    pageLocal = pageLocal.substring(0,pageLocal.length() - 1);
                                    sharedPreferencesHelper.put(SharedPref.HOME_IMAGES_LOCAL,pageLocal);
                                }
                            }
                        }

                        if(!startupStrs.isEmpty()){
                            startupStrs = startupStrs.substring(0,startupStrs.length() - 1);
                            if(!startupStrs.equals(sharedPreferencesHelper.get(SharedPref.APP_AD,""))){
                                sharedPreferencesHelper.put(SharedPref.APP_AD,startupStrs);
                                for(int x = 0;x < imgData.getStartUpImg().length; x++){
                                    String url = imgData.getStartUpImg()[x].getAdvertisingImage();
                                    String fileName = url.substring(url.lastIndexOf("/")+1);
                                    DownloadUtil.get().downloadPic(url,fileDir,fileName);
                                    startupLocal += fileDir + fileName + ",";
                                }
                                if(!startupLocal.isEmpty()){
                                    startupLocal = startupLocal.substring(0,startupLocal.length() - 1);
                                    sharedPreferencesHelper.put(SharedPref.APP_AD_LOCAL,startupLocal);
                                }
                            }
                        }

                        if(!personalStrs.isEmpty()){
                            personalStrs = personalStrs.substring(0,personalStrs.length() - 1);
                            if(!personalStrs.equals(sharedPreferencesHelper.get(SharedPref.USER_IMAGE,""))){
                                sharedPreferencesHelper.put(SharedPref.USER_IMAGE,personalStrs);
                                for(int x = 0;x < imgData.getPersonalImg().length; x++){
                                    String url = imgData.getPersonalImg()[x].getAdvertisingImage();
                                    String fileName = url.substring(url.lastIndexOf("/")+1);
                                    DownloadUtil.get().downloadPic(url,fileDir,fileName);
                                    personalLocal += fileDir + fileName + ",";
                                }
                                if(!personalLocal.isEmpty()){
                                    personalLocal = personalLocal.substring(0,personalLocal.length() - 1);
                                    sharedPreferencesHelper.put(SharedPref.USER_IMAGE_LOCAL,personalLocal);
                                }
                            }
                        }

                        //删除以前的图片
                        File[] files = new File(fileDir).listFiles();
                        if(files != null){
                            for(int i = 0;i < files.length;i++){
                                String filename = files[i].getName();
                                if(!picList.contains(filename)){
                                    files[i].delete();
                                }
                            }
                        }
                    }else{
                        Log.e(TAG, "handleMessage: "+message);
                    }
                }
            }catch (Exception e){
                Log.e(TAG, "Exception: " + e.getMessage());
                Toast.makeText(getApplicationContext(),"连接超时",Toast.LENGTH_LONG).show();
            }

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        //友盟初始化
        UMConfigure.init(this, "5745325be0f55a101e000404","umeng", UMConfigure.DEVICE_TYPE_PHONE,
                "8ad2e5c924932faf3be7912e04e49c46");
        initPush();

        //自动上报错误
        CrashReport.initCrashReport(getApplicationContext(), "注册时申请的APPID", false);

        sharedPreferencesHelper = new SharedPreferencesHelper(getApplicationContext());

        fileDir = getApplicationContext().getFilesDir().getAbsolutePath() + "/shands/";

        //初始化请求token
        String token = sharedPreferencesHelper.get(SharedPref.TOKEN,"").toString();
        HttpHelper.setAuthorization(token);
        //api参数初始化
        String v = sharedPreferencesHelper.get(SharedPref.API_V,"").toString();
        if(v.isEmpty()){
            ApiSignUtil.init();
            sharedPreferencesHelper.put(SharedPref.API_V,ApiSignUtil.getV());
        }else{
            ApiSignUtil.setV(v);
        }
    }

    void initPush(){
        PushAgent.getInstance(this).onAppStart();

        PushAgent mPushAgent = PushAgent.getInstance(this);
        //注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {

            @Override
            public void onSuccess(String deviceToken) {
                //注册成功会返回device token
                Log.i(TAG, "device token: " + deviceToken);
                sharedPreferencesHelper.put(SharedPref.DEVICE_TOKEN,deviceToken);
                initAppVersion();
            }

            @Override
            public void onFailure(String s, String s1) {
                Log.i(TAG, "register failed: " + s + " " + s1);
            }
        });

    }

    void initAppVersion(){
        new Thread(){
            @Override
            public void run() {
                Properties p = null;
                try {
                    p = PropertyUtil.getConfigProperties(getApplicationContext());
                    String channel = p.getProperty("channel").toString();
                    String version = String.valueOf(AppUtils.getVersionCode(getApplicationContext()));
                    String sysVersion = SystemUtil.getSystemVersion();
                    String deviceToken = sharedPreferencesHelper.get(SharedPref.DEVICE_TOKEN,"").toString();
                    String location = sharedPreferencesHelper.get(SharedPref.LOCATION,"").toString();
                    String token = sharedPreferencesHelper.get(SharedPref.USER_TOKEN,"").toString();
                    Gson gson = new Gson();
                    AppVersionReq req = new AppVersionReq();
                    req.setAppVersion(version);
                    req.setChannel(channel);
                    req.setDeviceNumber(deviceToken);
                    req.setSystemVersion(sysVersion);
                    req.setLocation(location);
                    req.setToken(token);
                    req.setVersionTypeEnum(Const.SOURCE_TYPE);

                    String json = gson.toJson(req);
                    String url = Const.APP_VERSION;
                    HttpHelper.sendOkHttpPost(url, json, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e(TAG,e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String body = response.body().string();
                            Log.i(TAG, "onResponse: " + body);
                            Bundle bundle = new Bundle();
                            bundle.putString("ret",body);
                            Message msg = new Message();
                            msg.setData(bundle);
                            msg.what = 1;
                            handler.sendMessage(msg);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG,e.getMessage());
                }

            }
        }.start();
    }

    {
        PlatformConfig.setWeixin("wxd1edb80decbaf700", "3baf1193c85774b3fd9d18447d76cab0");
        PlatformConfig.setSinaWeibo("3921700954", "04b48b094faeb16683c32669824ebdad", "http://sns.whalecloud.com");
        PlatformConfig.setQQZone("101511394", "bb7e28b22ce562a8b94d210a49f9d7c8");
    }
}
