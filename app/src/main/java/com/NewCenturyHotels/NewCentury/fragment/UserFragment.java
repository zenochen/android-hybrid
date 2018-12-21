package com.NewCenturyHotels.NewCentury.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.App;
import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.activity.ChangeAccountActivity;
import com.NewCenturyHotels.NewCentury.activity.Html5Activity;
import com.NewCenturyHotels.NewCentury.activity.MessageCenterActivity;
import com.NewCenturyHotels.NewCentury.activity.MyBalanceActivity;
import com.NewCenturyHotels.NewCentury.activity.MyPointsActivity;
import com.NewCenturyHotels.NewCentury.activity.PersonalActivity;
import com.NewCenturyHotels.NewCentury.activity.SettingsActivity;
import com.NewCenturyHotels.NewCentury.activity.SignInByCodeActivity;
import com.NewCenturyHotels.NewCentury.bean.AppVersionRes;
import com.NewCenturyHotels.NewCentury.bean.UserCenter;
import com.NewCenturyHotels.NewCentury.cons.AppInfo;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;
import com.NewCenturyHotels.NewCentury.transfer.GlideCircleTransform;
import com.NewCenturyHotels.NewCentury.transfer.GlideImageLoader;
import com.NewCenturyHotels.NewCentury.util.HttpHelper;
import com.NewCenturyHotels.NewCentury.util.SharedPreferencesHelper;
import com.NewCenturyHotels.NewCentury.util.StatusBarUtils;
import com.NewCenturyHotels.NewCentury.view.BlackRefreshHeader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sunfusheng.marqueeview.MarqueeView;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UserFragment extends Fragment implements View.OnClickListener{

    View view;
    PtrFrameLayout ptrFrameLayout;
    RelativeLayout loading;

    GridView tab4_gv_1;
    List<Map<String, Object>> tab4_gv1_dataList;
    SimpleAdapter tab4_gv1_adapter;

    GridView tab4_gv_2;
    List<Map<String, Object>> tab4_gv2_dataList;
    SimpleAdapter tab4_gv2_adapter;

    GridView tab4_gv_10;
    GridView tab4_gv_20;

    ImageButton tab4_btn_settings;
    ImageButton tab4_btn_msgs;

    TextView tab4_tv_change_account;
    TextView tab4_tv_name;
    RelativeLayout tab4_card;
    TextView tab4_tv_card;
    ImageView tab4_iv_edit;

    RelativeLayout tab4_balance;
    RelativeLayout tab4_credit;
    RelativeLayout tab4_ets;

    TextView tab4_tv_balance;
    TextView tab4_tv_credit;
    TextView tab4_tv_ets;

    private Banner banner_login;
    private Banner banner_unlogin;

    LinearLayout tab4_logined;
    LinearLayout tab4_unlogin;

    TextView tab4_login;
    //通知栏布局
    LinearLayout statusBar;

    SharedPreferencesHelper sharedPreferencesHelper;

    MarqueeView tab4_marque;
    List<UserCenter.NoticeInfo> infos;
    UserCenter userCenter;

    final static String TAG = UserFragment.class.getName();

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try{
                stopLoading();
                if(msg.what == 1){//获取个人中心信息
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        JsonObject data = jo.getAsJsonObject("data");
                        Gson gson = new Gson();
                        userCenter = gson.fromJson(data,UserCenter.class);
                        tab4_tv_name.setText(userCenter.getMemberInfoData().getNameCN());
                        tab4_tv_card.setText(userCenter.getMemberInfoData().getCardLevelName());
                        tab4_tv_balance.setText(userCenter.getMemberInfoData().getBalance());
                        tab4_tv_credit.setText(userCenter.getMemberInfoData().getPoints());
                        tab4_tv_ets.setText(userCenter.getVoucherCount());
                        if(!userCenter.getHeadImages().isEmpty()){
                            Glide.with(UserFragment.this).load(userCenter.getHeadImages())
                                    .apply(RequestOptions.bitmapTransform(new GlideCircleTransform(getContext())))
                                    .into((ImageView) view.findViewById(R.id.tab4_iv_headimg));
                        }

                        //初始化通知列表
                        infos = new ArrayList<>();
                        for(int j = 0;j < userCenter.getNoticeManagement().length;j++){

                            infos.add(userCenter.getNoticeManagement()[j]);
                        }
                        initMarque();
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tab4,container,false);
        initView();
        initPageData();

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            tab4_marque.stopFlipping();
        }else{
            tab4_marque.startFlipping();
        }
    }

    void initView(){
        ptrFrameLayout = (PtrFrameLayout) view.findViewById(R.id.tab4_ptr);

        tab4_unlogin = (LinearLayout) view.findViewById(R.id.tab4_unlogin);
        tab4_logined = (LinearLayout) view.findViewById(R.id.tab4_logined);

        loading = (RelativeLayout) view.findViewById(R.id.tab4_loading);

        //登录页面
        tab4_gv_1 = (GridView) view.findViewById(R.id.tab4_gv_1);
        tab4_gv_2 = (GridView) view.findViewById(R.id.tab4_gv_2);

        tab4_btn_settings = (ImageButton) view.findViewById(R.id.tab4_btn_settings);
        tab4_btn_msgs = (ImageButton) view.findViewById(R.id.tab4_btn_msgs);

        tab4_tv_change_account = (TextView) view.findViewById(R.id.tab4_tv_change_account);
        tab4_tv_name = (TextView) view.findViewById(R.id.tab4_tv_name);
        tab4_card = (RelativeLayout) view.findViewById(R.id.tab4_card);
        tab4_tv_card = (TextView) view.findViewById(R.id.tab4_tv_card);
        tab4_iv_edit = (ImageView) view.findViewById(R.id.tab4_iv_edit);

        tab4_balance = (RelativeLayout) view.findViewById(R.id.tab4_balance);
        tab4_credit = (RelativeLayout) view.findViewById(R.id.tab4_credit);
        tab4_ets = (RelativeLayout) view.findViewById(R.id.tab4_ets);

        tab4_tv_balance = (TextView) view.findViewById(R.id.tab4_tv_balance);
        tab4_tv_credit = (TextView) view.findViewById(R.id.tab4_tv_credit);
        tab4_tv_ets = (TextView) view.findViewById(R.id.tab4_tv_ets);

        tab4_marque = view.findViewById(R.id.tab4_marqueeView);
        banner_login = view.findViewById(R.id.tab4_banner_login);

        //未登录页面
        tab4_gv_10 = (GridView) view.findViewById(R.id.tab4_gv_10);
        tab4_gv_20 = (GridView) view.findViewById(R.id.tab4_gv_20);
        tab4_login = (TextView) view.findViewById(R.id.tab4_tv_login);
        banner_unlogin = view.findViewById(R.id.tab4_banner_unlogin);
        tab4_login.setOnClickListener(this);

        //点击事件
        tab4_btn_settings.setOnClickListener(this);
        tab4_btn_msgs.setOnClickListener(this);

        tab4_tv_change_account.setOnClickListener(this);
        tab4_card.setOnClickListener(this);
        tab4_iv_edit.setOnClickListener(this);

        tab4_balance.setOnClickListener(this);
        tab4_credit.setOnClickListener(this);
        tab4_ets.setOnClickListener(this);

        //定义下拉刷新样式
        BlackRefreshHeader blackRefreshHeader = new BlackRefreshHeader(getContext());
        ptrFrameLayout.setHeaderView(blackRefreshHeader);
        ptrFrameLayout.addPtrUIHandler(blackRefreshHeader);

        //下拉刷新监听
        ptrFrameLayout.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                ptrFrameLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(!(Boolean) sharedPreferencesHelper.get(SharedPref.LOGINED,false)){//未登录
                            Toast.makeText(getContext(),"请先登录",Toast.LENGTH_LONG).show();
                            ptrFrameLayout.refreshComplete();
                        }else{//已登录
                            initUserInfo();
                        }

                    }
                },1000);

            }
        });
        //设置模式
        ptrFrameLayout.setMode(PtrFrameLayout.Mode.REFRESH);

        //调整通知栏高度
        statusBar = (LinearLayout) view.findViewById(R.id.tab4_status_bar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        layoutParams.height = StatusBarUtils.getStatusBarHeight(getActivity());
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        statusBar.setLayoutParams(layoutParams);
    }

    void startLoading(){
        loading.setVisibility(View.VISIBLE);
        loading.setOnClickListener(null);
    }

    void stopLoading(){
        loading.setVisibility(View.GONE);
    }

    void initUserInfo(){
        new Thread(){
            @Override
            public void run() {
                String url = Const.USER_CENTER;
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
                        Message msg = new Message();
                        msg.setData(bundle);
                        msg.what = 1;
                        handler.sendMessage(msg);
                    }
                });
            }
        }.start();

    }

    void initMarque(){
        List<String> info = new ArrayList<>();
        for(int i = 0;i < infos.size();i++){
            String str = infos.get(i).getContent();
            if(str.length() > 18){
                str = str.substring(0,18);
            }
            info.add(str);
        }
        tab4_marque.startWithList(info);

        tab4_marque.setOnItemClickListener(new MarqueeView.OnItemClickListener() {
            @Override
            public void onItemClick(int position, TextView textView) {
                String url = infos.get(position).getUrl();
                Intent intent=new Intent(getContext(), Html5Activity.class);
                intent.putExtra("url",url);
                startActivity(intent);
            }
        });
    }

    void initBanner(Banner banner,List<?> images){
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        banner.setImages(images);
        banner.setDelayTime(8000);
        banner.isAutoPlay(true);
        banner.setIndicatorGravity(BannerConfig.CENTER);
        //banner设置方法全部调用完毕时最后调用
        banner.start();
        banner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                Log.i(TAG, "OnBannerClick: " + position);
                if(App.mInfo.get(AppInfo.APP_VERSION) != null) {
                    AppVersionRes.ImgDataInfo[] infos = ((AppVersionRes)App.mInfo.get(AppInfo.APP_VERSION)).getImgData().getPersonalImg();
                    if(infos.length > 0){
                        String url = infos[position].getRedirectUrl();
                        Intent intent=new Intent(getContext(), Html5Activity.class);
                        intent.putExtra("url",url);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    void initGvItemClick(){
        tab4_gv_1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(),Html5Activity.class);
                switch (i){
                    case 0:
                        intent.putExtra("url",Const.DAILY_SIGNIN);
                        break;
                    case 1:
                        intent.putExtra("url",Const.LUCKY_DRAW);
                        break;
                    case 2:
                        intent.putExtra("url",Const.GIFT_RECEIVE);
                        break;
                    case 3:
                        intent.putExtra("url",Const.GIFT_RECEIVE);
                        break;
                }
                startActivity(intent);
            }
        });
        tab4_gv_2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(),Html5Activity.class);
                switch (i){
                    case 0:
                        intent.putExtra("url",Const.MY_ORDER);
                        break;
                    case 1:
                        intent.putExtra("url",Const.MY_COMMENT);
                        break;
                    case 2:
                        intent.putExtra("url",Const.MY_COLLECTION);
                        break;
                    case 3:
                        intent.putExtra("url",Const.MY_CONSUME);
                        break;
                    case 4:
                        intent.putExtra("url",Const.POINT_STORE);
                        break;
                    case 5:
                        intent.putExtra("url",Const.POINT_STORE);
                        break;
                    case 6:
                        intent.putExtra("url",Const.POINT_STORE);
                        break;
                    case 7:
                        intent.putExtra("url",Const.PASSAGE_INFO);
                        break;
                    case 8:
                        intent.putExtra("url",Const.MY_INVOICE);
                        break;
                    case 9:
                        intent.putExtra("url",Const.ABOUT_US);
                        break;
                }
                startActivity(intent);
            }
        });

        tab4_gv_10.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getContext(),"请先登录",Toast.LENGTH_LONG).show();
            }
        });
        tab4_gv_20.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getContext(),"请先登录",Toast.LENGTH_LONG).show();
            }
        });
    }

    void initPageData(){

        //第一行按钮
        int icno[] = { R.drawable.membercenter_benifit,R.drawable.membercenter_package,
                R.drawable.membercenter_package,R.drawable.membercenter_kampong};
        //图标下的文字
        String name[]={"每日签到","积分抽奖","领取礼包","祺行梦想家"};
        tab4_gv1_dataList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i <icno.length; i++) {
            Map<String, Object> map=new HashMap<String, Object>();
            map.put("img", icno[i]);
            map.put("text",name[i]);
            tab4_gv1_dataList.add(map);
        }

        //初始化数据
        String[] from={"img","text"};
        int[] to={R.id.tab4_gv_iv,R.id.tab4_gv_tv};
        tab4_gv1_adapter=new SimpleAdapter(getContext(), tab4_gv1_dataList, R.layout.tab4_gv_item, from, to);
        tab4_gv_1.setAdapter(tab4_gv1_adapter);

        //我的服务按钮
        int icno2[] = { R.drawable.membercenter_orders,R.drawable.membercenter_comments,
                R.drawable.membercenter_favorite,R.drawable.membercenter_favorite,
                R.drawable.membercenter_mall,R.drawable.membercenter_gift,R.drawable.membercenter_cards,
                R.drawable.membercenter_gusets,R.drawable.membercenter_invoice,R.drawable.membercenter_invoice};
        //图标下的文字
        String name2[]={"我的订单","我的评论","我的收藏","我的消费","积分商城","礼品卡","联名卡","常客信息","我的发票","关于我们"};
        tab4_gv2_dataList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i <icno2.length; i++) {
            Map<String, Object> map=new HashMap<String, Object>();
            map.put("img", icno2[i]);
            map.put("text",name2[i]);
            tab4_gv2_dataList.add(map);
        }

        //初始化数据
        String[] from2={"img","text"};
        int[] to2={R.id.tab4_gv_iv,R.id.tab4_gv_tv};
        tab4_gv2_adapter=new SimpleAdapter(getContext(), tab4_gv2_dataList, R.layout.tab4_gv_item, from2, to2);
        tab4_gv_2.setAdapter(tab4_gv2_adapter);
        initGvItemClick();

        //未登录图标
        tab4_gv_10.setAdapter(tab4_gv1_adapter);
        tab4_gv_20.setAdapter(tab4_gv2_adapter);

        //登录与未登录状态页面
        sharedPreferencesHelper = new SharedPreferencesHelper(getContext());
        if(!(Boolean) sharedPreferencesHelper.get(SharedPref.LOGINED,false)){//未登录
            tab4_unlogin.setVisibility(View.VISIBLE);
        }else{//已登录
            tab4_logined.setVisibility(View.VISIBLE);
            startLoading();
            initUserInfo();
        }

        //中间展示图片
        List<File> files = new ArrayList<>();
        String paths = sharedPreferencesHelper.get(SharedPref.USER_IMAGE_LOCAL,"").toString();
        if(!paths.isEmpty()){
            String[] array = paths.split(",");
            for(int i = 0;i < array.length;i++){
                File file = new File(array[i]);
                files.add(file);
            }
        }

        if(files.size() > 0 && files.get(files.size() - 1).exists()){
            if(!(Boolean) sharedPreferencesHelper.get(SharedPref.LOGINED,false)){//未登录
                initBanner(banner_unlogin,files);
            }else{//已登录
                initBanner(banner_login,files);
            }
        }else{
            String urls = sharedPreferencesHelper.get(SharedPref.USER_IMAGE,"").toString();
            if(!urls.isEmpty()){
                String[] urlArray = urls.split(",");
                List<String> urlList = new ArrayList<>();
                for(int i = 0;i < urlArray.length;i++){
                    urlList.add(urlArray[i]);
                }
                if(!(Boolean) sharedPreferencesHelper.get(SharedPref.LOGINED,false)){//未登录
                    initBanner(banner_unlogin,urlList);
                }else{//已登录
                    initBanner(banner_login,urlList);
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.tab4_btn_settings:
                intent=new Intent(getContext(), SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.tab4_btn_msgs:
                intent=new Intent(getContext(), MessageCenterActivity.class);
                startActivity(intent);
                break;
            case R.id.tab4_tv_change_account:
                intent=new Intent(getContext(), ChangeAccountActivity.class);
                startActivity(intent);
                break;
            case R.id.tab4_card:
                intent=new Intent(getContext(), Html5Activity.class);
                intent.putExtra("url",Const.MEMBER);
                startActivity(intent);
                break;
            case R.id.tab4_iv_edit:
                intent=new Intent(getContext(), PersonalActivity.class);
                startActivity(intent);
                break;
            case R.id.tab4_balance:
                intent=new Intent(getContext(), Html5Activity.class);
                intent.putExtra("url",Const.MY_MONEY);
                startActivity(intent);
                break;
            case R.id.tab4_credit:
                intent=new Intent(getContext(), Html5Activity.class);
                intent.putExtra("url",Const.MY_POINTS);
                startActivity(intent);
                break;
            case R.id.tab4_ets:
                intent=new Intent(getContext(), Html5Activity.class);
                intent.putExtra("url",Const.MY_COUPONS);
                startActivity(intent);
                break;
            case R.id.tab4_tv_login:
                App.mInfo.put(AppInfo.TAB_INDEX,3);
                intent=new Intent(getContext(), SignInByCodeActivity.class);
                startActivity(intent);
                break;
        }
    }
}
