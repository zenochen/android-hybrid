package com.NewCenturyHotels.NewCentury.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.adapter.OrderAdapter;
import com.NewCenturyHotels.NewCentury.bean.TradeList;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.req.TradeReq;
import com.NewCenturyHotels.NewCentury.util.HttpHelper;
import com.NewCenturyHotels.NewCentury.view.CommonRefreshFooter;
import com.NewCenturyHotels.NewCentury.view.CommonRefreshHeader;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrDefaultHandler2;
import in.srain.cube.views.ptr.PtrFrameLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Fragment4 extends Fragment {

    ListView order_lv;
    RelativeLayout layer;
    RelativeLayout loading;

    OrderAdapter orderAdapter;
    List<TradeList> orders;

    private PtrFrameLayout ptrFrameLayout;

    View view;

    Thread thread;
    Thread tPullMore;

    int curPage = 1;
    int totalCount;

    final static String TAG = Fragment4.class.getName();

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try{
                if(msg.what == 1){//下拉刷新数据
                    stopLoading();

                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        JsonObject data = jo.getAsJsonObject("data");
                        JsonArray dataList = data.getAsJsonArray("list");
                        totalCount = data.get("totalCount").getAsInt();
                        orders.clear();
                        for(int i = 0;i < dataList.size();i++){
                            JsonObject jsonObject = (JsonObject) dataList.get(i);
                            Gson gson = new Gson();
                            TradeList trade = gson.fromJson(jsonObject,TradeList.class);
                            orders.add(trade);
                        }

                        if(orders.size() == 0){
                            layer.setVisibility(View.VISIBLE);
                        }else{
                            layer.setVisibility(View.GONE);
                        }

                        orderAdapter.notifyDataSetChanged();
                    }else{
                        layer.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(),message,Toast.LENGTH_LONG).show();
                    }

                    if(ptrFrameLayout.isRefreshing()){
                        ptrFrameLayout.refreshComplete();
                    }
                }else if(msg.what == 2){
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        JsonObject data = jo.getAsJsonObject("data");
                        JsonArray dataList = data.getAsJsonArray("list");
                        totalCount = data.get("totalCount").getAsInt();
                        for(int i = 0;i < dataList.size();i++){
                            JsonObject jsonObject = (JsonObject) dataList.get(i);
                            Gson gson = new Gson();
                            TradeList trade = gson.fromJson(jsonObject,TradeList.class);
                            orders.add(trade);
                        }

                        orderAdapter.notifyDataSetChanged();
                    }else{
                        Toast.makeText(getContext(),message,Toast.LENGTH_LONG).show();
                    }

                    if(ptrFrameLayout.isRefreshing()){
                        ptrFrameLayout.refreshComplete();
                    }
                }
            }catch (Exception e){
                Log.e(TAG,e.getMessage());
                Toast.makeText(getContext(),"连接超时",Toast.LENGTH_LONG).show();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment4, container, false);

        initView();
        initOrderData();

        return view;
    }

    private void initView() {

        order_lv = view.findViewById(R.id.order_lv_4);
        ptrFrameLayout = (PtrFrameLayout) view.findViewById(R.id.ptr_frame_layout4);
        layer = (RelativeLayout) view.findViewById(R.id.frg4_layer);
        loading = (RelativeLayout) view.findViewById(R.id.frg4_loading);

        //下拉刷新
        CommonRefreshHeader commonRefreshHeader = new CommonRefreshHeader(getContext());
        ptrFrameLayout.setHeaderView(commonRefreshHeader);
        ptrFrameLayout.addPtrUIHandler(commonRefreshHeader);
        //上拉加载
        CommonRefreshFooter commonRefreshFooter = new CommonRefreshFooter(getContext());
        ptrFrameLayout.setFooterView(commonRefreshFooter);
        ptrFrameLayout.addPtrUIHandler(commonRefreshFooter);
        //监听
        ptrFrameLayout.setPtrHandler(new PtrDefaultHandler2() {
            //加载更多监听
            @Override
            public void onLoadMoreBegin(PtrFrameLayout frame) {

                ptrFrameLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(orders.size() < totalCount){
                            curPage++;
                            pullMoreData();
                        }else{
                            Toast.makeText(getContext(),"已经加载所有数据",Toast.LENGTH_LONG).show();
                            ptrFrameLayout.refreshComplete();
                            return;
                        }
                    }
                },1000);

            }
            //下拉刷新监听
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                ptrFrameLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initData();
                    }
                },1000);

            }
        });
        //设置模式
        //BOTH：下拉刷新，下拉加载
        ptrFrameLayout.setMode(PtrFrameLayout.Mode.BOTH);
    }

    void startLoading(){
        loading.setVisibility(View.VISIBLE);
        loading.setOnClickListener(null);
    }

    void stopLoading(){
        loading.setVisibility(View.GONE);
    }

    void initOrderData(){

        orders = new ArrayList<>();
        orderAdapter = new OrderAdapter(getContext(),orders);
        order_lv.setAdapter(orderAdapter);
        //ListView item的点击事件
        order_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getContext(), "Click item" + i, Toast.LENGTH_SHORT).show();
            }
        });

        //ListView item 中的删除按钮的点击事件
        orderAdapter.setOnItemHandleClickListener(new OrderAdapter.onItemHandleListener() {
            @Override
            public void onPayClick(int i) {
                Toast.makeText(getContext(), "onPayClick item:" + i, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelClick(int i) {
                Toast.makeText(getContext(), "onCancelClick item:" + i, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCommentClick(int i) {
                Toast.makeText(getContext(), "onCommentClick item:" + i, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCommentDetailClick(int i) {
                Toast.makeText(getContext(), "onCommentDetailClick item:" + i, Toast.LENGTH_SHORT).show();
            }
        });
        startLoading();
        initData();
    }

    void initData(){
        thread = new Thread(){
            @Override
            public void run() {
                String url = Const.TRADE_LIST;
                Gson gson = new Gson();
                TradeReq req = new TradeReq();
                req.setPageNo(1);
                req.setPageSize(10);
                req.setTradeType(3);
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
                        msg.what = 1;
                        handler.sendMessage(msg);
                    }
                });
            }
        };
        thread.start();
    }

    void pullMoreData(){
        tPullMore = new Thread(){
            @Override
            public void run() {
                String url = Const.TRADE_LIST;
                Gson gson = new Gson();
                TradeReq req = new TradeReq();
                req.setPageNo(curPage);
                req.setPageSize(10);
                req.setTradeType(3);
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
        };
        tPullMore.start();
    }
}
