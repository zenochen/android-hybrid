package com.NewCenturyHotels.NewCentury.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;

public class HttpUtil {

    public static String httpGet(String url){

        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        String content = "";

        try {
            HttpResponse response = client.execute(get);
            if(response.getStatusLine().getStatusCode() == 200){
                HttpEntity entity = response.getEntity();
                content = EntityUtils.toString(entity,"utf-8");//将entity当中的数据转换为字符串
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }

    public static String httpPost(String url,List<NameValuePair> args){

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        String content = "";

        try {
            post.setEntity(new UrlEncodedFormEntity(args, HTTP.UTF_8));
            HttpResponse response = client.execute(post);
            if(response.getStatusLine().getStatusCode() == 200){
                HttpEntity entity = response.getEntity();
                content = EntityUtils.toString(entity,"utf-8");//将entity当中的数据转换为字符串
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }

    /**
     * 获取URL的域名
     */
    public static String getDomain(String url) {
        url = url.replace("http://", "").replace("https://", "");
        if (url.contains("/")) {
            url = url.substring(0, url.indexOf('/'));
        }
        return url;
    }

}