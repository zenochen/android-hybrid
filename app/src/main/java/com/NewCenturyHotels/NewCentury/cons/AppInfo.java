package com.NewCenturyHotels.NewCentury.cons;

import android.os.Environment;

public class AppInfo {
    //全局变量的key
    public static final String TAB_INDEX = "tabIndex";//身份认证的类型
    public static final String RESET_PWD = "reset_pwd";//是否重置密码
    public static final String CHANGE_MOBILE = "change_mobile";//是否修改手机号
    public static final String APP_VERSION = "app_version";//app版本信息
    //资源文件目录
    public static final String RES_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/shands/";
    //打开文件权限
    public static final String FILE_AUTHORITY = "com.NewCenturyHotels.NewCentury.fileprovider";
    public static final String INTENT_TYPE_OPEN_APK = "application/vnd.android.package-archive";
}
