package com.NewCenturyHotels.NewCentury.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import com.NewCenturyHotels.NewCentury.req.SendLoginedCheckReq;
import com.NewCenturyHotels.NewCentury.util.HttpHelper;
import com.NewCenturyHotels.NewCentury.util.StatusBarUtils;
import com.NewCenturyHotels.NewCentury.view.CommomDialog;
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
 * 身份认证——修改手机邮箱第一步
 */
public class ModStep1AuthActivity extends SwipeBackActivity implements View.OnClickListener{

    Intent intent;
    RelativeLayout ms1_auth_back;
    EditText ms1_auth_et;
    ImageView ms1_auth_clear;
    TextView ms1_auth_submit;
    RelativeLayout loading;
    TextView ms1_auth_notice;
    TextView ms1_auth_title;
    LinearLayout statusBar;
    TextView callPhone;

    //同盾校验
    private TDBindCaptcha captcha;
    String blackBox = "";
    String validateToken = "";

    //发送验证码类型
    String checkCodeType = "";

    Boolean changeMobile;
    String mobileOrEmail = "";

    final static String TAG = ModStep1AuthActivity.class.getName();

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

                        Toast.makeText(ModStep1AuthActivity.this,message,Toast.LENGTH_LONG).show();

                        intent = new Intent(ModStep1AuthActivity.this,ModStep1AuthCodeActivity.class);
                        intent.putExtra("mobileOrEmail",mobileOrEmail);
                        intent.putExtra("checkCodeToken",checkCodeToken);
                        intent.putExtra("often",often);
                        intent.putExtra("validateToken",validateToken);
                        intent.putExtra("blackBox",blackBox);
                        startActivity(intent);
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(ModStep1AuthActivity.this);
                    }else{
                        Toast.makeText(ModStep1AuthActivity.this,message,Toast.LENGTH_LONG).show();
                    }
                }
            }catch (Exception e){
                Log.e(TAG, "Exception: " + e.getMessage());
                Toast.makeText(ModStep1AuthActivity.this,"连接超时",Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_step1_auth);
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
        ms1_auth_back = (RelativeLayout) findViewById(R.id.ms1_auth_back);
        ms1_auth_et = (EditText) findViewById(R.id.ms1_auth_et);
        ms1_auth_clear = (ImageView) findViewById(R.id.ms1_auth_iv_clear);
        ms1_auth_submit = (TextView) findViewById(R.id.ms1_auth_next);
        loading = (RelativeLayout) findViewById(R.id.ms1_auth_loading);
        ms1_auth_notice = (TextView) findViewById(R.id.ms1_auth_notice);
        ms1_auth_title = (TextView) findViewById(R.id.ms1_auth_title);
        callPhone = (TextView) findViewById(R.id.ms1_auth_tv_phone);

        //调整通知栏高度
        statusBar = (LinearLayout) findViewById(R.id.ms1_auth_status_bar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this);
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        statusBar.setLayoutParams(layoutParams);

        //个人资料跳转来
        changeMobile = (Boolean) App.mInfo.get(AppInfo.CHANGE_MOBILE);
        mobileOrEmail = getIntent().getStringExtra("mobileOrEmail");
        if(changeMobile){
            ms1_auth_title.setText("修改手机号");
            ms1_auth_et.setInputType(InputType.TYPE_CLASS_NUMBER);
        }else{
            ms1_auth_title.setText("修改邮箱");
            ms1_auth_et.setHint("请输入绑定的邮箱");
            ms1_auth_et.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        if(mobileOrEmail != null){
            ms1_auth_et.setText(mobileOrEmail);
            ms1_auth_et.setSelection(mobileOrEmail.length());
        }

        ms1_auth_et.setEnabled(false);
        ms1_auth_clear.setEnabled(false);
    }

    void initEvent(){
        ms1_auth_back.setOnClickListener(this);
        ms1_auth_clear.setOnClickListener(this);
        ms1_auth_submit.setOnClickListener(this);
        callPhone.setOnClickListener(this);
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
                        String messageContentType = "";

                        //发送登录的验证码
                        if(!changeMobile){
                            messageContentType = Const.MESSAGE_CONTENT_TYPE_EMAIL;
                            url = Const.SEND_LOGIN_EMAIL_CHECK;
                        }else{
                            messageContentType = Const.MESSAGE_CONTENT_TYPE_PHONE;
                            url = Const.SEND_LOGIN_MOBILE_CHECK;
                        }

                        Gson gson = new Gson();
                        SendLoginedCheckReq req = new SendLoginedCheckReq();
                        req.setBlackBox(blackBox);
                        req.setCheckCodeType(checkCodeType);
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
            case R.id.ms1_auth_back:
                finish();
                break;
            case R.id.ms1_auth_tv_phone:
                CommomDialog dialog = new CommomDialog(ModStep1AuthActivity.this, R.style.dialog, "呼叫：10105050", new CommomDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if(confirm){
                            dialog.dismiss();
                            if (ActivityCompat.checkSelfPermission(ModStep1AuthActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                ActivityCompat.requestPermissions(ModStep1AuthActivity.this, new String[]{
                                        Manifest.permission.CALL_PHONE
                                }, 100);
                                return;
                            }

                            Intent intent = new Intent(Intent.ACTION_CALL);
                            Uri data = Uri.parse("tel:10105050");
                            intent.setData(data);
                            startActivity(intent);
                        }
                    }
                });
                dialog.show();
                break;
            case R.id.ms1_auth_iv_clear:
                ms1_auth_et.setText("");
                break;
            case R.id.ms1_auth_next:
                //设置验证码类型
                if(changeMobile){
                    checkCodeType = CheckCodeTypeEnum.API_MODIFY_MOBILE_CODE_KEY.toString();
                }else{
                    checkCodeType = CheckCodeTypeEnum.API_MODIFY_EMAIL_CODE_KEY.toString();
                }

                try{
                    blackBox = FMAgent.onEvent(ModStep1AuthActivity.this);
                    Log.i(TAG, "blackBox: " + blackBox);
                    captcha.verify();
                }catch (Exception e){
                    Log.e(TAG, "Exception: " + e.getMessage());
                    Toast.makeText(ModStep1AuthActivity.this,"请重新再试",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
