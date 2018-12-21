package com.NewCenturyHotels.NewCentury.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.App;
import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.bean.AppVersionRes;
import com.NewCenturyHotels.NewCentury.cons.AppInfo;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;
import com.NewCenturyHotels.NewCentury.util.DataCleanManager;
import com.NewCenturyHotels.NewCentury.util.HttpHelper;
import com.NewCenturyHotels.NewCentury.util.SharedPreferencesHelper;
import com.NewCenturyHotels.NewCentury.util.StatusBarUtils;
import com.NewCenturyHotels.NewCentury.view.CommomDialog;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

/**
 * 设置
 */
public class SettingsActivity extends SwipeBackActivity implements View.OnClickListener{

    RelativeLayout setting_back;
    RelativeLayout setting_club;
    RelativeLayout setting_rules;
    RelativeLayout setting_clear;
    RelativeLayout setting_version;
    RelativeLayout setting_logout;
    TextView currentVersion;
    Intent intent;
    LinearLayout statusBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        StatusBarUtils.with(this).init();
        initComponent();
        initEvent();
    }

    void initComponent(){
        setting_back = (RelativeLayout) findViewById(R.id.setting_back);
        setting_club = (RelativeLayout) findViewById(R.id.setting_tv_club);
        setting_rules = (RelativeLayout) findViewById(R.id.setting_tv_rules);
        setting_clear = (RelativeLayout) findViewById(R.id.setting_tv_clear);
        setting_version = (RelativeLayout) findViewById(R.id.setting_version);
        setting_logout = (RelativeLayout) findViewById(R.id.setting_tv_logout);
        currentVersion = (TextView) findViewById(R.id.setting_tv_version);

        if(App.mInfo.get(AppInfo.APP_VERSION) != null){
            String version = ((AppVersionRes) App.mInfo.get(AppInfo.APP_VERSION)).getAppVersionName();
            currentVersion.setText(version);
        }

        //调整通知栏高度
        statusBar = (LinearLayout) findViewById(R.id.setting_status_bar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this);
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        statusBar.setLayoutParams(layoutParams);
    }

    void initEvent(){
        setting_back.setOnClickListener(this);
        setting_club.setOnClickListener(this);
        setting_rules.setOnClickListener(this);
        setting_clear.setOnClickListener(this);
        setting_version.setOnClickListener(this);
        setting_logout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setting_back:
                finish();
                break;
            case R.id.setting_tv_club:
                intent=new Intent(SettingsActivity.this, Html5Activity.class);
                intent.putExtra("url", Const.ABOUT_US);
                startActivity(intent);
                break;
            case R.id.setting_tv_rules:
                intent=new Intent(SettingsActivity.this, Html5Activity.class);
                intent.putExtra("url", Const.CLUB_RULES);
                startActivity(intent);
                break;
            case R.id.setting_tv_clear:
                try {
                    String totalSize = DataCleanManager.getTotalCacheSize(this);
                    CommomDialog cleanDialog = new CommomDialog(this, R.style.dialog, "缓存大小：" + totalSize, new CommomDialog.OnCloseListener() {
                        @Override
                        public void onClick(Dialog dialog, boolean confirm) {
                            if(confirm){
                                DataCleanManager.clearAllCache(SettingsActivity.this);
                                Toast.makeText(SettingsActivity.this,"清除成功",Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        }
                    });
                    cleanDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.setting_tv_version:
                break;
            case R.id.setting_tv_logout:
                CommomDialog dialog =  new CommomDialog(this, R.style.dialog, "您确定退出？", new CommomDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if(confirm){
                            dialog.dismiss();
                            SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(SettingsActivity.this);
                            sharedPreferencesHelper.put(SharedPref.LOGINED,false);
                            sharedPreferencesHelper.put(SharedPref.HTML5_LOGINED,false);
                            sharedPreferencesHelper.put(SharedPref.TOKEN,"");
                            HttpHelper.setAuthorization("");
                            intent = new Intent(SettingsActivity.this,MainActivity.class);
                            intent.putExtra("tabIndex",3);
                            startActivity(intent);
                        }
                    }
                }).setTitle("系统提示");
                dialog.show();
                dialog.getSubmitTxt().setTextColor(Color.parseColor("#FFE0BC5A"));

                break;
        }
    }
}