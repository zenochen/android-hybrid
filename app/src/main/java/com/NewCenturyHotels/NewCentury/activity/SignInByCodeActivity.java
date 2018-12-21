package com.NewCenturyHotels.NewCentury.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.cons.CheckCodeTypeEnum;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.cons.Regex;
import com.NewCenturyHotels.NewCentury.req.CheckMemberReq;
import com.NewCenturyHotels.NewCentury.req.SendMobileCheckReq;
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
 * 验证码登录
 */
public class SignInByCodeActivity extends SwipeBackActivity implements View.OnClickListener{

    RelativeLayout signcode_back;
    EditText signcode_et;
    TextView signcode_get;
    TextView signcode_signin;
    ImageView iv_clear;
    Intent intent;
    LinearLayout statusBar;

    final String TAG = SignInByCodeActivity.class.getName();
    //同盾校验
    private TDBindCaptcha captcha;
    String blackBox = "";
    String validateToken = "";

    //判断是否已注册
    Boolean isLogin = false;
    String checkCodeToken = "";
    int often;

    RelativeLayout loading;

    String account;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try{
                stopLoading();
                if(msg.what == 1){//发送验证码
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        JsonObject data = jo.getAsJsonObject("data");
                        checkCodeToken = data.get("checkCodeToken").getAsString();
                        often = data.get("often").getAsInt();

                        intent = new Intent(SignInByCodeActivity.this,SignCodeActivity.class);
                        intent.putExtra("isLogin",isLogin);
                        intent.putExtra("mobile",account);
                        intent.putExtra("checkCodeToken",checkCodeToken);
                        intent.putExtra("often",often);
                        intent.putExtra("validateToken",validateToken);
                        intent.putExtra("blackBox",blackBox);
                        startActivity(intent);
                    }else{
                        Toast.makeText(SignInByCodeActivity.this,message,Toast.LENGTH_LONG).show();
                    }
                }else if(msg.what == 2) {//验证是否注册会员

                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if (code == 200) {
                        isLogin = true;
                    } else {
                        isLogin = false;
                    }

                    blackBox = FMAgent.onEvent(SignInByCodeActivity.this);
                    Log.i(TAG, "blackBox: " + blackBox);
                    captcha.verify();

                }
            }catch (Exception e){
                Log.e(TAG, "Exception: " + e.getMessage());
                Toast.makeText(SignInByCodeActivity.this,"连接超时",Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_by_code);
        StatusBarUtils.with(this).init();
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
                        req.setMobile(account);
                        req.setMessageContentType(messageContentType);
                        req.setValidateToken(validateToken);
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
                                msg.what = 1;
                                handler.sendMessage(msg);
                            }
                        });
                    }
                });

    }

    void initComponent(){
        signcode_back = (RelativeLayout) findViewById(R.id.signcode_back);
        signcode_et = (EditText) findViewById(R.id.signcode_et_account);
        signcode_get = (TextView) findViewById(R.id.signcode_get);
        signcode_signin = (TextView) findViewById(R.id.signcode_signin);
        iv_clear = (ImageView) findViewById(R.id.signcode_iv_clear);
        loading = (RelativeLayout) findViewById(R.id.signcode_loading);

        //调整通知栏高度
        statusBar = (LinearLayout) findViewById(R.id.signcode_status_bar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this);
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        statusBar.setLayoutParams(layoutParams);
    }

    void initEvent(){

        signcode_back.setOnClickListener(this);
        signcode_get.setOnClickListener(this);
        signcode_signin.setOnClickListener(this);
        iv_clear.setOnClickListener(this);
        signcode_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (i2 == 1 || i2 == 0) {
                    String tel = signcode_et.getText().toString().replace(" ", "");
                    if (tel.length() <= 3 && charSequence.toString().length() == 4) {
                        signcode_et.setText(tel);
                    }
                    if (tel.length() > 3 && tel.length() < 8) {
                        signcode_et.setText(tel.substring(0, 3) + " " + tel.substring(3, tel.length()));
                    }
                    if (tel.length() > 7) {
                        signcode_et.setText(tel.substring(0, 3) + " " + tel.substring(3, 7) + " " + tel.substring(7, tel.length()));
                    }
                    signcode_et.setSelection(signcode_et.getText().toString().length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

    void checkMember(){
         new Thread(){
            @Override
            public void run() {
                String url = Const.CHECK_MEMBER;
                Gson gson = new Gson();
                CheckMemberReq req = new CheckMemberReq();
                req.setEmail("");
                req.setMobile(account);
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
                        msg.what = 2;
                        handler.sendMessage(msg);
                    }
                });


            }
        }.start();
    }

    void startLoading(){
        loading.setVisibility(View.VISIBLE);
        loading.setOnClickListener(null);
    }

    void stopLoading(){
        loading.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signcode_back:
                finish();
                break;
            case R.id.signcode_get:
                account = signcode_et.getText().toString().replace(" ","").trim();
                if(account.isEmpty()){
                    Toast.makeText(SignInByCodeActivity.this,"手机号不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }

                String regex = Regex.MOBILE;
                if(!account.matches(regex)){
                    Toast.makeText(SignInByCodeActivity.this,"请输入正确的手机号",Toast.LENGTH_SHORT).show();
                    return;
                }

                startLoading();
                checkMember();

                break;
            case R.id.signcode_signin:
                intent = new Intent(SignInByCodeActivity.this,SignInActivity.class);
                startActivity(intent);
                break;
            case R.id.signcode_iv_clear:
                signcode_et.setText("");
                break;
        }
    }
}
