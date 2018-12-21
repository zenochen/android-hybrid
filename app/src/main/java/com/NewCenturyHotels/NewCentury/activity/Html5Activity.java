package com.NewCenturyHotels.NewCentury.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.bean.AppInitJs;
import com.NewCenturyHotels.NewCentury.bean.DeviceInfo;
import com.NewCenturyHotels.NewCentury.bean.GpsInfo;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;
import com.NewCenturyHotels.NewCentury.util.ApiSignUtil;
import com.NewCenturyHotels.NewCentury.util.Base64BitmapUtil;
import com.NewCenturyHotels.NewCentury.util.BitmapCompressHelper;
import com.NewCenturyHotels.NewCentury.util.HttpHelper;
import com.NewCenturyHotels.NewCentury.util.HttpUtil;
import com.NewCenturyHotels.NewCentury.util.PayResult;
import com.NewCenturyHotels.NewCentury.util.PayUtil;
import com.NewCenturyHotels.NewCentury.util.PropertyUtil;
import com.NewCenturyHotels.NewCentury.util.SharedPreferencesHelper;
import com.NewCenturyHotels.NewCentury.util.StatusBarUtil;
import com.NewCenturyHotels.NewCentury.util.StatusBarUtils;
import com.NewCenturyHotels.NewCentury.view.Html5WebView;
import com.NewCenturyHotels.NewCentury.wxapi.Constants;
import com.alipay.sdk.app.PayTask;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.google.gson.Gson;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.AppInfo;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.unionpay.UPPayAssistEx;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

/**
 * 嵌入web页面
 */
public class Html5Activity extends AppCompatActivity {

    private static final int SDK_PAY_FLAG = 1;
    private final int CAMERA_CODE = 2;
    File file;
    private IWXAPI api;
    private Html5WebView webView;
    RelativeLayout loading;
    Bitmap bm;

    GpsInfo gpsInfo;
    SharedPreferencesHelper sharedPreferencesHelper;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                stopLoading();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_html5);

        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(Html5Activity.this);
        Boolean logined = (Boolean) sharedPreferencesHelper.get(SharedPref.LOGINED,false);
        if(!logined){
            Intent intent = new Intent(Html5Activity.this,SignInByCodeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        amapGps();
        StatusBarUtils.with(this).init();
        initViews();
    }

    void initViews(){
        LinearLayout l = (LinearLayout) findViewById(R.id.html5_linear);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        webView = new Html5WebView(getApplicationContext());
        webView.setLayoutParams(params);
        l.addView(webView);

        loading = (RelativeLayout) findViewById(R.id.html5_loading);

        sharedPreferencesHelper = new SharedPreferencesHelper(Html5Activity.this);

        String url = getIntent().getStringExtra("url");

        if(!(Boolean) sharedPreferencesHelper.get(SharedPref.HTML5_LOGINED,false)){
            Properties p = null;
            try {
                p = PropertyUtil.getConfigProperties(getApplicationContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
            String channel = p.getProperty("channel").toString();
            url = Const.APP_ROOT + Const.MIDDLE + "?token="+ HttpHelper.getAuthorization()
                    +"&appkey=" + ApiSignUtil.getAppkey() + "&appSecret=" + ApiSignUtil.getAppSecret()
                    +"&redirectUrl=" + url + "&v="+ ApiSignUtil.getV() + "&channel=" + channel;
        }else{
            if(!url.contains("http")){
                url = Const.APP_ROOT + url;
            }
        }

        webView.addJavascriptInterface(new JsInterFace(), "android");
        webView.loadUrl(url);
//        webView.loadUrl("http://www.baidu.com");
//        webView.loadUrl("file:///android_asset/demo.html");

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                startLoading();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        try {
                            sleep(500);
                            handler.sendEmptyMessage(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });

        //根据状态栏颜色来决定状态栏文字用黑色还是白色
        StatusBarUtil.setStatusBarMode(this, true, R.color.white);
    }

    //左滑返回
    int startX;
    int endX;
    int scrollSize = 200;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = (int) event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        endX = (int) event.getX();
                        if(endX>startX && webView.canGoBack() && endX-startX>scrollSize){
                            webView.goBack();
                        }else if(endX<startX &&webView.canGoForward() && startX-endX>scrollSize){
                            webView.goForward();
                        }else if (endX>startX && !webView.canGoBack() && endX-startX>scrollSize){
                            finish();
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        return super.dispatchTouchEvent(ev);
    }

    void startLoading(){
        loading.setVisibility(View.VISIBLE);
        loading.setOnClickListener(null);
    }

    void stopLoading(){
        loading.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //拍照获取图片
        if (requestCode == CAMERA_CODE) {
            String path = file.getAbsolutePath().toString();
            webView.loadUrl("javascript:setImage('" + path + "')");
            //通知系统更新相册
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(file);
            intent.setData(uri);
            this.sendBroadcast(intent);
            return;
        }

        //QQ和微博返回值
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);

        /*************************************************
         * 步骤3：处理银联手机支付控件返回的支付结果
         ************************************************/
        if (data == null) {
            return;
        }

        String msg = "";
        /*
         * 支付控件返回字符串:success、fail、cancel 分别代表支付成功，支付失败，支付取消
         */
        String str = data.getExtras().getString("pay_result");
        if(str != null){
            if (str.equalsIgnoreCase("success")) {

                // 如果想对结果数据验签，可使用下面这段代码，但建议不验签，直接去商户后台查询交易结果
                // result_data结构见c）result_data参数说明
                if (data.hasExtra("result_data")) {
                    String result = data.getExtras().getString("result_data");
                    try {
                        JSONObject resultJson = new JSONObject(result);
                        String sign = resultJson.getString("sign");
                        String dataOrg = resultJson.getString("data");
                        // 此处的verify建议送去商户后台做验签
                        // 如要放在手机端验，则代码必须支持更新证书
                        boolean ret = true;//verify(dataOrg, sign, mMode);
                        if (ret) {
                            // 验签成功，显示支付结果
                            msg = "支付成功！";
                        } else {
                            // 验签失败
                            msg = "支付失败！";
                        }
                    } catch (JSONException e) {
                    }
                }
                // 结果result_data为成功时，去商户后台查询一下再展示成功
                msg = "支付成功！";
            } else if (str.equalsIgnoreCase("fail")) {
                msg = "支付失败！";
            } else if (str.equalsIgnoreCase("cancel")) {
                msg = "用户取消了支付";
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("支付结果通知");
            builder.setMessage(msg);
            builder.setInverseBackgroundForced(true);
            // builder.setCustomTitle();
            builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }

    }

    //权限申请回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int i = 0;i < permissions.length; i++){
            switch (permissions[i]){
                case Manifest.permission.CALL_PHONE:
                    if(grantResults[i] == 0){
    
                    }else{
                        Toast.makeText(Html5Activity.this, "请同意权限申请，以继续下一步操作！", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    }

    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {

                    Double longitude = amapLocation.getLongitude();
                    Double latitude = amapLocation.getLatitude();
                    String city = amapLocation.getCity();

                    gpsInfo = new GpsInfo();
                    gpsInfo.setLatitude(latitude);
                    gpsInfo.setLongitude(longitude);
                    gpsInfo.setCityName(city);

                }else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError","location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());
                }
            }
        }
    };
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
        switch (msg.what) {
            case SDK_PAY_FLAG: {
                @SuppressWarnings("unchecked")
                PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                /**
                 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                 */
                String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                String resultStatus = payResult.getResultStatus();
                // 判断resultStatus 为9000则代表支付成功
                if (TextUtils.equals(resultStatus, "9000")) {
                    // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                    Toast.makeText(Html5Activity.this, "支付成功", Toast.LENGTH_SHORT).show();
                } else {
                    // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                    Toast.makeText(Html5Activity.this, "支付失败", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
                break;
        }
        }
    };

    void amapGps(){
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();


        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//            //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
//            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        //设置定位模式为AMapLocationMode.Device_Sensors，仅设备模式。
//            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
        mLocationOption.setHttpTimeOut(20000);

        //获取一次定位结果：
        //该方法默认为false。
//            mLocationOption.setOnceLocation(true);

        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(true);

        //连续定位
        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
        mLocationOption.setInterval(10000);

        //关闭缓存机制
        mLocationOption.setLocationCacheEnable(false);

        /**
         * 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
         */
        mLocationOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Transport);
        if(null != mLocationClient){
            mLocationClient.setLocationOption(mLocationOption);
            //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
            mLocationClient.stopLocation();
            mLocationClient.startLocation();
        }

    }

    public class JsInterFace {

        @JavascriptInterface
        public String appInit() throws IOException {
            String ret = "";

            Properties p = PropertyUtil.getConfigProperties(getApplicationContext());
            String channel = p.getProperty("channel").toString();

            AppInitJs appInitJs = new AppInitJs();
            appInitJs.setAppkey(ApiSignUtil.getAppkey());
            appInitJs.setAppSecret(ApiSignUtil.getAppSecret());
            appInitJs.setChannel(channel);
            appInitJs.setV(ApiSignUtil.getV());
            appInitJs.setToken(HttpHelper.getAuthorization());

            Gson gson = new Gson();
            ret = gson.toJson(appInitJs);

            return ret;
        }

        @JavascriptInterface
        public void loginSuccess(){
            sharedPreferencesHelper.put(SharedPref.HTML5_LOGINED,true);
            Toast.makeText(Html5Activity.this,"同步登录成功",Toast.LENGTH_LONG).show();
        }

        @JavascriptInterface
        public void getBack(String url){
            Intent intent = new Intent(Html5Activity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }

        //获取设备号
        @JavascriptInterface
        public String getDevice() {

            String tac = "";
            try {

                ActivityCompat.requestPermissions(Html5Activity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);

                final TelephonyManager manager = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(Html5Activity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return "";
                }

                if (manager.getDeviceId() == null || manager.getDeviceId().equals("")) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        tac = manager.getDeviceId(0);
                    }
                } else {
                    tac = manager.getDeviceId();
                }

                Toast.makeText(Html5Activity.this, "手机设备号：" + tac, Toast.LENGTH_LONG).show();

            } catch (Exception e) {

            }

            Gson gson = new Gson();
            DeviceInfo info = new DeviceInfo();
            info.setDeviceNum(tac);

            return gson.toJson(info);
        }

        @JavascriptInterface
        public void wxpay(String tradeNo) {

            api = WXAPIFactory.createWXAPI(Html5Activity.this, Constants.APP_ID);//wxb4ba3c02aa476ea1

            api.registerApp(Constants.APP_ID);
            try {
                PayUtil p = new PayUtil();
                JSONObject content = p.pay("WEIXIN");
                if (content != null) {
                    Log.e("get server pay params:", content.toString());
                    JSONObject json = new JSONObject(content.getString("data"));
                    if (null != json) {
                        PayReq req = new PayReq();
                        //req.appId = "wxf8b4f85f3a794e77";  // 测试用appId
                        req.appId = json.getString("appid");
                        req.partnerId = json.getString("partnerid");
                        req.prepayId = json.getString("prepayid");
                        req.nonceStr = json.getString("noncestr");
                        req.timeStamp = json.getString("timestamp");
                        req.packageValue = json.getString("package");
                        req.sign = json.getString("sign");
                        req.extData = "app data"; // optional
                        Toast.makeText(Html5Activity.this, "正常调起支付", Toast.LENGTH_SHORT).show();
                        // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
                        api.sendReq(req);
                    } else {
                        Log.d("PAY_GET", "返回错误" + content.getString("msg"));
                        Toast.makeText(Html5Activity.this, "返回错误" + content.getString("msg"), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("PAY_GET", "服务器请求错误");
                    Toast.makeText(Html5Activity.this, "服务器请求错误", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("PAY_GET", "异常：" + e.getMessage());
                Toast.makeText(Html5Activity.this, "异常：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @JavascriptInterface
        public void alipay(String tradeNo) {
            try {
                PayUtil p = new PayUtil();
                JSONObject content = p.pay("ALIPAY");
                if (content != null) {
                    Log.e("get server pay params:", content.toString());
                    final String data = content.getString("data");
                    Map<String, Object> map = p.convertToMap(data);
                    ActivityCompat.requestPermissions(Html5Activity.this,
                            new String[]{Manifest.permission.READ_PHONE_STATE},
                            1);
                    if (null != map) {
                        Runnable payRunnable = new Runnable() {
                            @Override
                            public void run() {
                                PayTask alipay = new PayTask(Html5Activity.this);
                                Map<String, String> result = alipay.payV2(data, true);
                                Log.i("msp", result.toString());

                                Message msg = new Message();
                                msg.what = SDK_PAY_FLAG;
                                msg.obj = result;
                                mHandler.sendMessage(msg);
                            }
                        };

                        Thread payThread = new Thread(payRunnable);
                        payThread.start();
                    } else {
                        Log.d("PAY_GET", "返回错误" + content.getString("msg"));
                        Toast.makeText(Html5Activity.this, "返回错误" + content.getString("msg"), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("PAY_GET", "服务器请求错误");
                    Toast.makeText(Html5Activity.this, "服务器请求错误", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("PAY_GET", "异常：" + e.getMessage());
                Toast.makeText(Html5Activity.this, "异常：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @JavascriptInterface
        public void unionpay(String tradeNo) {
            try {
                PayUtil p = new PayUtil();
                String tn = p.getUnionPayTn();
                if (null != tn && !tn.isEmpty()) {
                    UPPayAssistEx.startPay(Html5Activity.this, null, null, tn, "01");
                    Toast.makeText(Html5Activity.this, "获取数据：" + tn.toString(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(Html5Activity.this, "支付错误", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Log.e("PAY_GET", "异常：" + e.getMessage());
                Toast.makeText(Html5Activity.this, "异常：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @JavascriptInterface
        public String getChannel() {
            String ret = "";
            //读取配置文件信息
            try {
                Properties p = PropertyUtil.getConfigProperties(getApplicationContext());
                ret = p.getProperty("channel").toString();
                Toast.makeText(Html5Activity.this, ret, Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return ret;
        }

        @JavascriptInterface
        public void callCamera() {
            Intent intent = new Intent();
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addCategory(Intent.CATEGORY_DEFAULT);

            file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/DCIM/Camera/" + System.currentTimeMillis() + ".jpg");
            file.getParentFile().mkdirs();

            //改变Uri  注意和xml中的一致
            Uri uri = FileProvider.getUriForFile(Html5Activity.this, com.NewCenturyHotels.NewCentury.cons.AppInfo.FILE_AUTHORITY, file);
            //添加权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

            ActivityCompat.requestPermissions(Html5Activity.this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);

            startActivityForResult(intent, CAMERA_CODE);
        }

        @JavascriptInterface
        public void callPhone(String phoneNum) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            Uri data = Uri.parse("tel:" + phoneNum);
            intent.setData(data);
            if (ActivityCompat.checkSelfPermission(Html5Activity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            startActivity(intent);
        }

        @JavascriptInterface
        public void setCookies(String cookiesPath,String cookies) {
            Map<String, String> cookieMap = new HashMap<>();
            SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("cookiesPath", cookiesPath);
            editor.commit();

            String cookie = sp.getString("cookies", "");// 从SharedPreferences中获取整个Cookie串
            if(TextUtils.isEmpty(cookie) || cookie.compareTo(cookies) != 0){
                cookie = cookies;
                editor.putString("cookies",cookies);
            }

            if (!TextUtils.isEmpty(cookie)) {
                String[] cookieArray = cookie.split(";");// 多个Cookie是使用分号分隔的
                for (int i = 0; i < cookieArray.length; i++) {
                    int position = cookieArray[i].indexOf("=");// 在Cookie中键值使用等号分隔
                    String cookieName = cookieArray[i].substring(0, position);// 获取键
                    String cookieValue = cookieArray[i].substring(position + 1);// 获取值

                    String value = cookieName + "=" + cookieValue;// 键值对拼接成 value
                    Log.i("cookie", value);
                    CookieManager.getInstance().setCookie(HttpUtil.getDomain(cookiesPath), value);// 设置 Cookie
                }
            }
        }

        @JavascriptInterface
        public String bitmapToBase64(){
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.splash);
            String bmstr = Base64BitmapUtil.bitmapToBase64(bitmap);
            Log.i("bitmapToBase64", "onCreate: " + bmstr);
            return bmstr;
        }

        @JavascriptInterface
        public String bitmapCompress(){
            String base64 = "";

            FileInputStream fis = null;
            try {
                fis = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath()
                        +"/DCIM/Camera/IMG_20180327_152649.jpg");

                Bitmap bitmap  = BitmapFactory.decodeStream(fis);

                Bitmap bmNew = BitmapCompressHelper.compressImage3(bitmap);
                bmNew = BitmapCompressHelper.compressImage(bmNew);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmNew.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                Log.i("compressImage", "bitmapCompress: " + baos.toByteArray().length);

                bm = bmNew;
                base64 = Base64BitmapUtil.bitmapToBase64(bmNew);

                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        +"/ATest/IMG000004.jpg");
                if(!file.exists()){
                    file.getParentFile().mkdir();
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                bmNew.compress(Bitmap.CompressFormat.PNG,100,fos);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return base64;
        }

        @JavascriptInterface
        public String amapGps(){
            Gson gson = new Gson();
            return  gson.toJson(gpsInfo);
        }

        //分享到微信朋友
        @JavascriptInterface
        public void shareWx(String url,String title,String content){
            UMWeb web = new UMWeb("http://mobile.umeng.com/social");
            web.setTitle("This is web title");
            web.setThumb(new UMImage(Html5Activity.this, R.mipmap.logo));
            web.setDescription("my description");
            new ShareAction(Html5Activity.this).withMedia(web )
                    .setPlatform(SHARE_MEDIA.WEIXIN)
                    .setCallback(shareListener).share();
        }

        //分享到微信朋友圈
        @JavascriptInterface
        public void shareWxTimeline(String url,String title,String content){
            UMWeb web = new UMWeb("http://mobile.umeng.com/social");
            web.setTitle("This is web title");
            web.setThumb(new UMImage(Html5Activity.this, R.mipmap.logo));
            web.setDescription("my description");
            new ShareAction(Html5Activity.this).withMedia(web )
                    .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)
                    .setCallback(shareListener).share();
        }

        //分享到QQ
        @JavascriptInterface
        public void shareQQ(String url,String title,String content){
            UMWeb web = new UMWeb("http://mobile.umeng.com/social");
            web.setTitle("This is web title");
            web.setThumb(new UMImage(Html5Activity.this, R.mipmap.logo));
            web.setDescription("my description");
            new ShareAction(Html5Activity.this).withMedia(web)
                    .setPlatform(SHARE_MEDIA.QQ)
                    .setCallback(shareListener).share();
        }

        //分享到新浪微博
        @JavascriptInterface
        public void shareWeibo(String url,String title,String content){
            UMWeb web = new UMWeb("http://mobile.umeng.com/social");
            web.setTitle("This is web title");
            web.setThumb(new UMImage(Html5Activity.this, R.mipmap.logo));
            web.setDescription("my description");
            new ShareAction(Html5Activity.this).withMedia(web )
                    .setPlatform(SHARE_MEDIA.SINA)
                    .setCallback(shareListener).share();
        }

        //打开地图软件
        @JavascriptInterface
        public void showMap(String address){
            //根据地名打开地图应用显示，字符串要记得编码！！
            String encodedName = Uri.encode(address);
            Uri locationUri = Uri.parse("geo:0,0?q="+encodedName);
            //根据经纬度打开地图显示，?z=11表示缩放级别，范围为1-23
            //Uri locationUri = Uri.parse("geo:26.5789070770,106.7170012064?z=11");

            Intent intent = new Intent(Intent.ACTION_VIEW);
            Intent chooser = Intent.createChooser(intent, "请选择地图软件");
            intent.setData(locationUri);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(chooser);
            }
        }
    }

    //监听友盟分享结果
    private UMShareListener shareListener = new UMShareListener() {
        /**
         * @descrption 分享开始的回调
         * @param platform 平台类型
         */
        @Override
        public void onStart(SHARE_MEDIA platform) {
        }
        /**
         * @descrption 分享成功的回调
         * @param platform 平台类型
         */
        @Override
        public void onResult(SHARE_MEDIA platform) {
            Toast.makeText(Html5Activity.this,"成功了",Toast.LENGTH_LONG).show();
        }
        /**
         * @descrption 分享失败的回调
         * @param platform 平台类型
         * @param t 错误原因
         */
        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            Toast.makeText(Html5Activity.this,"失败"+t.getMessage(),Toast.LENGTH_LONG).show();
        }
        /**
         * @descrption 分享取消的回调
         * @param platform 平台类型
         */
        @Override
        public void onCancel(SHARE_MEDIA platform) {
            Toast.makeText(Html5Activity.this,"取消了",Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()){
            webView.goBack();
        }else{
            super.onBackPressed();
        }
    }
}
