package com.NewCenturyHotels.NewCentury.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.App;
import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.bean.Person;
import com.NewCenturyHotels.NewCentury.cons.AppInfo;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.util.HttpHelper;
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
 * 个人中心
 */
public class PersonalActivity extends SwipeBackActivity implements View.OnClickListener{

    Intent intent;
    TextView tv_set_pwd;
    TextView tv_modify;
    RelativeLayout re_mobile;
    RelativeLayout re_email;
    RelativeLayout back;
    RelativeLayout loading;
    //个人信息
    TextView tv_name;
    TextView tv_sex;
    TextView tv_birthday;
    TextView tv_card_type;
    TextView tv_card_no;
    TextView tv_mobile;
    TextView tv_email;
    TextView tv_process;

    LinearLayout statusBar;

    int finishProcess;

    Boolean isMobileModify = false;
    Boolean isEmailModify = false;

    //个人信息数据
    Person person;

    final String TAG = PersonalActivity.class.getName();

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try{
                stopLoading();
                if(msg.what == 1){//获取个人信息
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        JsonObject data = jo.getAsJsonObject("data").getAsJsonObject("memberInfo");
                        Gson gson = new Gson();
                        person = gson.fromJson(data,Person.class);
                        if(person != null){
                            tv_name.setText(person.getNameCN());
                            if(person.getSex().equals("M")){
                                tv_sex.setText("男");
                            }else{
                                tv_sex.setText("女");
                            }

                            tv_birthday.setText(person.getBirthday());
                            tv_card_type.setText(person.getIdName());
                            tv_card_no.setText(person.getIdentityNO());
                            tv_mobile.setText(person.getMobile());
                            tv_email.setText(person.getEmail());
                            if(!person.getNameCN().isEmpty()){
                                finishProcess += 20;
                            }
                            if(!person.getSex().isEmpty()){
                                finishProcess += 20;
                            }
                            if(!person.getBirthday().isEmpty()){
                                finishProcess += 20;
                            }
                            if(!person.getIdName().isEmpty()){
                                finishProcess += 20;
                            }
                            if(!person.getIdentityNO().isEmpty()){
                                finishProcess += 20;
                            }
                            tv_process.setText(finishProcess + "%");
                        }

                        JsonObject jsonObject = jo.getAsJsonObject("data");
                        Boolean isModify = jsonObject.get("isModify").getAsBoolean();
                        if(isModify){
                            tv_modify.setVisibility(View.GONE);
                        }

                        isMobileModify = jsonObject.get("isMobileModify").getAsBoolean();
                        isEmailModify = jsonObject.get("isEmailModify").getAsBoolean();
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(PersonalActivity.this);
                    }else{
                        Toast.makeText(PersonalActivity.this,message,Toast.LENGTH_SHORT).show();
                    }
                }
            }catch (Exception e){
                Log.e(TAG, "Exception: " + e.getMessage());
                Toast.makeText(PersonalActivity.this,"连接超时",Toast.LENGTH_LONG).show();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);
        StatusBarUtils.with(this).init();
        initComponent();
        initEvent();
    }

    void initComponent(){
        tv_modify = (TextView) findViewById(R.id.pers_tv_modify);
        tv_set_pwd = (TextView) findViewById(R.id.pers_tv_setpwd);
        re_mobile = (RelativeLayout) findViewById(R.id.pers_mobile);
        re_email = (RelativeLayout) findViewById(R.id.pers_email);
        back = (RelativeLayout) findViewById(R.id.pers_back);
        loading = (RelativeLayout) findViewById(R.id.pers_loading);

        tv_name = (TextView) findViewById(R.id.pers_tv_name);
        tv_sex = (TextView) findViewById(R.id.pers_tv_sex);
        tv_birthday = (TextView) findViewById(R.id.pers_tv_birthday);
        tv_card_type = (TextView) findViewById(R.id.pers_tv_cardtype);
        tv_card_no = (TextView) findViewById(R.id.pers_tv_cardinfo);
        tv_mobile = (TextView) findViewById(R.id.pers_tv_mobile);
        tv_email = (TextView) findViewById(R.id.pers_tv_email);
        tv_process = (TextView) findViewById(R.id.pers_tv_process);

        //调整通知栏高度
        statusBar = (LinearLayout) findViewById(R.id.pers_status_bar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this);
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        statusBar.setLayoutParams(layoutParams);
    }

    void initEvent(){
        tv_modify.setOnClickListener(this);
        tv_set_pwd.setOnClickListener(this);
        re_mobile.setOnClickListener(this);
        re_email.setOnClickListener(this);
        back.setOnClickListener(this);

        initPersonInfo();
    }

    void initPersonInfo(){
        startLoading();
        new Thread(){
            @Override
            public void run() {
                String url = Const.VIP_MEMBER;
                String json = "{}";
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            initPersonInfo();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pers_back:
                finish();
                break;
            case R.id.pers_tv_setpwd:
                App.mInfo.put(AppInfo.RESET_PWD,true);
                intent = new Intent(PersonalActivity.this,SetPwdAuthActivity.class);
                startActivity(intent);
                break;
            case R.id.pers_tv_modify:
                intent = new Intent(PersonalActivity.this,PersonalBaseActivity.class);
                if(person != null){
                    intent.putExtra("name",person.getNameCN());
                    intent.putExtra("sex",person.getSex());
                    intent.putExtra("birthday",person.getBirthday());
                    intent.putExtra("idType",person.getIdType());
                    intent.putExtra("idNo",person.getIdentityNO());
                    startActivityForResult(intent,1);
                }
                break;
            case R.id.pers_mobile:
                if(isMobileModify){
                    Toast.makeText(PersonalActivity.this,"您已修改一次，请联系客服修改",Toast.LENGTH_LONG).show();
                    return;
                }
                App.mInfo.put(AppInfo.CHANGE_MOBILE,true);
                if(person.getMobile().isEmpty()){
                    intent = new Intent(PersonalActivity.this,SetPhoneEmailActivity.class);
                    startActivity(intent);
                }else{
                    intent = new Intent(PersonalActivity.this,ModStep1AuthActivity.class);
                    intent.putExtra("mobileOrEmail",person.getMobile());
                    startActivity(intent);
                }
                break;
            case R.id.pers_email:
                if(isEmailModify){
                    Toast.makeText(PersonalActivity.this,"您已修改一次，请联系客服修改",Toast.LENGTH_LONG).show();
                    return;
                }
                App.mInfo.put(AppInfo.CHANGE_MOBILE,false);
                if(person.getEmail().isEmpty()){
                    intent = new Intent(PersonalActivity.this,SetPhoneEmailActivity.class);
                    startActivity(intent);
                }else{
                    intent = new Intent(PersonalActivity.this,ModStep1AuthActivity.class);
                    intent.putExtra("mobileOrEmail",person.getEmail());
                    startActivity(intent);
                }
                break;
        }
    }
}
