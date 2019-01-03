package com.NewCenturyHotels.NewCentury.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.cons.CheckCodeTypeEnum;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;
import com.NewCenturyHotels.NewCentury.req.SendMobileCheckReq;
import com.NewCenturyHotels.NewCentury.req.SignByCodeReq;
import com.NewCenturyHotels.NewCentury.req.VerifyCheckReq;
import com.NewCenturyHotels.NewCentury.util.HttpHelper;
import com.NewCenturyHotels.NewCentury.util.SharedPreferencesHelper;
import com.NewCenturyHotels.NewCentury.util.StatusBarUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import cn.tongdun.android.shell.FMAgent;
import cn.tongdun.android.shell.exception.FMException;
import cn.tongdun.android.shell.inter.FMCallback;
import cn.tongdun.captchalib.CaptchaConfig;
import cn.tongdun.captchalib.FMCaptchaCallBack;
import cn.tongdun.captchalib.TDBindCaptcha;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 登录输入验证码
 */
public class SignCodeActivity extends SwipeBackActivity implements View.OnClickListener{

    RelativeLayout scode_back;
    TextView scode_notice;
    TextView scode_get;
    EditText scode_et_1;
    EditText scode_et_2;
    EditText scode_et_3;
    EditText scode_et_4;
    Intent intent;
    LinearLayout statusBar;

    final String TAG = SignCodeActivity.class.getName();

    //同盾
    private TDBindCaptcha captcha;
    String blackBox = "";
    String validateToken = "";

    //判断是登录还是注册
    Boolean isLogin = true;
    //手机号
    String mobile = "";
    //验证码
    String checkCode = "";
    String checkCodeToken = "";

    int often = 60;
    //倒计时
    Thread thread;

    RelativeLayout loading;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try{
                stopLoading();
                if(msg.what == 1){//倒计时
                    String str = "";
                    if(scode_get.getText().length() == 4){
                        return;
                    }
                    if(scode_get.getText().length() == 9){
                        str = scode_get.getText().toString().substring(5,7);
                    }else{
                        str = scode_get.getText().toString().substring(5,6);
                    }

                    Integer sec = (Integer.parseInt(str)-1);
                    if(sec == 0){
                        scode_get.setEnabled(true);
                        str = "重新发送";
                        scode_get.setText(str);
                        scode_get.setBackgroundResource(R.drawable.btn_finish);
                        often = 60;
                    }else{
                        str = "重新发送(" + sec +"s)";
                        scode_get.setText(str);
                        scode_get.setBackgroundResource(R.drawable.btn_finish_inactive);
                    }

                }else if(msg.what == 2){//发送验证码
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        JsonObject data = jo.getAsJsonObject("data");
                        checkCodeToken = data.get("checkCodeToken").getAsString();
                        int tmp = data.get("often").getAsInt();
                        if(tmp > 0){
                            often = tmp;
                        }
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(SignCodeActivity.this);
                    }else{
                        Toast.makeText(SignCodeActivity.this,message,Toast.LENGTH_SHORT).show();
                    }

                }else if(msg.what == 3){//验证码登录
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){

                        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(SignCodeActivity.this);
                        String token = jo.getAsJsonObject("data").get("token").getAsString();
                        sharedPreferencesHelper.put(SharedPref.TOKEN,token);
                        HttpHelper.setAuthorization(token);
                        sharedPreferencesHelper.put(SharedPref.LOGINED,true);
                        intent = new Intent(SignCodeActivity.this,MainActivity.class);
                        startActivity(intent);
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(SignCodeActivity.this);
                    }
                    else{
                        Toast.makeText(SignCodeActivity.this,message,Toast.LENGTH_SHORT).show();
                    }
                }else  if(msg.what == 4){//校验验证码
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        intent = new Intent(SignCodeActivity.this,SignUpAppendActivity.class);
                        intent.putExtra("mobile",mobile);
                        intent.putExtra("checkCode",checkCode);
                        intent.putExtra("checkCodeToken",checkCodeToken);
                        intent.putExtra("validateToken",validateToken);
                        intent.putExtra("blackBox",blackBox);
                        startActivity(intent);
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(SignCodeActivity.this);
                    }else{
                        Toast.makeText(SignCodeActivity.this,message,Toast.LENGTH_SHORT).show();
                    }
                }
            }catch (Exception e){
                Log.e(TAG, "Exception: " + e.getMessage());
                Toast.makeText(SignCodeActivity.this,"连接超时",Toast.LENGTH_LONG).show();
            }
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_code);
        StatusBarUtils.with(this).init();
        initData();
        initComponent();
        initEvent();

        new Thread(){
            @Override
            public void run() {
                try {
                    initTongdun();
                } catch (FMException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        thread.interrupt();
        thread = null;
    }

    void initData(){
        isLogin = getIntent().getBooleanExtra("isLogin",false);
        mobile = getIntent().getStringExtra("mobile");
        checkCodeToken = getIntent().getStringExtra("checkCodeToken");
        int tmp = getIntent().getIntExtra("often",60);
        if(tmp > 0){
            often = tmp;
        }
        validateToken = getIntent().getStringExtra("validateToken");
        blackBox = getIntent().getStringExtra("blackBox");
    }

    void initComponent(){

        scode_back = (RelativeLayout) findViewById(R.id.scode_back);
        scode_notice = (TextView) findViewById(R.id.scode_notice);
        scode_get = (TextView) findViewById(R.id.scode_get);
        scode_et_1 = (EditText) findViewById(R.id.scode_et_1);
        scode_et_2 = (EditText) findViewById(R.id.scode_et_2);
        scode_et_3 = (EditText) findViewById(R.id.scode_et_3);
        scode_et_4 = (EditText) findViewById(R.id.scode_et_4);

        loading = (RelativeLayout) findViewById(R.id.scode_loading);
        scode_notice.setText("验证码已发送至" + mobile);

        //调整通知栏高度
        statusBar = (LinearLayout) findViewById(R.id.scode_status_bar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this);
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        statusBar.setLayoutParams(layoutParams);
    }

    void startLoading(){
        loading.setVisibility(View.VISIBLE);
        loading.setOnClickListener(null);
    }

    void stopLoading(){
        loading.setVisibility(View.GONE);
    }

    void initEvent(){

        scode_back.setOnClickListener(this);
        scode_get.setOnClickListener(this);

        scode_et_1.requestFocus();
        scode_et_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() == 1){
                    scode_et_1.clearFocus();
                    scode_et_2.requestFocus();
                }
            }
        });

        scode_et_2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() == 1){
                    scode_et_2.clearFocus();
                    scode_et_3.requestFocus();
                }
            }
        });
        scode_et_3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() == 1){
                    scode_et_3.clearFocus();
                    scode_et_4.requestFocus();
                }
            }
        });
        scode_et_4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() == 1){
                    String code1 = scode_et_1.getText().toString().trim();
                    String code2 = scode_et_2.getText().toString().trim();
                    String code3 = scode_et_3.getText().toString().trim();
                    String code4 = scode_et_4.getText().toString().trim();
                    checkCode = code1 + code2 + code3 + code4;

                    //隐藏软键盘
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);

                    startLoading();
                    String url = "";
                    String json = "";
                    if(isLogin){
                        url = Const.SIGN_BY_CODE;
                        Gson gson = new Gson();
                        SignByCodeReq req = new SignByCodeReq();
                        req.setBlackBox(blackBox);
                        req.setCheckCode(checkCode);
                        req.setCheckCodeToken(checkCodeToken);
                        req.setMobile(mobile);
                        req.setValidateToken(validateToken);
                        req.setSource(Const.SOURCE_TYPE);
                        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(SignCodeActivity.this);
                        req.setDeviceId(sharedPreferencesHelper.get(SharedPref.DEVICE_TOKEN,"").toString());
                        json = gson.toJson(req);
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
                                Message msg = new Message();
                                msg.setData(bundle);
                                msg.what = 3;
                                handler.sendMessage(msg);
                            }
                        });
                    }else{
                        url = Const.VERIFY_CHECK_CODE;
                        String checkCodeType = CheckCodeTypeEnum.API_REGISTER_CODE_KEY.toString();
                        Gson gson = new Gson();
                        VerifyCheckReq req = new VerifyCheckReq();
                        req.setCheckCode(checkCode);
                        req.setCheckCodeToken(checkCodeToken);
                        req.setCheckCodeType(checkCodeType);
                        req.setMobile(mobile);
                        req.setEmail("");
                        json = gson.toJson(req);
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
                                Message msg = new Message();
                                msg.setData(bundle);
                                msg.what = 4;
                                handler.sendMessage(msg);
                            }
                        });
                    }
                }
            }
        });

        scode_et_2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL){
                    if(scode_et_2.getText().toString().isEmpty()){
                        scode_et_2.clearFocus();
                        scode_et_1.requestFocus();
                        scode_et_1.setText("");
                        scode_et_1.setSelection(scode_et_1.getText().length());
                    }else{
                        scode_et_2.setText("");
                    }

                }
                return false;
            }
        });
        scode_et_3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL){
                    if(scode_et_3.getText().toString().isEmpty()){
                        scode_et_3.clearFocus();
                        scode_et_2.requestFocus();
                        scode_et_2.setText("");
                        scode_et_2.setSelection(scode_et_2.getText().length());
                    }else{
                        scode_et_3.setText("");
                    }

                }
                return false;
            }
        });
        scode_et_4.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL){
                    if(scode_et_4.getText().toString().isEmpty()){
                        scode_et_4.clearFocus();
                        scode_et_3.requestFocus();
                        scode_et_3.setText("");
                        scode_et_3.setSelection(scode_et_3.getText().length());
                    }else{
                        scode_et_4.setText("");
                    }
                }
                return false;
            }
        });


        thread = new Thread(){
            @Override
            public void run() {

                while (--often > 0){
                    try {
                        sleep(1000);
                        handler.sendEmptyMessage(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        initSendCode();
    }

    void initSendCode(){
        scode_get.setEnabled(false);
        scode_get.setText(scode_get.getText()+"("+(often-1)+"s)");
        scode_get.setBackgroundResource(R.drawable.btn_finish_inactive);
        thread.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scode_back:
                finish();
                break;
            case R.id.scode_get:
                blackBox = FMAgent.onEvent(SignCodeActivity.this);
                Log.i(TAG, "blackBox: " + blackBox);
                captcha.verify();

                initSendCode();
                break;
        }
    }

    void initTongdun() throws FMException {
        //权限申请
        if(Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,  //必选
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,  //必选
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 100);
        }

        FMAgent.initWithCallback(this, FMAgent.ENV_SANDBOX, new FMCallback() {
            @Override
            public void onEvent(String blackbox) {
                // 注意这里不是主线程 请不要在这个函数里进行ui操作，否则可能会出现崩溃
                //tdBlackBox = blackbox;
                Log.i(TAG, "onEvent: " + blackbox);

            }
        });

        // 调用init进行初始化 FMCaptchaCallBack接口在第5项中
        captcha = TDBindCaptcha.init(this,
                new CaptchaConfig.Builder()
                        .appName("kaiyuan_and")//应用标识,由同盾提供
                        .partnerCode("kaiyuan")//合作方编码,由同盾提供
                        .tapToClose(true)//默认fasle
                        .openLog(true)//默认为false
                        .timeOut(6000)//默认5000ms,单位ms
                        .setLanguage(1)//设置语言类型，默认简体中文
                        .build(), new FMCaptchaCallBack(){
                    public void onReady() {}
                    public void onFailed(int va1, String var2) {
                        Log.i(TAG, "onFailed: " + va1 + " ," + var2);
                    }
                    public void onSuccess(String token) {
                        Log.i(TAG, "onSuccess: " + token);

                        validateToken = token;

                        String url = Const.SEND_MOBILE_CHECK;
                        String checkCodeType = "";
                        if(isLogin){
                            checkCodeType = CheckCodeTypeEnum.API_LOGIN_CODE_KEY.toString();
                        }else{
                            checkCodeType = CheckCodeTypeEnum.API_REGISTER_CODE_KEY.toString();
                        }
                        String messageContentType = Const.MESSAGE_CONTENT_TYPE_PHONE;
                        Gson gson = new Gson();
                        SendMobileCheckReq req = new SendMobileCheckReq();
                        req.setBlackBox(blackBox);
                        req.setCheckCodeType(checkCodeType);
                        req.setMobile(mobile);
                        req.setMessageContentType(messageContentType);
                        req.setValidateToken(validateToken);
                        String json = gson.toJson(req);
                        startLoading();
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
                                Message msg = new Message();
                                msg.setData(bundle);
                                msg.what = 2;
                                handler.sendMessage(msg);
                            }
                        });
                    }
                });

    }

}
