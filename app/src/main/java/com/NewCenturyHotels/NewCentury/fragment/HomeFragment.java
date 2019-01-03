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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.App;
import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.activity.Html5Activity;
import com.NewCenturyHotels.NewCentury.adapter.HomeGridAdapter;
import com.NewCenturyHotels.NewCentury.bean.AppVersionRes;
import com.NewCenturyHotels.NewCentury.bean.HomeGrid;
import com.NewCenturyHotels.NewCentury.cons.AppInfo;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;
import com.NewCenturyHotels.NewCentury.transfer.BannerTransformer;
import com.NewCenturyHotels.NewCentury.transfer.GlideImageLoader;
import com.NewCenturyHotels.NewCentury.util.HttpHelper;
import com.NewCenturyHotels.NewCentury.util.SharedPreferencesHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private RelativeLayout tab1_search;
    private GridView home_gridView;
    private Banner banner;
    private List<HomeGrid> iconDatas;
    private HomeGridAdapter gridAdapter;

    SharedPreferencesHelper sharedPreferencesHelper;

    final static String TAG = HomeFragment.class.getName();
    View view;

    Thread thread;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try{
                if(msg.what == 1){//首页ICON
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        JsonArray data = jo.getAsJsonArray("data");
                        iconDatas.clear();
                        for(int i = 0;i < data.size();i++){
                            JsonObject object = data.get(i).getAsJsonObject();
                            Gson gson = new Gson();
                            HomeGrid homeGrid = gson.fromJson(object,HomeGrid.class);
                            iconDatas.add(homeGrid);
                        }
                        gridAdapter.notifyDataSetChanged();
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.tab1, container, false);
        initView();
        initGridViewData();
        initPic();

        return view;
    }

    void initView(){
        tab1_search = (RelativeLayout) view.findViewById(R.id.tab1_search);
        home_gridView = (GridView) view.findViewById(R.id.tab1_gv);
        banner = view.findViewById(R.id.banner);

        tab1_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getContext(), Html5Activity.class);
                intent.putExtra("url",Const.SEARCH_HOTEL);
                startActivity(intent);
            }
        });
    }

    //设置图片集合
    void initPic(){
        sharedPreferencesHelper = new SharedPreferencesHelper(getActivity());
        Boolean ifFirst = (Boolean) sharedPreferencesHelper.get(SharedPref.FIRST_SHOW,true);
        if(ifFirst){
            String urls = sharedPreferencesHelper.get(SharedPref.HOME_IMAGES,"").toString();
            if(!urls.isEmpty()){
                String[] urlArray = urls.split(",");
                List<String> picUrls = new ArrayList<>();
                for(int i = 0;i < urlArray.length;i++){
                    picUrls.add(urlArray[i]);
                }
                initBanner(picUrls);
            }
        }else{
            List<File> files = new ArrayList<>();
            sharedPreferencesHelper = new SharedPreferencesHelper(getContext());
            String paths = sharedPreferencesHelper.get(SharedPref.HOME_IMAGES_LOCAL,"").toString();
            if(!paths.isEmpty()){
                String[] array = paths.split(",");
                for(int i = 0;i < array.length;i++){
                    File file = new File(array[i]);
                    files.add(file);
                }
            }
            if(files.size() > 0 && files.get(files.size() - 1).exists()){
                initBanner(files);
            }else{
                String urls = sharedPreferencesHelper.get(SharedPref.HOME_IMAGES,"").toString();
                if(!urls.isEmpty()){
                    String[] urlArray = urls.split(",");
                    List<String> picUrls = new ArrayList<>();
                    for(int i = 0;i < urlArray.length;i++){
                        picUrls.add(urlArray[i]);
                    }
                    initBanner(picUrls);
                }
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if(hidden){
            banner.setEnabled(false);
            banner.stopAutoPlay();
        }else{
            banner.setEnabled(true);
            banner.startAutoPlay();
        }
    }

    void initBanner(List<?> images){
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        banner.setImages(images);
        banner.setDelayTime(8000);
        banner.isAutoPlay(true);
        banner.setIndicatorGravity(BannerConfig.RIGHT);
        banner.setBannerAnimation(BannerTransformer.class);
        banner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                AppVersionRes.ImgDataInfo imgDataInfo = ((AppVersionRes)(App.mInfo.get(AppInfo.APP_VERSION))).getImgData().getPageImg()[position];
                String url = imgDataInfo.getRedirectUrl();
                Intent intent=new Intent(getContext(), Html5Activity.class);
                intent.putExtra("url",url);
                intent.putExtra("needNotLogin",true);
                startActivity(intent);
            }
        });
        //banner设置方法全部调用完毕时最后调用
        banner.start();
    }

    void initGridViewData() {
        iconDatas = new ArrayList<>();
        gridAdapter = new HomeGridAdapter(getContext(),iconDatas);
        home_gridView.setAdapter(gridAdapter);

        home_gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String url = iconDatas.get(i).getRedirectSrc();
                if(url.contains("http")){
                    url = url.replace("${token}",HttpHelper.getAuthorization());
                }
                Intent intent=new Intent(getContext(), Html5Activity.class);
                intent.putExtra("url",url);
                intent.putExtra("needNotLogin",true);
                startActivity(intent);
            }
        });

        thread = new Thread(){
            @Override
            public void run() {
                String url = Const.HOME_PAGE_ICON;
                HttpHelper.sendOkHttpPost(url, "", new Callback() {
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
}
