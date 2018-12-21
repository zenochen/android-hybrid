package com.NewCenturyHotels.NewCentury.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;
import com.NewCenturyHotels.NewCentury.util.SharedPreferencesHelper;

/**
 * 欢迎页
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //避免重新启动APP
        if((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0){
            finish();
            return;
        }
        //全屏显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        setContentView(R.layout.activity_welcome);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferencesHelper helper = new SharedPreferencesHelper(WelcomeActivity.this);
                Boolean ifFirst = (Boolean) helper.get(SharedPref.IS_FIRST,true);
                if(ifFirst){
                    Intent intent = new Intent(WelcomeActivity.this,GuideActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Intent intent = new Intent(WelcomeActivity.this,AdvertisingActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        },1500);
    }
}
