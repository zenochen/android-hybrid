package com.NewCenturyHotels.NewCentury.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.App;
import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.cons.AppInfo;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;
import com.NewCenturyHotels.NewCentury.req.ChangePasswordReq;
import com.NewCenturyHotels.NewCentury.util.HttpHelper;
import com.NewCenturyHotels.NewCentury.util.SharedPreferencesHelper;
import com.NewCenturyHotels.NewCentury.util.StatusBarUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 重置密码、忘记密码
 */
public class SetPwdActivity extends SwipeBackActivity implements View.OnClickListener{

    RelativeLayout setpwd_back;
    EditText setpwd_et;
    ImageView setpwd_eye;
    TextView setpwd_submit;

    RelativeLayout loading;
    LinearLayout statusBar;

    //会员卡数据
    Spinner setpwd_sp;
    List<Map<String,Object>> spData;
    SimpleAdapter cardAdapter;

    Intent intent;

    //同盾校验
    String blackBox = "";
    String validateToken = "";
    //修改的手机号邮箱
    String mobileOrEmail = "";
    //验证码
    String checkCode = "";
    String checkCodeToken = "";

    //卡号
    String carNo;
    //新密码
    String password;
    //密码是否隐藏
    Boolean isHidden = true;
    
    final static String TAG = SetPwdActivity.class.getName();

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try{
                stopLoading();
                if(msg.what == 1){//修改密码
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        if((Boolean) (App.mInfo.get(AppInfo.RESET_PWD))){
                            SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(SetPwdActivity.this);
                            sharedPreferencesHelper.put(SharedPref.LOGINED,false);
                            sharedPreferencesHelper.put(SharedPref.TOKEN,"");
                            HttpHelper.setAuthorization("");
                        }
                        intent = new Intent(SetPwdActivity.this,SignInActivity.class);
                        intent.putExtra("canBack",false);
                        startActivity(intent);
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(SetPwdActivity.this);
                    }else{
                        Toast.makeText(SetPwdActivity.this,message,Toast.LENGTH_LONG).show();
                    }
                }else if(msg.what == 2){//获取会员卡列表
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 10406){
                        JsonObject data = jo.getAsJsonObject("data");
                        JsonArray array = data.getAsJsonArray("cardNoListList");
                        spData.clear();
                        for(int i = 0;i < array.size(); i++){
                            Map<String,Object> map = new HashMap<>();
                            JsonObject cardjo = array.get(i).getAsJsonObject();
                            map.put("cardNo",cardjo.get("cardNo").getAsString());
                            map.put("cardType",cardjo.get("cardLevelName").getAsString());
                            spData.add(map);
                        }
                        cardAdapter.notifyDataSetChanged();
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(SetPwdActivity.this);
                    }else{
                        Toast.makeText(SetPwdActivity.this,message,Toast.LENGTH_LONG).show();
                    }
                }
            }catch (Exception e){
                Log.e(TAG, "Exception: " + e.getMessage());
                Toast.makeText(SetPwdActivity.this,"连接超时",Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pwd);
        StatusBarUtils.with(this).init();
        initComponent();
        initEvent();
        initSpData();
    }

    void initComponent(){
        setpwd_back = (RelativeLayout) findViewById(R.id.setpwd_back);
        setpwd_et = (EditText) findViewById(R.id.setpwd_et);
        setpwd_eye = (ImageView) findViewById(R.id.setpwd_iv_eye);
        setpwd_submit = (TextView) findViewById(R.id.setpwd_submit);
        setpwd_sp = (Spinner) findViewById(R.id.setpwd_sp);

        loading = (RelativeLayout) findViewById(R.id.setpwd_loading);

        //调整通知栏高度
        statusBar = (LinearLayout) findViewById(R.id.setpwd_status_bar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this);
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        statusBar.setLayoutParams(layoutParams);

        blackBox = getIntent().getStringExtra("blackBox");
        validateToken = getIntent().getStringExtra("validateToken");
        mobileOrEmail = getIntent().getStringExtra("mobileOrEmail");
        checkCode = getIntent().getStringExtra("checkCode");
        checkCodeToken = getIntent().getStringExtra("checkCodeToken");
    }

    void initSpData(){

        spData = new ArrayList<>();
        String[] from = new String[]{"cardNo","cardType"};
        int[] to = new int[]{R.id.spcard_cardno,R.id.spcard_cardtype};

        cardAdapter = new SimpleAdapter(this,spData,R.layout.member_card_sp_item,from,to);
        setpwd_sp.setAdapter(cardAdapter);

        new Thread(){
            @Override
            public void run() {
                String url = Const.MODIFY_PWD;
                Gson gson = new Gson();
                ChangePasswordReq req = new ChangePasswordReq();
                req.setBlackBox(blackBox);
                req.setCheckCode(checkCode);
                req.setCheckCodeToken(checkCodeToken);
                req.setUserName(mobileOrEmail);
                req.setValidateToken(validateToken);
                String json = gson.toJson(req);
                HttpHelper.sendOkHttpPost(url, json, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "onFailure: "+e.getMessage());
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

    void initEvent(){
        setpwd_back.setOnClickListener(this);
        setpwd_eye.setOnClickListener(this);
        setpwd_submit.setOnClickListener(this);
    }

    void modifyPwd(){
        new Thread(){
            @Override
            public void run() {
                String url = Const.MODIFY_PWD;
                Gson gson = new Gson();
                ChangePasswordReq req = new ChangePasswordReq();
                req.setBlackBox(blackBox);
                req.setCardNo(carNo);
                req.setCheckCode(checkCode);
                req.setCheckCodeToken(checkCodeToken);
                req.setPassword(password);
                req.setUserName(mobileOrEmail);
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
        }.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setpwd_back:
                finish();
                break;
            case R.id.setpwd_iv_eye:
                if(isHidden){//显示密码
                    setpwd_et.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    setpwd_et.setSelection(setpwd_et.getText().toString().length());
                    isHidden = false;
                    setpwd_eye.setBackgroundResource(R.drawable.textfield_openeye);
                }else{//隐藏密码
                    setpwd_et.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    setpwd_et.setSelection(setpwd_et.getText().toString().length());
                    isHidden = true;
                    setpwd_eye.setBackgroundResource(R.drawable.textfield_closeeye);
                }
                break;
            case R.id.setpwd_submit:
                password = setpwd_et.getText().toString().trim();
                if(password.isEmpty() || password.length() < 6){
                    Toast.makeText(SetPwdActivity.this,"请输入至少6位密码",Toast.LENGTH_LONG).show();
                    return;
                }
                carNo = (String) spData.get(setpwd_sp.getSelectedItemPosition()).get("cardNo");

                startLoading();
                modifyPwd();
                break;
        }
    }
}
