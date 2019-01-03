package com.NewCenturyHotels.NewCentury.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.adapter.MessageAdapter;
import com.NewCenturyHotels.NewCentury.bean.MessageList;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.util.HttpHelper;
import com.NewCenturyHotels.NewCentury.util.StatusBarUtils;
import com.NewCenturyHotels.NewCentury.view.CommonRefreshHeader;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 消息中心
 */
public class MessageCenterActivity extends SwipeBackActivity implements View.OnClickListener{

    RelativeLayout msg_back;
    ListView msg_lv;
    //下拉刷新
    PtrFrameLayout ptrFrameLayout;
    //暂无数据提示
    PtrFrameLayout ptrFrameLayout0;

    RelativeLayout loading;
    LinearLayout statusBar;

    //数据列表
    List<MessageList> msgs;
    MessageAdapter adapter;

    final static String TAG =  MessageCenterActivity.class.getName();

    Handler handler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            try{
                stopLoading();
                if(msg.what == 1){
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        JsonArray array = jo.getAsJsonArray("data");
                        List<MessageList> list = new ArrayList<>();
                        msgs.clear();
                        for(int i = 0; i < array.size(); i ++){
                            JsonObject object = (JsonObject) array.get(i);
                            Gson gson = new Gson();
                            MessageList item = gson.fromJson(object,MessageList.class);
                            list.add(item);
                        }
                        msgs.addAll(list);
                        adapter.notifyDataSetChanged();
                        if(msgs.size() == 0){
                            ptrFrameLayout0.setVisibility(View.VISIBLE);
                            ptrFrameLayout.setVisibility(View.GONE);
                        }else{
                            ptrFrameLayout0.setVisibility(View.GONE);
                            ptrFrameLayout.setVisibility(View.VISIBLE);
                        }
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(MessageCenterActivity.this);
                    }else{
                        ptrFrameLayout0.setVisibility(View.VISIBLE);
                        ptrFrameLayout.setVisibility(View.GONE);
                        Toast.makeText(MessageCenterActivity.this,message,Toast.LENGTH_LONG).show();
                    }

                    if(ptrFrameLayout0.isRefreshing()){
                        ptrFrameLayout0.refreshComplete();
                    }
                    if(ptrFrameLayout.isRefreshing()){
                        ptrFrameLayout.refreshComplete();
                    }
                }
            }catch (Exception e){
                Log.e(TAG, "Exception: " + e.getMessage());
                Toast.makeText(MessageCenterActivity.this,"连接超时",Toast.LENGTH_LONG).show();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_center);
        StatusBarUtils.with(this).init();
        initComponent();
        initEvent();
    }

    void initComponent(){
        msg_back = (RelativeLayout) findViewById(R.id.msg_back);
        msg_lv = (ListView) findViewById(R.id.msg_lv);
        ptrFrameLayout = (PtrFrameLayout) findViewById(R.id.msg_ptr);
        ptrFrameLayout0 = (PtrFrameLayout) findViewById(R.id.msg_ptr0);
        loading = (RelativeLayout) findViewById(R.id.msg_loading);

        //下拉刷新
        CommonRefreshHeader commonRefreshHeader = new CommonRefreshHeader(this);
        ptrFrameLayout.setHeaderView(commonRefreshHeader);
        ptrFrameLayout.addPtrUIHandler(commonRefreshHeader);

        //下拉刷新监听
        ptrFrameLayout.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                ptrFrameLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshData();
                    }
                },1000);

            }
        });
        //设置模式
        ptrFrameLayout.setMode(PtrFrameLayout.Mode.REFRESH);

        //下拉刷新
        CommonRefreshHeader commonRefreshHeader0 = new CommonRefreshHeader(this);
        ptrFrameLayout0.setHeaderView(commonRefreshHeader0);
        ptrFrameLayout0.addPtrUIHandler(commonRefreshHeader0);

        //下拉刷新监听
        ptrFrameLayout0.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                ptrFrameLayout0.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshData();
                    }
                },1000);

            }
        });
        //设置模式
        ptrFrameLayout0.setMode(PtrFrameLayout.Mode.REFRESH);

        //调整通知栏高度
        statusBar = (LinearLayout) findViewById(R.id.msg_status_bar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this);
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        statusBar.setLayoutParams(layoutParams);
    }

    void initEvent(){

        msg_back.setOnClickListener(this);

        msgs = new ArrayList<MessageList>();
        adapter = new MessageAdapter(this,msgs);
        adapter.setOnItemDetailListener(new MessageAdapter.OnItemDetailListener() {
            @Override
            public void onItemDetailClick(int i) {
                Intent intent = new Intent(MessageCenterActivity.this,MessageDetailActivity.class);
                intent.putExtra("msgId",msgs.get(i).getId());
                startActivity(intent);
            }
        });
        msg_lv.setAdapter(adapter);

        startLoading();
        refreshData();
    }

    void refreshData(){
        new Thread(){
            @Override
            public void run() {
                String url = Const.MSG_LIST;
                HttpHelper.sendOkHttpPost(url, "{}", new Callback() {
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
                        android.os.Message msg = new android.os.Message();
                        msg.setData(bundle);
                        msg.what = 1;
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
            case R.id.msg_back:
                finish();
                break;
        }
    }
}
