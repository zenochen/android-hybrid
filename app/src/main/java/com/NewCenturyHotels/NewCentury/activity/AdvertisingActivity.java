package com.NewCenturyHotels.NewCentury.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.bean.Advertising;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;
import com.NewCenturyHotels.NewCentury.req.AdvertiseReq;
import com.NewCenturyHotels.NewCentury.util.HttpHelper;
import com.NewCenturyHotels.NewCentury.util.RandomUtil;
import com.NewCenturyHotels.NewCentury.util.SharedPreferencesHelper;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 广告页
 */
public class AdvertisingActivity extends AppCompatActivity implements View.OnClickListener{

    //跳过广告
    TextView ad_timer;
    //广告图片
    ImageView ad_bg;
    int i = 5;
    Boolean isRunning = true;

    Thread thread;

    SharedPreferencesHelper sharedPreferencesHelper;

    final static String TAG = AdvertisingActivity.class.getName();

    //倒计时
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try{
                if(msg.what == 2){
                    String timer = ad_timer.getText().toString();
                    Integer t = Integer.parseInt(timer.substring(0,1));
                    t = t-1;
                    ad_timer.setText(t + "秒");
                    if(t == 0 && isRunning){
                        toMain();
                    }
                }
            }catch (Exception e){
                Log.e(TAG, "Exception: " + e.getMessage());
                Toast.makeText(AdvertisingActivity.this,"连接超时",Toast.LENGTH_LONG).show();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_advertising);

        initView();
        startTimer();
    }

    void initView(){
        ad_timer = findViewById(R.id.ad_tv_timer);
        ad_bg = findViewById(R.id.ad_iv_bg);
        ad_timer.setOnClickListener(this);
        ad_bg.setOnClickListener(this);

        sharedPreferencesHelper = new SharedPreferencesHelper(AdvertisingActivity.this);
        String filePath = sharedPreferencesHelper.get(SharedPref.APP_AD_LOCAL,"").toString();
        if(!filePath.isEmpty()){
            List<File> files = new ArrayList<>();
            String[] array = filePath.split(",");
            for(int i = 0;i < array.length;i++){
                File file = new File(array[i]);
                files.add(file);
            }
            int index = RandomUtil.getRandomIndex(0,files.size()-1);
            if(files.get(index).exists()){
                Glide.with(AdvertisingActivity.this)
                        .load(files.get(index))
                        .into(ad_bg);
            }else{
                String urls = sharedPreferencesHelper.get(SharedPref.APP_AD,"").toString();
                if(!urls.isEmpty()){
                    String[] urlArray = urls.split(",");
                    int random = RandomUtil.getRandomIndex(0,urlArray.length-1);
                    String url = urlArray[random];
                    Glide.with(AdvertisingActivity.this)
                            .load(url)
                            .into(ad_bg);
                }
            }

        }
    }

    void startTimer(){
        //倒计时
        thread = new Thread(){
            @Override
            public void run() {

                while (--i >= 0){
                    try {
                        sleep(1000);
                        handler.sendEmptyMessage(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        thread.start();
    }

    void toMain(){
        Intent intent = new Intent(AdvertisingActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ad_tv_timer:
                isRunning = false;
                toMain();
                break;
            case R.id.ad_iv_bg:
                break;
        }
    }

    @Override
    public void onBackPressed() {

    }
}