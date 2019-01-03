package com.NewCenturyHotels.NewCentury.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.App;
import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.cons.AppInfo;
import com.NewCenturyHotels.NewCentury.cons.CheckCodeTypeEnum;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.cons.Regex;
import com.NewCenturyHotels.NewCentury.req.SendEmailCheckReq;
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
 * 修改手机邮箱
 */
public class SetPhoneEmailActivity extends SwipeBackActivity implements View.OnClickListener{

    Intent intent;
    RelativeLayout setemail_back;
    EditText setemail_et;
    ImageView setemail_clear;
    TextView setemail_get;
    TextView title;
    TextView subTitle;
    TextView subTitle2;
    ImageView icon;
    RelativeLayout loading;
    LinearLayout statusBar;

    //同盾校验
    String blackBox = "";
    String validateToken = "";
    //被修改的值
    String mobileOrEmail = "";
    //验证码
    String checkCode = "";
    String checkCodeToken = "";
    //第一步获取的验证码
    String oldCheckCode = "";
    String oldCheckCodeToken = "";
    //输入是否手机号
    Boolean isMobile;
    //输入
    String input;

    private TDBindCaptcha captcha;

    final static String TAG = SetPhoneEmailActivity.class.getName();

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try{
                stopLoading();
                if(msg.what == 1){
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        JsonObject data = jo.getAsJsonObject("data");
                        String checkCodeToken = data.get("checkCodeToken").getAsString();
                        int often = data.get("often").getAsInt();

                        intent = new Intent(SetPhoneEmailActivity.this,ModStep2AuthCodeActivity.class);
                        intent.putExtra("mobileOrEmail",input);
                        intent.putExtra("checkCodeToken",checkCodeToken);
                        intent.putExtra("often",often);
                        intent.putExtra("validateToken",validateToken);
                        intent.putExtra("blackBox",blackBox);
                        intent.putExtra("oldCheckCodeToken",oldCheckCodeToken);
                        intent.putExtra("oldCheckCode",oldCheckCode);
                        startActivity(intent);
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(SetPhoneEmailActivity.this);
                    }else{
                        Toast.makeText(SetPhoneEmailActivity.this,message,Toast.LENGTH_SHORT).show();
                    }
                }
            }catch (Exception e){
                Log.e(TAG, "Exception: " + e.getMessage());
                Toast.makeText(SetPhoneEmailActivity.this,"连接超时",Toast.LENGTH_LONG).show();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_phone_email);
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
        setemail_back = (RelativeLayout) findViewById(R.id.setemail_back);
        setemail_et = (EditText) findViewById(R.id.setemail_et);
        setemail_clear = (ImageView) findViewById(R.id.setemail_iv_clear);
        setemail_get = (TextView) findViewById(R.id.setemail_get);
        title = (TextView) findViewById(R.id.setemail_title);
        subTitle = (TextView) findViewById(R.id.setemail_subtitle);
        subTitle2 = (TextView) findViewById(R.id.setemail_subtitle2);
        icon = (ImageView) findViewById(R.id.setemail_iv_1);
        loading = (RelativeLayout) findViewById(R.id.setemail_loading);

        //调整通知栏高度
        statusBar = (LinearLayout) findViewById(R.id.setemail_status_bar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this);
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        statusBar.setLayoutParams(layoutParams);

        isMobile = (Boolean) (App.mInfo.get(AppInfo.CHANGE_MOBILE));
        if(isMobile){
            title.setText("设置新手机号");
            subTitle.setText("请设置手机号");
            subTitle2.setText("手机号");
            setemail_et.setHint("请输入新手机号");
            icon.setBackgroundResource(R.drawable.signin_mobile_phone);
            setemail_et.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        blackBox = getIntent().getStringExtra("blackBox");
        validateToken = getIntent().getStringExtra("validateToken");
        mobileOrEmail = getIntent().getStringExtra("mobileOrEmail");
        checkCode = getIntent().getStringExtra("checkCode");
        checkCodeToken = getIntent().getStringExtra("checkCodeToken");
        oldCheckCode = getIntent().getStringExtra("oldCheckCode");
        oldCheckCodeToken = getIntent().getStringExtra("oldCheckCodeToken");
    }

    void initEvent(){
        setemail_back.setOnClickListener(this);
        setemail_clear.setOnClickListener(this);
        setemail_get.setOnClickListener(this);
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
                        String checkCodeType = "";
                        String messageContentType = "";
                        if(isMobile){
                            url = Const.SEND_MOBILE_CHECK;
                            checkCodeType = CheckCodeTypeEnum.API_NEW_MODIFY_MOBILE_CODE_KEY.toString();
                            messageContentType = Const.MESSAGE_CONTENT_TYPE_PHONE;
                            Gson gson = new Gson();
                            SendMobileCheckReq req = new SendMobileCheckReq();
                            req.setBlackBox(blackBox);
                            req.setCheckCodeType(checkCodeType);
                            req.setMobile(input);
                            req.setMessageContentType(messageContentType);
                            req.setValidateToken(validateToken);
                            json = gson.toJson(req);
                        }else{
                            url = Const.SEND_EMAIL_CHECK;
                            checkCodeType = CheckCodeTypeEnum.API_NEW_MODIFY_EMAIL_CODE_KEY.toString();
                            messageContentType = Const.MESSAGE_CONTENT_TYPE_EMAIL;
                            Gson gson = new Gson();
                            SendEmailCheckReq req = new SendEmailCheckReq();
                            req.setBlackBox(blackBox);
                            req.setCheckCodeType(checkCodeType);
                            req.setEmail(input);
                            req.setMessageContentType(messageContentType);
                            req.setValidateToken(validateToken);
                            json = gson.toJson(req);
                        }

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
            case R.id.setemail_back:
                finish();
                break;
            case R.id.setemail_iv_clear:
                break;
            case R.id.setemail_get:
                input = setemail_et.getText().toString().trim();
                if(isMobile){
                    String regex = Regex.MOBILE;
                    if(!input.matches(regex)){
                        Toast.makeText(SetPhoneEmailActivity.this,"请输入正确的手机号",Toast.LENGTH_LONG).show();
                        return;
                    }
                }else{
                    String regex = Regex.EMAIL;
                    if(!input.matches(regex)){
                        Toast.makeText(SetPhoneEmailActivity.this,"请输入正确的邮箱",Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                blackBox = FMAgent.onEvent(SetPhoneEmailActivity.this);
                Log.i(TAG, "blackBox: " + blackBox);
                captcha.verify();
                break;
        }
    }
}
