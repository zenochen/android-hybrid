package com.NewCenturyHotels.NewCentury.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class PayUtil {

    final String pay_api_path = "http://test1.shands.cn/rest/api.do?api=trade.pay&appkey=100001";

    private static final String TN_URL_01 = "http://101.231.204.84:8091/sim/getacptn";

    public String getUnionPayTn(){

        String tn = HttpUtil.httpGet(TN_URL_01);

        return tn;
    }

    public JSONObject pay(String payWay) throws UnsupportedEncodingException {

        String data = "{\"tradeNo\"" + ":\"M170825107112\",";
        data += "\"payWay\"" + ":\""+payWay+"\",";
        data += "\"userToken\"" + ":\"p38jJPXj8qiw0a6a_1511327936290\"}";

        data = URLEncoder.encode(data,"utf-8");
        String path = pay_api_path + "&data=" + data;
        String json = HttpUtil.httpGet(path);

        JSONObject jsonObject = null;
        JSONObject datas = null;
        try {
            jsonObject = new JSONObject(json);

            if(jsonObject != null){
                String ret = jsonObject.getString("ret");
                if(ret.equals("200")){
                    datas = jsonObject.getJSONObject("data");
                }else{
                    return null;
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return datas;
    }

    public Map<String,Object> convertToMap(String con){
        String[] kv = con.split("&");
        Map<String,Object> map = new HashMap<>();

        for (String string:kv) {
            String[] _kv = string.split("=");
            map.put(_kv[0],_kv[1]);
        }

        return map;
    }

}
