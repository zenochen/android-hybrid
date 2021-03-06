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

import com.NewCenturyHotels.NewCentury.App;
import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.cons.AppInfo;
import com.NewCenturyHotels.NewCentury.cons.CheckCodeTypeEnum;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;
import com.NewCenturyHotels.NewCentury.req.ChangeMobileEmailReq;
import com.NewCenturyHotels.NewCentury.req.SendMobileCheckReq;
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
 * 身份认证输入验证码第二步
 */
public class ModStep2AuthCodeActivity extends SwipeBackActivity implements View.OnClickListener{

    RelativeLayout ms2_auth_code_back;
    TextView ms2_auth_code_notice;
    TextView ms2_auth_code_submit;
    EditText ms2_auth_code_et_1;
    EditText ms2_auth_code_et_2;
    EditText ms2_auth_code_et_3;
    EditText ms2_auth_code_et_4;
    Intent intent;
    LinearLayout statusBar;

    final String TAG = ModStep2AuthCodeActivity.class.getName();

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
    //第一步获取的验证码
    String oldCheckCode = "";
    String oldCheckCodeToken = "";

    int often = 60;

    //倒计时
    Thread thread;
    //是否修改手机号
    Boolean isMobile;

    RelativeLayout loading;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try{
                stopLoading();
                if(msg.what == 1){//发送验证码倒计时
                    String str = "";
                    if(ms2_auth_code_submit.getText().length() == 4){
                        return;
                    }
                    if(ms2_auth_code_submit.getText().length() == 9){
                        str = ms2_auth_code_submit.getText().toString().substring(5,7);
                    }else{
                        str = ms2_auth_code_submit.getText().toString().substring(5,6);
                    }

                    Integer sec = (Integer.parseInt(str)-1);
                    if(sec == 0){
                        ms2_auth_code_submit.setEnabled(true);
                        str = "重新发送";
                        ms2_auth_code_submit.setText(str);
                        ms2_auth_code_submit.setBackgroundResource(R.drawable.btn_finish);
                        often = 60;
                    }else{
                        str = "重新发送(" + sec +"s)";
                        ms2_auth_code_submit.setText(str);
                        ms2_auth_code_submit.setBackgroundResource(R.drawable.btn_finish_inactive);
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
                        HttpHelper.reLogin(ModStep2AuthCodeActivity.this);
                    }else{
                        Toast.makeText(ModStep2AuthCodeActivity.this,message,Toast.LENGTH_SHORT).show();
                    }
                }else if(msg.what == 3){//验证码验证
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        //修改手机邮箱第二步
                        startLoading();
                        changeMobile();
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(ModStep2AuthCodeActivity.this);
                    }else{
                        Toast.makeText(ModStep2AuthCodeActivity.this,message,Toast.LENGTH_LONG).show();
                    }
                }else if(msg.what == 4)//修改手机邮箱
                {
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(ModStep2AuthCodeActivity.this);
                        sharedPreferencesHelper.put(SharedPref.LOGINED,false);
                        sharedPreferencesHelper.put(SharedPref.TOKEN,"");
                        HttpHelper.setAuthorization("");
                        intent = new Intent(ModStep2AuthCodeActivity.this,SignInActivity.class);
                        intent.putExtra("canBack",false);
                        startActivity(intent);
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(ModStep2AuthCodeActivity.this);
                    }else{
                        Toast.makeText(ModStep2AuthCodeActivity.this,message,Toast.LENGTH_LONG).show();
                    }
                }

            }catch (Exception e){
                Log.e(TAG, "Exception: " + e.getMessage());
                Toast.makeText(ModStep2AuthCodeActivity.this,"连接超时",Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_step2_auth_code);
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
        oldCheckCode = getIntent().getStringExtra("oldCheckCode");
        oldCheckCodeToken = getIntent().getStringExtra("oldCheckCodeToken");
        if(oldCheckCode == null){
            oldCheckCode = "";
            oldCheckCodeToken = "";
        }
        often = getIntent().getIntExtra("often",60);
        often = 60;

        //设置验证码类型
        isMobile = (Boolean) App.mInfo.get(AppInfo.CHANGE_MOBILE);
        if(isMobile){
            checkCodeType = CheckCodeTypeEnum.API_NEW_MODIFY_MOBILE_CODE_KEY.toString();
        }else{
            checkCodeType = CheckCodeTypeEnum.API_NEW_MODIFY_EMAIL_CODE_KEY.toString();
        }

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

        ms2_auth_code_back = (RelativeLayout) findViewById(R.id.ms2_auth_code_back);
        ms2_auth_code_notice = (TextView) findViewById(R.id.ms2_auth_code_notice);
        ms2_auth_code_submit = (TextView) findViewById(R.id.ms2_auth_code_submit);
        ms2_auth_code_et_1 = (EditText) findViewById(R.id.ms2_auth_code_et_1);
        ms2_auth_code_et_2 = (EditText) findViewById(R.id.ms2_auth_code_et_2);
        ms2_auth_code_et_3 = (EditText) findViewById(R.id.ms2_auth_code_et_3);
        ms2_auth_code_et_4 = (EditText) findViewById(R.id.ms2_auth_code_et_4);
        loading = (RelativeLayout) findViewById(R.id.ms2_auth_code_loading);

        ms2_auth_code_notice.setText("验证码已发送至" + mobileOrEmail);

        //调整通知栏高度
        statusBar = (LinearLayout) findViewById(R.id.ms2_auth_code_status_bar);
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

        ms2_auth_code_back.setOnClickListener(this);
        ms2_auth_code_submit.setOnClickListener(this);
        ms2_auth_code_et_1.requestFocus();
        ms2_auth_code_et_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() == 1){
                    ms2_auth_code_et_1.clearFocus();
                    ms2_auth_code_et_2.requestFocus();
                }
            }
        });

        ms2_auth_code_et_2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() == 1){
                    ms2_auth_code_et_2.clearFocus();
                    ms2_auth_code_et_3.requestFocus();
                }
            }
        });
        ms2_auth_code_et_3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() == 1){
                    ms2_auth_code_et_3.clearFocus();
                    ms2_auth_code_et_4.requestFocus();
                }
            }
        });
        ms2_auth_code_et_4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() == 1){
                    String code1 = ms2_auth_code_et_1.getText().toString().trim();
                    String code2 = ms2_auth_code_et_2.getText().toString().trim();
                    String code3 = ms2_auth_code_et_3.getText().toString().trim();
                    String code4 = ms2_auth_code_et_4.getText().toString().trim();
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
                    if(isMobile){
                        req.setMobile(mobileOrEmail);
                        req.setEmail("");
                    }else{
                        req.setMobile("");
                        req.setEmail(mobileOrEmail);
                    }

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

        ms2_auth_code_et_2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL){
                    if(ms2_auth_code_et_2.getText().toString().isEmpty()){
                        ms2_auth_code_et_2.clearFocus();
                        ms2_auth_code_et_1.requestFocus();
                        ms2_auth_code_et_1.setText("");
                        ms2_auth_code_et_1.setSelection(ms2_auth_code_et_1.getText().length());
                    }else{
                        ms2_auth_code_et_2.setText("");
                    }
                }
                return false;
            }
        });
        ms2_auth_code_et_3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL){
                    if(ms2_auth_code_et_3.getText().toString().isEmpty()){
                        ms2_auth_code_et_3.clearFocus();
                        ms2_auth_code_et_2.requestFocus();
                        ms2_auth_code_et_2.setText("");
                        ms2_auth_code_et_2.setSelection(ms2_auth_code_et_2.getText().length());
                    }else{
                        ms2_auth_code_et_3.setText("");
                    }
                }
                return false;
            }
        });
        ms2_auth_code_et_4.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL){
                    if(ms2_auth_code_et_4.getText().toString().isEmpty()){
                        ms2_auth_code_et_4.clearFocus();
                        ms2_auth_code_et_3.requestFocus();
                        ms2_auth_code_et_3.setText("");
                        ms2_auth_code_et_3.setSelection(ms2_auth_code_et_3.getText().length());
                    }else{
                        ms2_auth_code_et_4.setText("");
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

    void changeMobile(){
        new Thread(){
            @Override
            public void run() {
                String url = Const.CHANGE_MOBIL_EMAIL;
                Gson gson = new Gson();
                ChangeMobileEmailReq req = new ChangeMobileEmailReq();
                req.setCheckCode(oldCheckCode);
                req.setCheckCodeToken(oldCheckCodeToken);
                req.setNewUserName(mobileOrEmail);
                req.setNewCheckCode(checkCode);
                req.setNewCheckCodeToken(checkCodeToken);
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
                        Message msg = new Message();
                        msg.setData(bundle);
                        msg.what = 4;
                        handler.sendMessage(msg);
                    }
                });
            }
        }.start();
    }

    void initSendCode(){
        ms2_auth_code_submit.setEnabled(false);
        ms2_auth_code_submit.setText(ms2_auth_code_submit.getText()+"("+(often-1)+"s)");
        ms2_auth_code_submit.setBackgroundResource(R.drawable.btn_finish_inactive);
        thread.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ms2_auth_code_back:
                finish();
                break;
            case R.id.ms2_auth_code_submit:
                blackBox = FMAgent.onEvent(ModStep2AuthCodeActivity.this);
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
