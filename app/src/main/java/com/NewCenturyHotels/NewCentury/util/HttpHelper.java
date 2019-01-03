package com.NewCenturyHotels.NewCentury.util;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.activity.MainActivity;
import com.NewCenturyHotels.NewCentury.activity.SignInByCodeActivity;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpHelper {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String PrefixAuthorization = "Bearer ";
    private static String Authorization = "";
    private static final String _Authorization = "Authorization";

    public static String getAuthorization() {
        return Authorization;
    }

    public static void setAuthorization(String authorization) {
        Authorization = authorization;
    }

    public static void reLogin(Context context){
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(context);
        sharedPreferencesHelper.put(SharedPref.LOGINED,false);
        sharedPreferencesHelper.put(SharedPref.HTML5_LOGINED,false);
        sharedPreferencesHelper.put(SharedPref.TOKEN,"");
        HttpHelper.setAuthorization("");
        Toast.makeText(context,"请重新登录",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(context,MainActivity.class);
        intent.putExtra("reLogin",true);
        context.startActivity(intent);
    }

    public static String get(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }

    public static String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("shands","2.0")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }

    //使用Get方式获取服务器上数据
    public static void sendOkHttpGet(final String address, final okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);
    }

    //使用Post方式向服务器上提交数据并获取返回提示数据
    public static void sendOkHttpPost(String address, final String json, final okhttp3.Callback callback) {
        RequestBody body = RequestBody.create(JSON, json);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(60, TimeUnit.SECONDS)//设置写入超时时间
                .build();
        Request request;

        long timestamp = System.currentTimeMillis();
        String token = ApiSignUtil.appSecret + "&" + ApiSignUtil.appkey + "&" + json + "&" + timestamp;
        token = MD5Util.encryp(token);

        if(Authorization.isEmpty()){
            request = new Request.Builder()
                    .url(address)
                    .addHeader("shands","2.0")
                    .addHeader("appkey",ApiSignUtil.appkey)
                    .addHeader("timestamp",String.valueOf(timestamp))
                    .addHeader("token",token)
                    .addHeader("v",ApiSignUtil.getV())
                    .post(body).build();
        }else{
            request = new Request.Builder()
                    .url(address)
                    .addHeader("shands","2.0")
                    .addHeader("appkey",ApiSignUtil.appkey)
                    .addHeader("timestamp",String.valueOf(timestamp))
                    .addHeader("token",token)
                    .addHeader("v",ApiSignUtil.getV())
                    .addHeader(_Authorization,PrefixAuthorization + Authorization)
                    .post(body).build();
        }
        client.newCall(request).enqueue(callback);
    }

    public static String doJsonPost(String urlPath, String Json) {
        // HttpClient 6.0被抛弃了
        String result = "";
        BufferedReader reader = null;
        try {
            URL url = new URL(urlPath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            // 设置文件类型:
            conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");
            // 设置接收类型否则返回415错误
            //conn.setRequestProperty("accept","*/*")此处为暴力方法设置接受所有类型，以此来防范返回415;
            conn.setRequestProperty("accept","application/json");
            // 往服务器里面发送数据
            if (Json != null && !TextUtils.isEmpty(Json)) {
                byte[] writebytes = Json.getBytes();
                // 设置文件长度
                conn.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
                OutputStream outwritestream = conn.getOutputStream();
                outwritestream.write(Json.getBytes());
                outwritestream.flush();
                outwritestream.close();
                Log.d("hlhupload", "doJsonPost: conn"+conn.getResponseCode());
            }
            if (conn.getResponseCode() == 200) {
                reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                result = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
