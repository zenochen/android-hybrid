package com.NewCenturyHotels.NewCentury.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FileUtil {

    public boolean checkLastVersion(Context context){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String version = sp.getString("version","1.0.0");
        String versionNew = getLastVersion();

        return version.compareTo(versionNew) == 0;
    }

    public String getLastVersion(){
        HttpUtil.httpGet("");

        return "";
    }

    public void updateVersion(Context context,String version){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("version",version);
        editor.commit();
        Toast.makeText(context,sp.getString("version","1.0.0"),Toast.LENGTH_LONG).show();
    }

    public File downLoadFile(String httpUrl, Context context) {
        // TODO Auto-generated method stub
        final String fileName = "update.apk";
        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/shands/update";

        File tmpFile = new File(filepath);
        if (!tmpFile.exists()) {
            tmpFile.mkdir();
        }
        final File file = new File(filepath + fileName);

        try {
            URL url = new URL(httpUrl);
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStream is = conn.getInputStream();
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buf = new byte[256];
                conn.connect();
                double count = 0;
                if (conn.getResponseCode() >= 400) {
                    Toast.makeText(context, "连接超时", Toast.LENGTH_SHORT).show();
                } else {
                    while (count <= 100) {
                        if (is != null) {
                            int numRead = is.read(buf);
                            if (numRead <= 0) {
                                break;
                            } else {
                                fos.write(buf, 0, numRead);
                            }

                        } else {
                            break;
                        }

                    }
                }

                conn.disconnect();
                fos.close();
                is.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return file;
    }

    //打开APK程序代码
    private void openFile(File file,Context context) {
        // TODO Auto-generated method stub
        Log.e("OpenFile", file.getName());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
        context.startActivity(intent);
    }

}
