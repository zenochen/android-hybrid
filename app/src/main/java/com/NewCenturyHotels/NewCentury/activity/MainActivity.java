package com.NewCenturyHotels.NewCentury.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.App;
import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.bean.AppVersionRes;
import com.NewCenturyHotels.NewCentury.cons.AppInfo;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;
import com.NewCenturyHotels.NewCentury.fragment.HomeFragment;
import com.NewCenturyHotels.NewCentury.fragment.OrderFragment;
import com.NewCenturyHotels.NewCentury.fragment.SocialFragment;
import com.NewCenturyHotels.NewCentury.fragment.UserFragment;
import com.NewCenturyHotels.NewCentury.util.AppUtils;
import com.NewCenturyHotels.NewCentury.util.DownloadUtil;
import com.NewCenturyHotels.NewCentury.util.SharedPreferencesHelper;
import com.NewCenturyHotels.NewCentury.util.StatusBarUtils;
import com.NewCenturyHotels.NewCentury.view.AppVersionDialog;
import com.hjm.bottomtabbar.BottomTabBar;

import java.io.File;

/**
 * 首页
 */
public class MainActivity extends AppCompatActivity{

    private BottomTabBar mBottomTabBar;
    int tabIndex;
    RelativeLayout main_prog;
    TextView prog_tv;
    ProgressBar prog_pb;
    RelativeLayout prog_dismiss;
    Boolean isBackRunning = false;

    //通知栏通知
    NotificationManager notificationManager;
    Notification.Builder builder;
    Notification notification;
    AppVersionRes appVersionRes;
    AppVersionDialog versionDialog;

    String apkName = "default.apk";

    final static String TAG = MainActivity.class.getName();

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                setProgressBar(msg.getData().getInt("progress"));
            }else if(msg.what == 2){
                main_prog.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initViews();//初始化控件
        //重新登录
        Boolean reLogin = getIntent().getBooleanExtra("reLogin",false);
        if(reLogin){
            Intent intent = new Intent(MainActivity.this,SignInByCodeActivity.class);
            startActivity(intent);
        }
        //广告页跳转
        String url = getIntent().getStringExtra("url");
        if(url != null){
            Intent intent = new Intent(MainActivity.this,Html5Activity.class);
            intent.putExtra("needNotLogin",true);
            intent.putExtra("url",url);
            startActivity(intent);
        }
        //请求权限
        initPermission();
        //检测版本
        checkAppVersion();

        StatusBarUtils.with(this).init();
    }

    //初始化控件
    private void initViews() {

        main_prog = (RelativeLayout) findViewById(R.id.main_prg);
        prog_tv = (TextView) findViewById(R.id.main_prg_progress);
        prog_pb = (ProgressBar) findViewById(R.id.main_prg_progressbar);
        prog_dismiss = (RelativeLayout) findViewById(R.id.main_prg_dismiss);
        prog_dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main_prog.setVisibility(View.GONE);
                initNotification();
                isBackRunning = true;
            }
        });

        mBottomTabBar = (BottomTabBar) findViewById(R.id.bottom_tab_bar);

        mBottomTabBar
                .init(getSupportFragmentManager())//初始化方法，必须第一个调用；传入参数为V4包下的FragmentManager
                .setImgSize(25,25)//设置ICON图片的尺寸
                .setFontSize(12)//设置文字的尺寸
                .setTabPadding(5,0,5)//设置ICON图片与上部分割线的间隔、图片与文字的间隔、文字与底部的间隔
                .setChangeColor(Color.BLACK,Color.GRAY)//设置选中的颜色、未选中的颜色
                .addTabItem("首页", R.drawable.tab_home_active, R.drawable.tab_home_inactive,HomeFragment.class)//设置文字、一张图片、fragment
                .addTabItem("种草", R.drawable.tab_social_active, R.drawable.tab_social_inactive, SocialFragment.class)//设置文字、两张图片、fragment
                .addTabItem("订单", R.drawable.tab_order_active, R.drawable.tab_order_inactive, OrderFragment.class)
                .addTabItem("我的", R.drawable.tab_user_active, R.drawable.tab_user_inactive, UserFragment.class)
                .isShowDivider(false)//设置是否显示分割线
                .setTabBarBackgroundColor(Color.WHITE)//设置底部导航栏颜色
                .setOnTabChangeListener(new BottomTabBar.OnTabChangeListener() {
                    @Override
                    public void onTabChange(int position, String name, View view) {
                        Log.i("TGA", "位置：" + position + "选项卡的文字内容：" + name);
                        if(position == 1){
                            SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(MainActivity.this);
                            Boolean logined = (Boolean) sharedPreferencesHelper.get(SharedPref.LOGINED,false);
                            if(!logined){
                                mBottomTabBar.setCurrentTab(0);
                                App.mInfo.put(AppInfo.TAB_INDEX,1);
                                Intent intent = new Intent(MainActivity.this,SignInByCodeActivity.class);
                                startActivity(intent);
                            }
                        }
                    }//添加选项卡切换监听
                });

        tabIndex = getIntent().getIntExtra("tabIndex",0);
        if(App.mInfo.get(AppInfo.TAB_INDEX) != null){
            tabIndex = (Integer) App.mInfo.get(AppInfo.TAB_INDEX);
        }
        mBottomTabBar.setCurrentTab(tabIndex);
        App.mInfo.put(AppInfo.TAB_INDEX,null);
    }

    public void setProgressBar(int progress){
        prog_tv.setText(progress + "%");
        prog_pb.setProgress(progress);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startInstallPermissionSettingActivity() {
        //注意这个是8.0新API
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
        startActivityForResult(intent, 10086);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10086) {
            handleAppUpdate();
        }
    }

    void handleAppUpdate(){
        if(appVersionRes != null){
            main_prog.setVisibility(View.VISIBLE);
            main_prog.setOnClickListener(null);
            downFile(appVersionRes.getDownloadUrl());
//            downFile("http://a8.pc6.com/ysx7/zhangyue780.apk");
            versionDialog.dismiss();
            Toast.makeText(MainActivity.this,"正在后台下载，请等待",Toast.LENGTH_LONG).show();
        }
    }

    private void initNotification() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new Notification.Builder(this);
        builder.setContentTitle("正在更新...") //设置通知标题
                .setSmallIcon(R.mipmap.icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.icon)) //设置通知的大图标
                .setDefaults(Notification.DEFAULT_LIGHTS) //设置通知的提醒方式： 呼吸灯
                .setPriority(Notification.PRIORITY_MAX) //设置通知的优先级：最大
                .setAutoCancel(false)//设置通知被点击一次是否自动取消
                .setContentText("下载进度:" + "0%")
                .setProgress(100, 0, false)
                .setWhen(System.currentTimeMillis()).setNumber(1);
        notification = builder.build();//构建通知对象
        notificationManager.notify(1, notification);
    }

    public void downFile(String url) {
        String fileDir = "";
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.M){//Android 6.0
            fileDir = AppInfo.RES_DIR;
        }else{
            fileDir = this.getFilesDir().getAbsolutePath() + "/shands/";
        }

        try{
            DownloadUtil.get().download(url, fileDir, apkName, new DownloadUtil.OnDownloadListener() {
                @Override
                public void onDownloadSuccess(File file) {
                    Log.e("update", "onSuccess: 下载完成" + file.getPath());
                    if(isBackRunning){
                        notificationManager.cancel(1);
                        builder.setContentTitle("下载完成")
                                .setContentText("点击安装")
                                .setAutoCancel(true);//设置通知被点击一次是否自动取消
                        builder.setProgress(100, 100, false);
                        notification = builder.setContentIntent(getNotifyIntent(file)).build();
                        notificationManager.notify(2, notification);
                    }else{
                        handler.sendEmptyMessage(2);
                    }
                    //打开apk
                    startActivity(getInstallIntent(file));
                }

                @Override
                public void onDownloading(int progress) {
                    if(isBackRunning){
                        builder.setProgress(100, progress, false);
                        builder.setContentText("下载进度:" + progress + "%");
                        notification = builder.build();
                        notificationManager.notify(1, notification);
                    }else{
                        Message message = new Message();
                        message.what = 1;
                        Bundle data = new Bundle();
                        data.putInt("progress",progress);
                        message.setData(data);
                        handler.sendMessage(message);
                    }
                }

                @Override
                public void onDownloadFailed(Exception e) {
                    //下载异常进行相关提示操作
                    Log.e("failed", "onDownloadFailed: 下载异常，请重新再试");
                    Toast.makeText(MainActivity.this,"下载异常，请重新再试",Toast.LENGTH_LONG).show();
                }
            });
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
            Toast.makeText(MainActivity.this,"网络异常",Toast.LENGTH_LONG).show();
        }

    }

    public String getMIMEType(File var0) {
        String var1 = "";
        String var2 = var0.getName();
        String var3 = var2.substring(var2.lastIndexOf(".") + 1, var2.length()).toLowerCase();
        var1 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(var3);
        return var1;
    }

    private PendingIntent getNotifyIntent(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (file.exists()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//Android 7.0
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(FileProvider.getUriForFile(this, AppInfo.FILE_AUTHORITY, file), AppInfo.INTENT_TYPE_OPEN_APK);
            }else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M){//Android 6.0
                intent.setDataAndType(Uri.fromFile(file), getMIMEType(file));
            } else {////Android 6.0以下
                intent.setDataAndType(Uri.fromFile(file), AppInfo.INTENT_TYPE_OPEN_APK);
            }
        }
        return PendingIntent.getActivity(this, 0, intent, 0);
    }

    private Intent getInstallIntent(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (file.exists()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//Android 7.0
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(FileProvider.getUriForFile(this, AppInfo.FILE_AUTHORITY, file), AppInfo.INTENT_TYPE_OPEN_APK);
            } else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M){//Android 6.0
                intent.setDataAndType(Uri.fromFile(file), getMIMEType(file));
            } else {////Android 6.0以下
                intent.setDataAndType(Uri.fromFile(file), AppInfo.INTENT_TYPE_OPEN_APK);
            }
        }
        return intent;
    }

    void checkAppVersion(){
        int currentVersion = AppUtils.getVersionCode(MainActivity.this);
        appVersionRes = (AppVersionRes) App.mInfo.get(AppInfo.APP_VERSION);
        if(appVersionRes != null){
            int remoteVersion = Integer.parseInt(appVersionRes.getAppVersion());
            if(remoteVersion > currentVersion){
                versionDialog = new AppVersionDialog(this,R.style.dialog,"", new AppVersionDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if(confirm){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//Android 8.0
                                if (getPackageManager().canRequestPackageInstalls()) {
                                    handleAppUpdate();
                                } else {
                                    // 申请权限。
                                    startInstallPermissionSettingActivity();
                                }
                            }else{
                                handleAppUpdate();
                            }
                        }else{
                            if(appVersionRes.getIfForcedUpdate() == 1){
                                Toast.makeText(MainActivity.this,"必须升级新版本，app才能正常使用！",Toast.LENGTH_LONG).show();
                            }else{
                                versionDialog.dismiss();
                            }
                        }
                    }
                });
                versionDialog.setCancelable(false);
                versionDialog.setTitle("版本升级(" + appVersionRes.getAppVersionName() + ")");
                versionDialog.setContent(appVersionRes.getDescription());
                versionDialog.show();
                apkName = appVersionRes.getDownloadUrl().substring(appVersionRes.getDownloadUrl().lastIndexOf("/")+1);
            }
        }
    }

    public void initPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA,
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onBackPressed() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }
}
