package com.NewCenturyHotels.NewCentury.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
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
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.cons.Regex;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;
import com.NewCenturyHotels.NewCentury.req.SignInReq;
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
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 账号密码登录
 */
public class SignInActivity extends AppCompatActivity implements View.OnClickListener{

    RelativeLayout signin_back;
    TextView signin_forget;
    TextView signin_code;
    TextView signin_submit;
    TextView signin_notice;

    //账号密码输入
    EditText et_acc;
    EditText et_pwd;
    ImageView iv_hidden;
    Boolean isHidden = true;
    RelativeLayout loading;
    LinearLayout statusBar;

    Intent intent;
    Boolean canBack;

    final String TAG = SignInActivity.class.getName();

    //同盾校验
    private TDBindCaptcha captcha;
    String blackBox = "";

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
                        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(SignInActivity.this);
                        sharedPreferencesHelper.put(SharedPref.LOGINED,true);
                        String token = jo.getAsJsonObject("data").get("token").getAsString();
                        sharedPreferencesHelper.put(SharedPref.TOKEN,token);
                        HttpHelper.setAuthorization(token);
                        intent = new Intent(SignInActivity.this,MainActivity.class);
                        if(!canBack){//忘记密码后登录
                            intent.putExtra("tabIndex",3);
                        }
                        startActivity(intent);
                    }else{
                        Toast.makeText(SignInActivity.this,message,Toast.LENGTH_LONG).show();
                    }
                }
            }catch (Exception e){
                Log.e(TAG, "Exception: " + e.getMessage());
                Toast.makeText(SignInActivity.this,"连接超时",Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
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

                        startLoading();

                        String acc = et_acc.getText().toString().trim();
                        String pwd = et_pwd.getText().toString().trim();

                        Gson gson = new Gson();
                        SignInReq req = new SignInReq();
                        req.setValidateToken(token);
                        req.setPassword(pwd);
                        req.setUserName(acc);
                        req.setBlackBox(blackBox);
                        req.setSource(Const.SOURCE_TYPE);
                        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(SignInActivity.this);
                        req.setDeviceId(sharedPreferencesHelper.get(SharedPref.DEVICE_TOKEN,"").toString());
                        String jsonStr = gson.toJson(req);

                        String url = Const.SIGN_IN;

                        HttpHelper.sendOkHttpPost(url, jsonStr, new Callback() {
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

        signin_back = (RelativeLayout) findViewById(R.id.signin_back);
        signin_forget = (TextView) findViewById(R.id.signin_tv_forget);
        signin_code = (TextView) findViewById(R.id.signin_tv_code);
        signin_submit = (TextView) findViewById(R.id.signin_tv_submit);
        et_acc = (EditText) findViewById(R.id.signin_et_account);
        et_pwd = (EditText) findViewById(R.id.signin_et_pwd);
        iv_hidden = (ImageView) findViewById(R.id.signin_iv_eye);
        loading = (RelativeLayout) findViewById(R.id.signin_loading);
        signin_notice = (TextView) findViewById(R.id.signin_notice);

        //调整通知栏高度
        statusBar = (LinearLayout) findViewById(R.id.signin_status_bar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this);
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        statusBar.setLayoutParams(layoutParams);

        canBack = getIntent().getBooleanExtra("canBack",true);
    }

    void initEvent(){

        signin_back.setOnClickListener(this);
        signin_forget.setOnClickListener(this);
        signin_code.setOnClickListener(this);
        signin_submit.setOnClickListener(this);
        iv_hidden.setOnClickListener(this);
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
            case R.id.signin_back:
                if(canBack){
                    finish();
                }
                break;
            case R.id.signin_tv_forget:
                App.mInfo.put(AppInfo.RESET_PWD,false);
                intent = new Intent(SignInActivity.this,SetPwdAuthActivity.class);
                startActivity(intent);
                break;
            case R.id.signin_tv_code:
                intent = new Intent(SignInActivity.this,SignInByCodeActivity.class);
                startActivity(intent);
                break;
            case R.id.signin_tv_submit:

                String account = et_acc.getText().toString().trim();
                if(account.isEmpty()){
                    Toast.makeText(SignInActivity.this,"手机号不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                String pwd = et_pwd.getText().toString().trim();
                if(pwd.isEmpty()){
                    Toast.makeText(SignInActivity.this,"密码不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!account.matches(Regex.EMAIL) && !account.matches(Regex.NUMBER)){
                    Toast.makeText(SignInActivity.this,"请输入正确的邮箱或卡号",Toast.LENGTH_SHORT).show();
                    return;
                }


                blackBox = FMAgent.onEvent(SignInActivity.this);
                Log.i(TAG, "blackBox: " + blackBox);
                captcha.verify();
                break;
            case R.id.signin_iv_eye:
                if(isHidden){//显示密码
                    et_pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    et_pwd.setSelection(et_pwd.getText().toString().length());
                    isHidden = false;
                    iv_hidden.setBackgroundResource(R.drawable.textfield_openeye);
                }else{//隐藏密码
                    et_pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    et_pwd.setSelection(et_pwd.getText().toString().length());
                    isHidden = true;
                    iv_hidden.setBackgroundResource(R.drawable.textfield_closeeye);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(canBack){
            finish();
        }
    }
}
