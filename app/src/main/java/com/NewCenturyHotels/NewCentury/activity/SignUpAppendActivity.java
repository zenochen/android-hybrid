package com.NewCenturyHotels.NewCentury.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;
import com.NewCenturyHotels.NewCentury.req.SignUpReq;
import com.NewCenturyHotels.NewCentury.util.HttpHelper;
import com.NewCenturyHotels.NewCentury.util.SharedPreferencesHelper;
import com.NewCenturyHotels.NewCentury.util.StatusBarUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 注册补充资料
 */
public class SignUpAppendActivity extends SwipeBackActivity implements View.OnClickListener{

    Intent intent;
    RelativeLayout sapp_back;
    EditText sapp_et;
    TextView sapp_submit;
    RadioButton rb_male;
    RadioButton rb_female;
    LinearLayout statusBar;

    //同盾校验
    String blackBox = "";
    String validateToken = "";
    //手机号
    String mobile = "";
    //验证码
    String checkCode = "";
    String checkCodeToken = "";
    //姓名
    String name;
    //性别
    String sex;

    RelativeLayout loading;

    final static String TAG = SignUpAppendActivity.class.getName();

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
                        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(SignUpAppendActivity.this);
                        sharedPreferencesHelper.put(SharedPref.LOGINED,true);
                        String token = jo.getAsJsonObject("data").get("token").getAsString();
                        sharedPreferencesHelper.put(SharedPref.TOKEN,token);
                        HttpHelper.setAuthorization(token);
                        Toast.makeText(SignUpAppendActivity.this,"注册成功",Toast.LENGTH_LONG).show();
                        intent = new Intent(SignUpAppendActivity.this,MainActivity.class);
                        startActivity(intent);
                    }else{
                        Toast.makeText(SignUpAppendActivity.this,message,Toast.LENGTH_LONG).show();
                    }
                }
            }catch (Exception e){
                Log.e(TAG, "Exception: " + e.getMessage());
                Toast.makeText(SignUpAppendActivity.this,"连接超时",Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_append);
        StatusBarUtils.with(this).init();
        initComponent();
        initEvent();

        Intent i = getIntent();
        blackBox = i.getStringExtra("blackBox");
        validateToken = i.getStringExtra("validateToken");
        mobile = i.getStringExtra("mobile");
        checkCode = i.getStringExtra("checkCode");
        checkCodeToken = i.getStringExtra("checkCodeToken");
    }

    void initComponent(){
        sapp_back = (RelativeLayout) findViewById(R.id.sapp_back);
        sapp_et = (EditText) findViewById(R.id.sapp_et_1);
        sapp_submit = (TextView) findViewById(R.id.sapp_submit);

        loading = (RelativeLayout) findViewById(R.id.sapp_loading);

        rb_male = (RadioButton) findViewById(R.id.sapp_male);
        rb_female = (RadioButton) findViewById(R.id.sapp_female);

        //设置radiobutton图片大小
        Drawable drawable_news = getResources().getDrawable(R.drawable.radio_select);
        drawable_news.setBounds(0, 0, 50, 50);
        rb_male.setCompoundDrawables(drawable_news, null, null, null);

        Drawable drawable_female = getResources().getDrawable(R.drawable.radio_select);
        drawable_female.setBounds(0, 0, 50, 50);
        rb_female.setCompoundDrawables(drawable_female, null, null, null);

        //调整通知栏高度
        statusBar = (LinearLayout) findViewById(R.id.sapp_status_bar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this);
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        statusBar.setLayoutParams(layoutParams);
    }

    void initEvent(){
        sapp_back.setOnClickListener(this);
        sapp_submit.setOnClickListener(this);
    }

    void signUp(){
        new Thread(){
            @Override
            public void run() {
                String url = Const.SIGN_UP;
                Gson gson = new Gson();
                SignUpReq req = new SignUpReq();
                req.setMobile(mobile);
                req.setCheckCode(checkCode);
                req.setCheckCodeToken(checkCodeToken);
                req.setValidateToken(validateToken);
                req.setBlackBox(blackBox);
                req.setNameCN(name);
                req.setSex(sex);
                req.setNeedLogin(true);
                req.setSource(Const.SOURCE_TYPE);
                SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(SignUpAppendActivity.this);
                req.setDeviceId(sharedPreferencesHelper.get(SharedPref.DEVICE_TOKEN,"").toString());
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
                        msg.what = 1;
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
            case R.id.sapp_back:
                finish();
                break;
            case R.id.sapp_submit:
                name = sapp_et.getText().toString().trim();
                sex = rb_male.isChecked() ? "M" : "F";
                if(name == null || name.isEmpty()){
                    Toast.makeText(this,"姓名不能为空",Toast.LENGTH_LONG).show();
                    return;
                }

                startLoading();
                signUp();
                break;
        }
    }
}
