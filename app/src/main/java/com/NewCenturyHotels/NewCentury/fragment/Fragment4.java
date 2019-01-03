package com.NewCenturyHotels.NewCentury.fragment;

import android.app.Dialog;
import android.content.Intent;
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
import com.NewCenturyHotels.NewCentury.activity.Html5Activity;
import com.NewCenturyHotels.NewCentury.adapter.OrderAdapter;
import com.NewCenturyHotels.NewCentury.bean.TradeList;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;
import com.NewCenturyHotels.NewCentury.req.TradeHandleReq;
import com.NewCenturyHotels.NewCentury.req.TradeReq;
import com.NewCenturyHotels.NewCentury.util.HttpHelper;
import com.NewCenturyHotels.NewCentury.util.SharedPreferencesHelper;
import com.NewCenturyHotels.NewCentury.view.CommomDialog;
import com.NewCenturyHotels.NewCentury.view.CommonRefreshFooter;
import com.NewCenturyHotels.NewCentury.view.CommonRefreshHeader;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrDefaultHandler2;
import in.srain.cube.views.ptr.PtrFrameLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Fragment4 extends Fragment {

    ListView order_lv;
    RelativeLayout loading;

    OrderAdapter orderAdapter;
    List<TradeList> orders;

    private PtrFrameLayout ptrFrameLayout;
    private PtrFrameLayout ptrFrameLayout0;

    View view;

    Thread thread;
    Thread tPullMore;

    int curPage = 1;
    int totalCount;

    final static String TAG = Fragment4.class.getName();
    final int GET_BACK = 1;
    String tradeNo = "";

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try{
                if(msg.what == 1 || msg.what == 2){
                    stopLoading();
                }
                if(msg.what == 1){//下拉刷新数据
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
                            ptrFrameLayout.setVisibility(View.GONE);
                            ptrFrameLayout0.setVisibility(View.VISIBLE);
                        }else{
                            ptrFrameLayout.setVisibility(View.VISIBLE);
                            ptrFrameLayout0.setVisibility(View.GONE);
                        }

                        orderAdapter.notifyDataSetChanged();
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(getActivity());
                    }else{
                        ptrFrameLayout0.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(),message,Toast.LENGTH_LONG).show();
                    }

                    if(ptrFrameLayout.isRefreshing()){
                        ptrFrameLayout.refreshComplete();
                    }
                    if(ptrFrameLayout0.isRefreshing()){
                        ptrFrameLayout0.refreshComplete();
                    }
                }else if(msg.what == 2){//上拉加载
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
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(getActivity());
                    }else{
                        Toast.makeText(getContext(),message,Toast.LENGTH_LONG).show();
                    }

                    if(ptrFrameLayout.isRefreshing()){
                        ptrFrameLayout.refreshComplete();
                    }
                }else if(msg.what == 3){//订单取消
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        initData();
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(getActivity());
                    }else{
                        stopLoading();
                        Toast.makeText(getContext(),message,Toast.LENGTH_LONG).show();
                    }
                }else if(msg.what == 4){//订单删除
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        initData();
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(getActivity());
                    }else{
                        stopLoading();
                        Toast.makeText(getContext(),message,Toast.LENGTH_LONG).show();
                    }
                }else if(msg.what == 5){//支付方式
                    stopLoading();
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        JsonObject data = jo.getAsJsonObject("data");
                        JsonArray oper = data.getAsJsonArray("orderOperates");
                        String payWays = "";
                        for(int i = 0;i < oper.size();i++){
                            JsonObject jsonObject = oper.get(i).getAsJsonObject();
                            if(jsonObject.get("api").getAsString().contains("pay")){
                                JsonArray array = jsonObject.getAsJsonArray("tradePayWayList");
                                for(int j = 0;j < array.size();j++){
                                    payWays = payWays + array.get(j).getAsString() + ",";
                                }

                                payWays = payWays.substring(0,payWays.length() - 1);
                                break;
                            }
                        }
                        Intent intent=new Intent(getContext(), Html5Activity.class);
                        String url = Const.PAY + tradeNo;
                        intent.putExtra("url",url);
                        intent.putExtra("tradeNo",tradeNo);
                        intent.putExtra("payWays",payWays);
                        startActivityForResult(intent,GET_BACK);
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(getActivity());
                    }else{
                        Toast.makeText(getContext(),message,Toast.LENGTH_LONG).show();
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
        ptrFrameLayout0 = view.findViewById(R.id.ptr_frame_layout40);
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

        //下拉刷新
        CommonRefreshHeader commonRefreshHeader0 = new CommonRefreshHeader(getContext());
        ptrFrameLayout0.setHeaderView(commonRefreshHeader0);
        ptrFrameLayout0.addPtrUIHandler(commonRefreshHeader0);
        //监听
        ptrFrameLayout0.setPtrHandler(new PtrDefaultHandler() {

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
        ptrFrameLayout0.setMode(PtrFrameLayout.Mode.REFRESH);
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
                Intent intent=new Intent(getContext(), Html5Activity.class);
                String url = Const.ORDER_DETAIL + orders.get(i).getTradeNo();
                intent.putExtra("url",url);
                startActivity(intent);
            }
        });

        //ListView item 中的删除按钮的点击事件
        orderAdapter.setOnItemHandleClickListener(new OrderAdapter.onItemHandleListener() {
            @Override
            public void onPayClick(int i) {
                tradeNo = orders.get(i).getTradeNo();
                String url = Const.TRADE_DETAIL;
                TradeHandleReq req = new TradeHandleReq();
                req.setTradeNo(orders.get(i).getTradeNo());
                Gson gson = new Gson();
                String json = gson.toJson(req);

                startLoading();

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
                        android.os.Message msg = new android.os.Message();
                        msg.setData(bundle);
                        msg.what = 5;
                        handler.sendMessage(msg);
                    }
                });
            }

            @Override
            public void onCancelClick(final int i) {
                CommomDialog dialog = new CommomDialog(getContext(), R.style.dialog, "确定取消？", new CommomDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if(confirm){
                            dialog.dismiss();
                            String url = Const.TRADE_CANCEL;
                            TradeHandleReq req = new TradeHandleReq();
                            req.setTradeNo(orders.get(i).getTradeNo());
                            Gson gson = new Gson();
                            String json = gson.toJson(req);

                            startLoading();

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
                                    android.os.Message msg = new android.os.Message();
                                    msg.setData(bundle);
                                    msg.what = 3;
                                    handler.sendMessage(msg);
                                }
                            });
                        }
                    }
                });
                dialog.show();
            }

            @Override
            public void onCommentClick(int i) {
                Intent intent=new Intent(getContext(), Html5Activity.class);
                String url = Const.PUBLISH_COMMENT + orders.get(i).getTradeNo();
                intent.putExtra("url",url);
                startActivityForResult(intent,GET_BACK);
            }

            @Override
            public void onCommentDetailClick(int i) {
                Intent intent=new Intent(getContext(), Html5Activity.class);
                String url = Const.SHOW_COMMENT;
                intent.putExtra("url",url);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(final int i) {
                CommomDialog dialog = new CommomDialog(getContext(), R.style.dialog, "确定删除？", new CommomDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if(confirm){
                            dialog.dismiss();
                            String url = Const.TRADE_DEL;
                            TradeHandleReq req = new TradeHandleReq();
                            req.setTradeNo(orders.get(i).getTradeNo());
                            Gson gson = new Gson();
                            String json = gson.toJson(req);

                            startLoading();

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
                                    android.os.Message msg = new android.os.Message();
                                    msg.setData(bundle);
                                    msg.what = 4;
                                    handler.sendMessage(msg);
                                }
                            });
                        }
                    }
                });
                dialog.show();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GET_BACK){
            startLoading();
            initData();
        }
    }
}
