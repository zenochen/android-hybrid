package com.NewCenturyHotels.NewCentury.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
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
 * 修改密码身份认证
 */
public class SetPwdAuthActivity extends SwipeBackActivity implements View.OnClickListener{

    Intent intent;
    RelativeLayout sp_auth_back;
    EditText sp_auth_et;
    ImageView sp_auth_clear;
    TextView sp_auth_submit;
    RelativeLayout loading;
    TextView sp_auth_notice;
    TextView sp_auth_title;
    LinearLayout statusBar;

    //输入
    String input;

    //同盾校验
    private TDBindCaptcha captcha;
    String blackBox = "";
    String validateToken = "";

    //发送验证码类型
    String checkCodeType = "";

    final static String TAG = SetPwdAuthActivity.class.getName();

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
                        String checkCodeToken = data.get("checkCodeToken").getAsString();
                        int often = data.get("often").getAsInt();

                        Toast.makeText(SetPwdAuthActivity.this,message,Toast.LENGTH_LONG).show();

                        intent = new Intent(SetPwdAuthActivity.this,SetPwdAuthCodeActivity.class);
                        intent.putExtra("mobileOrEmail",input);
                        intent.putExtra("checkCodeToken",checkCodeToken);
                        intent.putExtra("often",often);
                        intent.putExtra("validateToken",validateToken);
                        intent.putExtra("blackBox",blackBox);
                        startActivity(intent);
                    }else{
                        Toast.makeText(SetPwdAuthActivity.this,message,Toast.LENGTH_LONG).show();
                    }
                }else if(msg.what == 2) {//验证是否注册会员

                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if (code == 200) {
                        blackBox = FMAgent.onEvent(SetPwdAuthActivity.this);
                        Log.i(TAG, "blackBox: " + blackBox);
                        captcha.verify();
                    } else {
                        Toast.makeText(SetPwdAuthActivity.this,message,Toast.LENGTH_LONG).show();
                    }

                }
            }catch (Exception e){
                Log.e(TAG, "Exception: " + e.getMessage());
                Toast.makeText(SetPwdAuthActivity.this,"连接超时",Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pwd_auth);
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

    void initComponent(){
        sp_auth_back = (RelativeLayout) findViewById(R.id.sp_auth_back);
        sp_auth_et = (EditText) findViewById(R.id.sp_auth_et);
        sp_auth_clear = (ImageView) findViewById(R.id.sp_auth_iv_clear);
        sp_auth_submit = (TextView) findViewById(R.id.sp_auth_next);
        loading = (RelativeLayout) findViewById(R.id.sp_auth_loading);
        sp_auth_notice = (TextView) findViewById(R.id.sp_auth_notice);
        sp_auth_title = (TextView) findViewById(R.id.sp_auth_title);

        //调整通知栏高度
        statusBar = (LinearLayout) findViewById(R.id.sp_auth_status_bar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this);
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        statusBar.setLayoutParams(layoutParams);
    }

    void initEvent(){

        sp_auth_back.setOnClickListener(this);
        sp_auth_clear.setOnClickListener(this);
        sp_auth_submit.setOnClickListener(this);
    }

    void checkMember(){
        new Thread(){
            @Override
            public void run() {
                String url = Const.CHECK_MEMBER;

                String account = sp_auth_et.getText().toString().trim();
                String json = "";
                String regex = Regex.EMAIL;
                if(account.matches(regex)){
                    Gson gson = new Gson();
                    CheckMemberReq req = new CheckMemberReq();
                    req.setEmail(account);
                    req.setMobile("");
                    json = gson.toJson(req);
                }else{
                    Gson gson = new Gson();
                    CheckMemberReq req = new CheckMemberReq();
                    req.setEmail("");
                    req.setMobile(account);
                    json = gson.toJson(req);
                }

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
                        checkCodeType = CheckCodeTypeEnum.API_UPDATE_PASSWORD_CODE_KEY.toString();

                        url = Const.SEND_MOBILE_CHECK;
                        Gson gson = new Gson();
                        SendMobileCheckReq req = new SendMobileCheckReq();
                        req.setBlackBox(blackBox);
                        req.setCheckCodeType(checkCodeType);
                        req.setMobile(input);
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
                                msg.what = 1;
                                handler.sendMessage(msg);
                            }
                        });
                    }
                });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sp_auth_back:
                finish();
                break;
            case R.id.sp_auth_iv_clear:
                sp_auth_et.setText("");
                break;
            case R.id.sp_auth_next:
                input = sp_auth_et.getText().toString().trim();
                if(input.isEmpty()){
                    Toast.makeText(SetPwdAuthActivity.this,"输入不能为空",Toast.LENGTH_LONG).show();
                    return;
                }
                String regex = Regex.MOBILE;

                if(!input.matches(regex)){
                    Toast.makeText(SetPwdAuthActivity.this,"请输入正确的手机号",Toast.LENGTH_SHORT).show();
                    return;
                }
                startLoading();
                checkMember();
                break;
        }
    }
}
