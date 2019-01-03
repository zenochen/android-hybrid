package com.NewCenturyHotels.NewCentury.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.bean.AppInitJs;
import com.NewCenturyHotels.NewCentury.bean.DeviceInfo;
import com.NewCenturyHotels.NewCentury.bean.GpsInfo;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.cons.PayWayEnum;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;
import com.NewCenturyHotels.NewCentury.req.ChargeReq;
import com.NewCenturyHotels.NewCentury.req.OrderPayReq;
import com.NewCenturyHotels.NewCentury.util.ApiSignUtil;
import com.NewCenturyHotels.NewCentury.util.HttpHelper;
import com.NewCenturyHotels.NewCentury.util.PayResult;
import com.NewCenturyHotels.NewCentury.util.PayUtil;
import com.NewCenturyHotels.NewCentury.util.PropertyUtil;
import com.NewCenturyHotels.NewCentury.util.SharedPreferencesHelper;
import com.NewCenturyHotels.NewCentury.util.StatusBarUtil;
import com.NewCenturyHotels.NewCentury.util.StatusBarUtils;
import com.NewCenturyHotels.NewCentury.view.CommomDialog;
import com.NewCenturyHotels.NewCentury.view.Html5WebView;
import com.NewCenturyHotels.NewCentury.wxapi.Constants;
import com.alipay.sdk.app.PayTask;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.umeng.socialize.shareboard.ShareBoardConfig;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;
import com.unionpay.UPPayAssistEx;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Properties;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 嵌入web页面
 */
public class Html5Activity extends AppCompatActivity {

    private static final int SDK_PAY_FLAG = 1;
    private IWXAPI api;
    private Html5WebView webView;
    RelativeLayout loading;
    RelativeLayout titleLayer;
    TextView tv_title;
    RelativeLayout close;
    Bitmap bm;

    GpsInfo gpsInfo;
    SharedPreferencesHelper sharedPreferencesHelper;
    String _phoneNum;
    Boolean needNotLogin;

    String _tradeNo = "";

    ShareAction mShareAction;
    UMShareListener mShareListener;

    //图片上传
    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private final static int FILE_CHOOSER_RESULT_CODE = 10000;

    final static String TAG =  Html5Activity.class.getName();

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try{
                stopLoading();
                if(msg.what == 0){
                    startLoading();
                }else if(msg.what == 2){//微信支付
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        api = WXAPIFactory.createWXAPI(Html5Activity.this, Constants.APP_ID);
                        api.registerApp(Constants.APP_ID);
                        if(_tradeNo != null){
                            sharedPreferencesHelper.put(SharedPref.TRADE_NO,_tradeNo);
                        }else{
                            sharedPreferencesHelper.put(SharedPref.TRADE_NO,"");
                        }

                        String json = jo.get("data").getAsString();
                        JSONObject jsonObject = new JSONObject(json);
                        PayReq req = new PayReq();
                        req.appId = (String) jsonObject.get("appid");
                        req.partnerId = (String) jsonObject.get("partnerid");
                        req.prepayId = (String) jsonObject.get("prepayid");
                        req.nonceStr = (String) jsonObject.get("noncestr");
                        req.timeStamp = (String) jsonObject.get("timestamp");
                        req.packageValue = (String) jsonObject.get("package");
                        req.sign = (String) jsonObject.get("sign");
                        req.extData = "app data";
                        api.sendReq(req);
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(Html5Activity.this);
                    }else{
                        Toast.makeText(Html5Activity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }else if(msg.what == 3){//支付宝支付
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        final String data = jo.get("data").getAsString();
                        PayUtil p = new PayUtil();
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
                                    Log.i("alipay", result.toString());

                                    Message msg = new Message();
                                    msg.what = SDK_PAY_FLAG;
                                    msg.obj = result;
                                    mHandler.sendMessage(msg);
                                }
                            };

                            Thread payThread = new Thread(payRunnable);
                            payThread.start();
                        }
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(Html5Activity.this);
                    }else{
                        Toast.makeText(Html5Activity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }else if(msg.what == 4){//银联支付
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        String tn = jo.get("data").getAsString();
                        if (null != tn && !tn.isEmpty()) {
                            UPPayAssistEx.startPay(Html5Activity.this, null, null, tn, "01");
                        } else {
                            Toast.makeText(Html5Activity.this, "银联支付错误", Toast.LENGTH_SHORT).show();
                        }
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(Html5Activity.this);
                    }else{
                        Toast.makeText(Html5Activity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }else if(msg.what == 5){
                    webView.loadUrl(Const.APP_ROOT + Const.ORDER_DETAIL + _tradeNo);
                }
            }catch (Exception e){
                Log.e(TAG, "Exception: " + e.getMessage());
                Toast.makeText(Html5Activity.this,"连接超时",Toast.LENGTH_LONG).show();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_html5);

        needNotLogin = getIntent().getBooleanExtra("needNotLogin",false);
        if(!needNotLogin){
            SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(Html5Activity.this);
            Boolean logined = (Boolean) sharedPreferencesHelper.get(SharedPref.LOGINED,false);
            if(!logined){
                Intent intent = new Intent(Html5Activity.this,SignInByCodeActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        }

        amapGps();
        StatusBarUtils.with(this).init();
        initViews();
    }

    private static class CustomShareListener implements UMShareListener {

        private WeakReference<Html5Activity> mActivity;

        private CustomShareListener(Html5Activity activity) {
            mActivity = new WeakReference(activity);
        }

        @Override
        public void onStart(SHARE_MEDIA platform) {

        }

        @Override
        public void onResult(SHARE_MEDIA platform) {
            if (platform != SHARE_MEDIA.MORE && platform != SHARE_MEDIA.SMS
                    && platform != SHARE_MEDIA.EMAIL
                    && platform != SHARE_MEDIA.FLICKR
                    && platform != SHARE_MEDIA.FOURSQUARE
                    && platform != SHARE_MEDIA.TUMBLR
                    && platform != SHARE_MEDIA.POCKET
                    && platform != SHARE_MEDIA.PINTEREST

                    && platform != SHARE_MEDIA.INSTAGRAM
                    && platform != SHARE_MEDIA.GOOGLEPLUS
                    && platform != SHARE_MEDIA.YNOTE
                    && platform != SHARE_MEDIA.EVERNOTE) {
                Toast.makeText(mActivity.get(), " 分享成功啦", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            if (platform != SHARE_MEDIA.MORE && platform != SHARE_MEDIA.SMS
                    && platform != SHARE_MEDIA.EMAIL
                    && platform != SHARE_MEDIA.FLICKR
                    && platform != SHARE_MEDIA.FOURSQUARE
                    && platform != SHARE_MEDIA.TUMBLR
                    && platform != SHARE_MEDIA.POCKET
                    && platform != SHARE_MEDIA.PINTEREST

                    && platform != SHARE_MEDIA.INSTAGRAM
                    && platform != SHARE_MEDIA.GOOGLEPLUS
                    && platform != SHARE_MEDIA.YNOTE
                    && platform != SHARE_MEDIA.EVERNOTE) {
                Toast.makeText(mActivity.get(), " 分享失败啦", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            Toast.makeText(mActivity.get(), " 分享取消了", Toast.LENGTH_SHORT).show();
        }
    }

    void initViews(){
        LinearLayout l = (LinearLayout) findViewById(R.id.html5_linear);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        webView = new Html5WebView(getApplicationContext());
        webView.setLayoutParams(params);
        l.addView(webView);

        loading = (RelativeLayout) findViewById(R.id.html5_loading);
        titleLayer = (RelativeLayout) findViewById(R.id.html5_title_layer);
        tv_title = (TextView) findViewById(R.id.html5_title);
        close = (RelativeLayout) findViewById(R.id.html5_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        sharedPreferencesHelper = new SharedPreferencesHelper(Html5Activity.this);

        String payWays = getIntent().getStringExtra("payWays");
        String url = getIntent().getStringExtra("url");
        _tradeNo = getIntent().getStringExtra("tradeNo");

        if(!url.contains("http")){
            titleLayer.setVisibility(View.GONE);
        }else{
            titleLayer.setVisibility(View.VISIBLE);
        }

        if(!(Boolean) sharedPreferencesHelper.get(SharedPref.HTML5_LOGINED,false)){
            if(!url.contains("http")){
                Properties p = null;
                try {
                    p = PropertyUtil.getConfigProperties(getApplicationContext());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String channel = p.getProperty("channel").toString();
                if(url.contains(Const.PAY)){
                    url = Const.APP_ROOT + Const.MIDDLE + "?token="+ HttpHelper.getAuthorization()
                            +"&appkey=" + ApiSignUtil.getAppkey() + "&appSecret=" + ApiSignUtil.getAppSecret()
                            +"&redirectUrl=" + url + "&v="+ ApiSignUtil.getV() + "&channel=" + channel + "&paytype=" + payWays;
                }else{
                    url = Const.APP_ROOT + Const.MIDDLE + "?token="+ HttpHelper.getAuthorization()
                            +"&appkey=" + ApiSignUtil.getAppkey() + "&appSecret=" + ApiSignUtil.getAppSecret()
                            +"&redirectUrl=" + url + "&v="+ ApiSignUtil.getV() + "&channel=" + channel;
                }
            }
        }else{
            if(url.contains(Const.PAY)){
                url = Const.APP_ROOT + url + "&type=" + payWays;;
            }else{
                if(!url.contains("http")){
                    url = Const.APP_ROOT + url;
                }
            }

        }

        webView.addJavascriptInterface(new JsInterFace(), "android");
        webView.loadUrl(url);
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
                webView.getSettings().setBlockNetworkImage(false);
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        try {
                            sleep(1000);
                            handler.sendEmptyMessage(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });

        webView.setWebChromeClient(new WebChromeClient(){
            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> valueCallback) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            // For Android  >= 3.0
            public void openFileChooser(ValueCallback valueCallback, String acceptType) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            //For Android  >= 4.1
            public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            // For Android >= 5.0
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                uploadMessageAboveL = filePathCallback;
                openImageChooserActivity();
                return true;
            }
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                tv_title.setText(title);
            }
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });

        //根据状态栏颜色来决定状态栏文字用黑色还是白色
        StatusBarUtil.setStatusBarMode(this, true, R.color.white);
    }

    //打开图库
    private void openImageChooserActivity() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return;
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }

    //左滑返回
    int startX;
    int endX;
    int startY;
    int endY;
    int scrollSize = 250;
    int scrollSizeY = 150;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        endX = (int) event.getX();
                        endY = (int) event.getY();
                        if(endX>startX && webView.canGoBack() && endX-startX>scrollSize && Math.abs(endY-startY) < scrollSizeY){
                            webView.goBack();
                        }else if (endX>startX && !webView.canGoBack() && endX-startX>scrollSize && Math.abs(endY-startY) < scrollSizeY){
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
        //选择上传图片
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessage && null == uploadMessageAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (uploadMessage != null) {
                uploadMessage.onReceiveValue(result);
                uploadMessage = null;
            }
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
        String mMode = "01";//"00" - 启动银联正式环境 "01" - 连接银联测试环境
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
                        boolean ret = verify(dataOrg, sign, mMode);
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

            Toast.makeText(Html5Activity.this,msg,Toast.LENGTH_LONG).show();
        }

    }

    private boolean verify(String msg, String sign64, String mode) {
        // 此处的verify，商户需送去商户后台做验签
        return true;
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
                    if(!_tradeNo.isEmpty()){
                        webView.loadUrl(Const.APP_ROOT + Const.ORDER_DETAIL + _tradeNo);
                    }
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
        public void reLogin(){
            SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(Html5Activity.this);
            sharedPreferencesHelper.put(SharedPref.LOGINED,false);
            sharedPreferencesHelper.put(SharedPref.HTML5_LOGINED,false);
            sharedPreferencesHelper.put(SharedPref.TOKEN,"");
            HttpHelper.setAuthorization("");
            Toast.makeText(Html5Activity.this,"请重新登录",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Html5Activity.this,MainActivity.class);
            intent.putExtra("reLogin",true);
            startActivity(intent);
        }

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
            if(!HttpHelper.getAuthorization().isEmpty()){
                sharedPreferencesHelper.put(SharedPref.HTML5_LOGINED,true);
            }
        }

        //返回原生
        @JavascriptInterface
        public void backNative(){
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
        public void orderDetail(String tradeNo){
            _tradeNo = tradeNo;
            handler.sendEmptyMessage(5);
        }

        @JavascriptInterface
        public void charge(final String payWay, final String amount){
            _tradeNo = null;
            handler.sendEmptyMessage(0);
            new Thread(){
                @Override
                public void run() {
                    String url = Const.CHARGE_TRADE;
                    ChargeReq req = new ChargeReq();
                    req.setRechargeAmount(amount);
                    req.setRechargePayWay(payWay);
                    Gson gson = new Gson();
                    String json = gson.toJson(req);
                    HttpHelper.sendOkHttpPost(url, json, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e(TAG, "onFailure: " + e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String body = response.body().string();
                            Log.i(TAG, "onResponse: " + body);
                            Bundle bundle = new Bundle();
                            bundle.putString("ret",body);
                            android.os.Message msg = new android.os.Message();
                            msg.setData(bundle);
                            switch (payWay){
                                case "Alipay":
                                    msg.what = 3;
                                    break;
                                case "Wxpay":
                                    msg.what = 2;
                                    break;
                                case "UnionPay":
                                    msg.what = 4;
                                    break;
                            }

                            handler.sendMessage(msg);
                        }
                    });
                }
            }.start();
        }

        @JavascriptInterface
        public void wxpay(final String tradeNo) {
            _tradeNo = tradeNo;
            handler.sendEmptyMessage(0);
            new Thread(){
                @Override
                public void run() {
                    String url = Const.ORDER_PAY;
                    OrderPayReq req = new OrderPayReq();
                    req.setPayWay(PayWayEnum.WEIXIN.toString());
                    req.setTradeNo(tradeNo);
                    Gson gson = new Gson();
                    String json = gson.toJson(req);
                    HttpHelper.sendOkHttpPost(url, json, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e(TAG, "onFailure: " + e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String body = response.body().string();
                            Log.i(TAG, "onResponse: " + body);
                            Bundle bundle = new Bundle();
                            bundle.putString("ret",body);
                            android.os.Message msg = new android.os.Message();
                            msg.setData(bundle);
                            msg.what = 2;
                            handler.sendMessage(msg);
                        }
                    });
                }
            }.start();
        }

        @JavascriptInterface
        public void alipay(final String tradeNo) {
            _tradeNo = tradeNo;
            handler.sendEmptyMessage(0);
            new Thread(){
                @Override
                public void run() {
                    String url = Const.ORDER_PAY;
                    OrderPayReq req = new OrderPayReq();
                    req.setPayWay(PayWayEnum.ALIPAY.toString());
                    req.setTradeNo(tradeNo);
                    Gson gson = new Gson();
                    String json = gson.toJson(req);
                    HttpHelper.sendOkHttpPost(url, json, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e(TAG, "onFailure: " + e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String body = response.body().string();
                            Log.i(TAG, "onResponse: " + body);
                            Bundle bundle = new Bundle();
                            bundle.putString("ret",body);
                            android.os.Message msg = new android.os.Message();
                            msg.setData(bundle);
                            msg.what = 3;
                            handler.sendMessage(msg);
                        }
                    });
                }
            }.start();
        }

        @JavascriptInterface
        public void unionpay(final String tradeNo) {
            _tradeNo = tradeNo;
            handler.sendEmptyMessage(0);
            new Thread(){
                @Override
                public void run() {
                    String url = Const.ORDER_PAY;
                    OrderPayReq req = new OrderPayReq();
                    req.setPayWay(PayWayEnum.UNIONPAY.toString());
                    req.setTradeNo(tradeNo);
                    Gson gson = new Gson();
                    String json = gson.toJson(req);
                    HttpHelper.sendOkHttpPost(url, json, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e(TAG, "onFailure: " + e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String body = response.body().string();
                            Log.i(TAG, "onResponse: " + body);
                            Bundle bundle = new Bundle();
                            bundle.putString("ret",body);
                            android.os.Message msg = new android.os.Message();
                            msg.setData(bundle);
                            msg.what = 4;
                            handler.sendMessage(msg);
                        }
                    });
                }
            }.start();
        }

        @JavascriptInterface
        public void callPhone(final String phoneNum) {
            _phoneNum = phoneNum;
            CommomDialog dialog = new CommomDialog(Html5Activity.this, R.style.dialog, "呼叫：" + phoneNum, new CommomDialog.OnCloseListener() {
                @Override
                public void onClick(Dialog dialog, boolean confirm) {
                    if(confirm){
                        dialog.dismiss();
                        if (ActivityCompat.checkSelfPermission(Html5Activity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            ActivityCompat.requestPermissions(Html5Activity.this, new String[]{
                                    Manifest.permission.CALL_PHONE
                            }, 100);
                            return;
                        }
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        Uri data = Uri.parse("tel:" + phoneNum);
                        intent.setData(data);
                        startActivity(intent);
                    }
                }
            });
            dialog.show();
        }

        @JavascriptInterface
        public String amapGps(){
            Gson gson = new Gson();
            return  gson.toJson(gpsInfo);
        }

        @JavascriptInterface
        public void shareClick(final String title, final String desc, final String link, final String imgUrl){
            mShareListener = new CustomShareListener(Html5Activity.this);
            mShareAction = new ShareAction(Html5Activity.this).setDisplayList(
                    SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.QQ)
                    .setShareboardclickCallback(new ShareBoardlistener() {
                        @Override
                        public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                            UMWeb web = new UMWeb(link);
                            web.setTitle(title);
                            web.setDescription(desc);
                            web.setThumb(new UMImage(Html5Activity.this, imgUrl));
                            new ShareAction(Html5Activity.this).withMedia(web)
                                    .setPlatform(share_media)
                                    .setCallback(mShareListener)
                                    .share();

                        }
                    });
            ShareBoardConfig config = new ShareBoardConfig();
            config.setMenuItemBackgroundShape(ShareBoardConfig.BG_SHAPE_NONE);
            mShareAction.open(config);
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

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()){
            webView.goBack();
        }else{
            super.onBackPressed();
        }
    }
}
