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

import com.NewCenturyHotels.NewCentury.App;
import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.bean.AppVersionRes;
import com.NewCenturyHotels.NewCentury.cons.AppInfo;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;
import com.NewCenturyHotels.NewCentury.util.RandomUtil;
import com.NewCenturyHotels.NewCentury.util.SharedPreferencesHelper;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 广告页
 */
public class AdvertisingActivity extends AppCompatActivity implements View.OnClickListener{

    //跳过广告
    TextView ad_timer;
    //广告图片
    ImageView ad_bg;
    int i = 3;
    Boolean isRunning = true;

    Thread thread;

    SharedPreferencesHelper sharedPreferencesHelper;
    String url;

    final static String TAG = AdvertisingActivity.class.getName();

    //倒计时
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try{
                if(msg.what == 2){
                    String timer = ad_timer.getText().toString();
                    Integer t = Integer.parseInt(timer.substring(2,3));
                    t = t-1;
                    ad_timer.setText("跳过" + t + "s");
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
        Boolean ifFirst = (Boolean) sharedPreferencesHelper.get(SharedPref.FIRST_SHOW,true);
        int index = (Integer) sharedPreferencesHelper.get(SharedPref.AD_PIC_INDEX,0);

        String app = sharedPreferencesHelper.get(SharedPref.APP_VERSION,"").toString();
        Gson gson = new Gson();
        AppVersionRes res = gson.fromJson(app,AppVersionRes.class);

        if(ifFirst){
            String url = res.getImgData().getStartUpImg()[index].getAdvertisingImage();
            Glide.with(AdvertisingActivity.this)
                    .load(url)
                    .into(ad_bg);
            sharedPreferencesHelper.put(SharedPref.FIRST_SHOW,false);
        }else{
            sharedPreferencesHelper = new SharedPreferencesHelper(AdvertisingActivity.this);
            String filePath = sharedPreferencesHelper.get(SharedPref.APP_AD_LOCAL,"").toString();

            if(!filePath.isEmpty()){
                List<File> files = new ArrayList<>();
                String[] array = filePath.split(",");
                for(int i = 0;i < array.length;i++){
                    File file = new File(array[i]);
                    files.add(file);
                }
                index = RandomUtil.getRandom(index,files.size()-1);
                sharedPreferencesHelper.put(SharedPref.AD_PIC_INDEX,index);
                if(files.get(index).exists()){
                    Glide.with(AdvertisingActivity.this)
                            .load(files.get(index))
                            .into(ad_bg);
                }else{
                    String urls = sharedPreferencesHelper.get(SharedPref.APP_AD,"").toString();
                    if(!urls.isEmpty()){
                        String[] urlArray = urls.split(",");
                        index = RandomUtil.getRandom(index,urlArray.length-1);
                        sharedPreferencesHelper.put(SharedPref.AD_PIC_INDEX,index);
                        String url = urlArray[index];
                        Glide.with(AdvertisingActivity.this)
                                .load(url)
                                .into(ad_bg);
                    }
                }
            }
        }


        if(!app.isEmpty()){
            url = res.getImgData().getStartUpImg()[index].getRedirectUrl();
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

    void toMain2(String url){
        if(!url.isEmpty()){
            Intent intent = new Intent(AdvertisingActivity.this,MainActivity.class);
            intent.putExtra("url",url);
            intent.putExtra("needNotLogin",true);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        thread.interrupt();
        thread = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ad_tv_timer:
                isRunning = false;
                toMain();
                break;
            case R.id.ad_iv_bg:
                isRunning = false;
                toMain2(url);
                break;
        }
    }

    @Override
    public void onBackPressed() {

    }
}