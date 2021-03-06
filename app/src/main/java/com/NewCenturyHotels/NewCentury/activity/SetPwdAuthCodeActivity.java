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
import com.NewCenturyHotels.NewCentury.req.SendMobileCheckReq;
import com.NewCenturyHotels.NewCentury.req.VerifyCheckReq;
import com.NewCenturyHotels.NewCentury.util.HttpHelper;
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
 * 身份认证输入验证码
 */
public class SetPwdAuthCodeActivity extends SwipeBackActivity implements View.OnClickListener{

    RelativeLayout sp_auth_code_back;
    TextView sp_auth_code_notice;
    TextView sp_auth_code_submit;
    EditText sp_auth_code_et_1;
    EditText sp_auth_code_et_2;
    EditText sp_auth_code_et_3;
    EditText sp_auth_code_et_4;
    Intent intent;
    LinearLayout statusBar;

    final String TAG = SetPwdAuthCodeActivity.class.getName();

    //同盾
    private TDBindCaptcha captcha;
    String blackBox = "";
    String validateToken = "";

    //发送验证码类型
    String checkCodeType = "";
    //输入
    String mobileOrEmail = "";
    //输入的验证码
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
                if(msg.what == 1){//发送验证码倒计时
                    String str = "";
                    if(sp_auth_code_submit.getText().length() == 4){
                        return;
                    }
                    if(sp_auth_code_submit.getText().length() == 9){
                        str = sp_auth_code_submit.getText().toString().substring(5,7);
                    }else{
                        str = sp_auth_code_submit.getText().toString().substring(5,6);
                    }

                    Integer sec = (Integer.parseInt(str)-1);
                    if(sec == 0){
                        sp_auth_code_submit.setEnabled(true);
                        str = "重新发送";
                        sp_auth_code_submit.setText(str);
                        sp_auth_code_submit.setBackgroundResource(R.drawable.btn_finish);
                        often = 60;
                    }else{
                        str = "重新发送(" + sec +"s)";
                        sp_auth_code_submit.setText(str);
                        sp_auth_code_submit.setBackgroundResource(R.drawable.btn_finish_inactive);
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
                        often = data.get("often").getAsInt();
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(SetPwdAuthCodeActivity.this);
                    }else{
                        Toast.makeText(SetPwdAuthCodeActivity.this,message,Toast.LENGTH_SHORT).show();
                    }
                }else if(msg.what == 3){//验证码验证
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        intent = new Intent(SetPwdAuthCodeActivity.this,SetPwdActivity.class);
                        intent.putExtra("mobileOrEmail",mobileOrEmail);
                        intent.putExtra("checkCodeToken",checkCodeToken);
                        intent.putExtra("checkCode",checkCode);
                        intent.putExtra("validateToken",validateToken);
                        intent.putExtra("blackBox",blackBox);
                        startActivity(intent);
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(SetPwdAuthCodeActivity.this);
                    }else{
                        Toast.makeText(SetPwdAuthCodeActivity.this,message,Toast.LENGTH_LONG).show();
                    }
                }

            }catch (Exception e){
                Log.e(TAG, "Exception: " + e.getMessage());
                Toast.makeText(SetPwdAuthCodeActivity.this,"连接超时",Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pwd_auth_code);
        StatusBarUtils.with(this).init();
        initData();
        initComponent();
        initEvent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        thread.interrupt();
        thread = null;
    }

    void initData(){
        blackBox = getIntent().getStringExtra("blackBox");
        validateToken = getIntent().getStringExtra("validateToken");
        mobileOrEmail = getIntent().getStringExtra("mobileOrEmail");
        checkCodeToken = getIntent().getStringExtra("checkCodeToken");
        often = getIntent().getIntExtra("often",60);
        often = 60;
        checkCodeType = CheckCodeTypeEnum.API_UPDATE_PASSWORD_CODE_KEY.toString();

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

    void initComponent(){

        sp_auth_code_back = (RelativeLayout) findViewById(R.id.sp_auth_code_back);
        sp_auth_code_notice = (TextView) findViewById(R.id.sp_auth_code_notice);
        sp_auth_code_submit = (TextView) findViewById(R.id.sp_auth_code_submit);
        sp_auth_code_et_1 = (EditText) findViewById(R.id.sp_auth_code_et_1);
        sp_auth_code_et_2 = (EditText) findViewById(R.id.sp_auth_code_et_2);
        sp_auth_code_et_3 = (EditText) findViewById(R.id.sp_auth_code_et_3);
        sp_auth_code_et_4 = (EditText) findViewById(R.id.sp_auth_code_et_4);
        loading = (RelativeLayout) findViewById(R.id.sp_auth_code_loading);

        sp_auth_code_notice.setText("验证码已发送至" + mobileOrEmail);

        //调整通知栏高度
        statusBar = (LinearLayout) findViewById(R.id.sp_auth_code_status_bar);
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

        sp_auth_code_back.setOnClickListener(this);
        sp_auth_code_submit.setOnClickListener(this);
        sp_auth_code_et_1.requestFocus();
        sp_auth_code_et_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() == 1){
                    sp_auth_code_et_1.clearFocus();
                    sp_auth_code_et_2.requestFocus();
                }
            }
        });

        sp_auth_code_et_2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() == 1){
                    sp_auth_code_et_2.clearFocus();
                    sp_auth_code_et_3.requestFocus();
                }
            }
        });
        sp_auth_code_et_3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() == 1){
                    sp_auth_code_et_3.clearFocus();
                    sp_auth_code_et_4.requestFocus();
                }
            }
        });
        sp_auth_code_et_4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() == 1){
                    String code1 = sp_auth_code_et_1.getText().toString().trim();
                    String code2 = sp_auth_code_et_2.getText().toString().trim();
                    String code3 = sp_auth_code_et_3.getText().toString().trim();
                    String code4 = sp_auth_code_et_4.getText().toString().trim();
                    checkCode = code1 + code2 + code3 + code4;

                    //隐藏软键盘
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);

                    String url = "";
                    String json = "";

                    Gson gson = new Gson();
                    VerifyCheckReq req = new VerifyCheckReq();
                    req.setCheckCode(checkCode);
                    req.setCheckCodeToken(checkCodeToken);
                    req.setCheckCodeType(checkCodeType);
                    req.setMobile(mobileOrEmail);
                    req.setEmail("");
                    json = gson.toJson(req);
                    url = Const.VERIFY_CHECK_CODE;

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
                            msg.what = 3;
                            handler.sendMessage(msg);
                        }
                    });
                }
            }
        });

        sp_auth_code_et_2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL){
                    if(sp_auth_code_et_2.getText().toString().isEmpty()){
                        sp_auth_code_et_2.clearFocus();
                        sp_auth_code_et_1.requestFocus();
                        sp_auth_code_et_1.setText("");
                        sp_auth_code_et_1.setSelection(sp_auth_code_et_1.getText().length());
                    }else{
                        sp_auth_code_et_2.setText("");
                    }
                }
                return false;
            }
        });
        sp_auth_code_et_3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL){
                    if(sp_auth_code_et_3.getText().toString().isEmpty()){
                        sp_auth_code_et_3.clearFocus();
                        sp_auth_code_et_2.requestFocus();
                        sp_auth_code_et_2.setText("");
                        sp_auth_code_et_2.setSelection(sp_auth_code_et_2.getText().length());
                    }else{
                        sp_auth_code_et_3.setText("");
                    }
                }
                return false;
            }
        });
        sp_auth_code_et_4.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL){
                    if(sp_auth_code_et_4.getText().toString().isEmpty()){
                        sp_auth_code_et_4.clearFocus();
                        sp_auth_code_et_3.requestFocus();
                        sp_auth_code_et_3.setText("");
                        sp_auth_code_et_3.setSelection(sp_auth_code_et_3.getText().length());
                    }else{
                        sp_auth_code_et_4.setText("");
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
        sp_auth_code_submit.setEnabled(false);
        sp_auth_code_submit.setText(sp_auth_code_submit.getText()+"("+(often-1)+"s)");
        sp_auth_code_submit.setBackgroundResource(R.drawable.btn_finish_inactive);
        thread.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sp_auth_code_back:
                finish();
                break;
            case R.id.sp_auth_code_submit:
                blackBox = FMAgent.onEvent(SetPwdAuthCodeActivity.this);
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

                        String url = "";
                        String json = "";
                        String messageContentType = Const.MESSAGE_CONTENT_TYPE_PHONE;


                        url = Const.SEND_MOBILE_CHECK;
                        Gson gson = new Gson();
                        SendMobileCheckReq req = new SendMobileCheckReq();
                        req.setBlackBox(blackBox);
                        req.setCheckCodeType(checkCodeType);
                        req.setMobile(mobileOrEmail);
                        req.setMessageContentType(messageContentType);
                        req.setValidateToken(validateToken);
                        json = gson.toJson(req);

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
