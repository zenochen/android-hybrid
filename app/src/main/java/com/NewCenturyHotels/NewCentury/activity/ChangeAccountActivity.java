package com.NewCenturyHotels.NewCentury.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.adapter.MemberCardAdapter;
import com.NewCenturyHotels.NewCentury.bean.MemberCard;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;
import com.NewCenturyHotels.NewCentury.req.ChangeCardReq;
import com.NewCenturyHotels.NewCentury.util.HttpHelper;
import com.NewCenturyHotels.NewCentury.util.SharedPreferencesHelper;
import com.NewCenturyHotels.NewCentury.util.StatusBarUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 切换会员卡
 */
public class ChangeAccountActivity extends SwipeBackActivity implements View.OnClickListener{

    RelativeLayout change_back;
    ListView change_lv;
    TextView change_finish;
    RelativeLayout loading;
    LinearLayout statusBar;

    List<MemberCard> cards;
    MemberCardAdapter adapter;
    MemberCard selectedCard;

    String carNo = "";

    final static String TAG = ChangeAccountActivity.class.getName();

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try{
                stopLoading();
                if(msg.what == 1){//获取会员卡列表
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        JsonObject data = jo.getAsJsonObject("data");
                        JsonArray array = data.getAsJsonArray("cardNoListList");
                        String selectedCard = data.get("selectedCardNo").getAsString();
                        cards.clear();
                        for(int i=0;i < array.size();i++){
                            JsonObject jsonObject = array.get(i).getAsJsonObject();
                            Gson gson = new Gson();
                            MemberCard card = gson.fromJson(jsonObject,MemberCard.class);
                            if(card.getCardNo().equals(selectedCard)){
                                card.setSelected(true);
                            }
                            cards.add(card);
                        }

                        if(cards.size() <= 1){
                            change_finish.setVisibility(View.GONE);
                        }
                        adapter.notifyDataSetChanged();
                    }else{
                        Toast.makeText(ChangeAccountActivity.this,message,Toast.LENGTH_LONG).show();
                    }
                }else if(msg.what == 2){//切换会员卡
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        Toast.makeText(ChangeAccountActivity.this,"切换成功",Toast.LENGTH_LONG).show();
                        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(ChangeAccountActivity.this);
                        String token = jo.getAsJsonObject("data").get("token").getAsString();
                        sharedPreferencesHelper.put(SharedPref.TOKEN,token);
                        HttpHelper.setAuthorization(token);
                        finish();
                    }else{
                        Toast.makeText(ChangeAccountActivity.this,message,Toast.LENGTH_LONG).show();
                    }

                }
            }catch (Exception e){
                Log.e(TAG, "Exception: " + e.getMessage());
                Toast.makeText(ChangeAccountActivity.this,"连接超时",Toast.LENGTH_LONG).show();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_account);
        StatusBarUtils.with(this).init();
        initComponent();
        initEvent();
    }

    void initComponent(){
        change_back = (RelativeLayout) findViewById(R.id.change_back);
        change_lv = (ListView) findViewById(R.id.change_lv);
        change_finish = (TextView) findViewById(R.id.change_finish);
        loading = (RelativeLayout) findViewById(R.id.change_loading);

        //调整通知栏高度
        statusBar = (LinearLayout) findViewById(R.id.change_status_bar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this);
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        statusBar.setLayoutParams(layoutParams);
    }

    void initEvent(){

        change_back.setOnClickListener(this);
        change_finish.setOnClickListener(this);
        change_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                for (int j = 0 ; j < cards.size(); j++) {
                    if(i == j){
                        cards.get(j).setSelected(true);
                        selectedCard = cards.get(j);
                    }else{
                        cards.get(j).setSelected(false);
                    }
                }

                adapter.notifyDataSetChanged();
            }
        });

        cards = new ArrayList<>();
        adapter = new MemberCardAdapter(this,cards);
        change_lv.setAdapter(adapter);

        startLoading();
        new Thread(){
            @Override
            public void run() {
                String url = Const.MEMEBER_CARDS;
                String json = "{}";
                HttpHelper.sendOkHttpPost(url, json, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "onFailure: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String body = response.body().string();
                        Log.i(TAG, "onResponse: " + body);
                        Bundle bundle = new Bundle();
                        bundle.putString("ret",body);
                        Message msg = new Message();
                        msg.setData(bundle);
                        msg.what = 1;
                        handler.sendMessage(msg);
                    }
                });
            }
        }.start();
    }

    void changeCard(){
        new Thread(){
            @Override
            public void run() {
                String url = Const.CHANGE_CARD;
                Gson gson = new Gson();
                ChangeCardReq req = new ChangeCardReq();
                req.setCardNo(carNo);
                String json = gson.toJson(req);
                HttpHelper.sendOkHttpPost(url, json, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "onFailure: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String body = response.body().string();
                        Log.i(TAG, "onResponse: " + body);
                        Bundle bundle = new Bundle();
                        bundle.putString("ret",body);
                        Message msg = new Message();
                        msg.setData(bundle);
                        msg.what = 2;
                        handler.sendMessage(msg);
                    }
                });
            }
        }.start();
    }

    void startLoading(){
        loading.setVisibility(View.VISIBLE);
        loading.setOnClickListener(null);
    }

    void stopLoading(){
        loading.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.change_back:
                finish();
                break;
            case R.id.change_finish:
                for (MemberCard card :
                        cards) {
                    if (card.getSelected()) {
                        carNo = card.getCardNo();
                    }
                }
                if(carNo.isEmpty()){
                    Toast.makeText(ChangeAccountActivity.this,"请选择切换的会员卡",Toast.LENGTH_LONG).show();
                    return;
                }

                startLoading();
                changeCard();
                break;
        }
    }
}
