package com.NewCenturyHotels.NewCentury.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.NewCenturyHotels.NewCentury.App;
import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.activity.Html5Activity;
import com.NewCenturyHotels.NewCentury.activity.SignInByCodeActivity;
import com.NewCenturyHotels.NewCentury.adapter.MyFragmentPagerAdapter;
import com.NewCenturyHotels.NewCentury.cons.AppInfo;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;
import com.NewCenturyHotels.NewCentury.util.SharedPreferencesHelper;
import com.NewCenturyHotels.NewCentury.util.StatusBarUtils;

public class OrderFragment extends Fragment implements View.OnClickListener{

    View view;
    RelativeLayout tab3_layer2;
    LinearLayout tab3_layer3;

    TextView tab3_login;
    TextView tab3_order;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private MyFragmentPagerAdapter myFragmentPagerAdapter;

    private TabLayout.Tab one;
    private TabLayout.Tab two;
    private TabLayout.Tab three;
    private TabLayout.Tab four;

    private LinearLayout statusBar;

    SharedPreferencesHelper sharedPreferencesHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tab3,container,false);

        initView();
        initTab3Layer();
        return view;
    }

    void initView(){
        tab3_layer2 = (RelativeLayout) view.findViewById(R.id.tab3_layer2);
        tab3_layer3 = (LinearLayout) view.findViewById(R.id.tab3_layer3);

        tab3_login = (TextView) view.findViewById(R.id.tab3_login);
        tab3_order = (TextView) view.findViewById(R.id.tab3_orders);

        //tab3-select
        //使用适配器将ViewPager与Fragment绑定在一起
        mViewPager= (ViewPager) view.findViewById(R.id.tab3_viewPager);
        myFragmentPagerAdapter = new MyFragmentPagerAdapter(getActivity().getSupportFragmentManager());
        mViewPager.setAdapter(myFragmentPagerAdapter);

        //将TabLayout与ViewPager绑定在一起
        mTabLayout = (TabLayout) view.findViewById(R.id.tab3_tabLayout);
        mTabLayout.setupWithViewPager(mViewPager);

        //指定Tab的位置
        one = mTabLayout.getTabAt(0);
        two = mTabLayout.getTabAt(1);
        three = mTabLayout.getTabAt(2);
        four = mTabLayout.getTabAt(3);

        tab3_order.setOnClickListener(this);
        tab3_login.setOnClickListener(this);

        //调整通知栏高度
        statusBar = (LinearLayout) view.findViewById(R.id.tab3_status_bar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        layoutParams.height = StatusBarUtils.getStatusBarHeight(getActivity());
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        statusBar.setLayoutParams(layoutParams);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.tab3_orders:
                intent=new Intent(getContext(), Html5Activity.class);
                startActivity(intent);
                break;
            case R.id.tab3_login:
                App.mInfo.put(AppInfo.TAB_INDEX,2);
                intent=new Intent(getContext(), SignInByCodeActivity.class);
                startActivity(intent);
                break;

        }
    }

    private void initTab3Layer(){
        sharedPreferencesHelper = new SharedPreferencesHelper(getContext());
        if(!(Boolean) sharedPreferencesHelper.get(SharedPref.LOGINED,false)){//未登录
            tab3_layer2.setVisibility(View.VISIBLE);
        }else{//已登录
            tab3_layer3.setVisibility(View.VISIBLE);
        }
    }
}
