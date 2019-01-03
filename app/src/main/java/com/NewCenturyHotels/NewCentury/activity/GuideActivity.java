package com.NewCenturyHotels.NewCentury.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;
import com.NewCenturyHotels.NewCentury.util.SharedPreferencesHelper;
import com.NewCenturyHotels.NewCentury.util.StatusBarUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 引导页
 */
public class GuideActivity extends AppCompatActivity implements View.OnClickListener{

    ViewPager viewPager;
    List<View> mTabs = new ArrayList<View>();

    TextView tv_center;
    LinearLayout statusBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        StatusBarUtils.with(this).init();
        initView();
        initEvent();
    }

    void initView(){
        viewPager = findViewById(R.id.guide_vp);

        //获取到四个Tab
        LayoutInflater inflater = LayoutInflater.from(this);
        View tab1 = inflater.inflate(R.layout.guide_tab1, null);
        View tab3 = inflater.inflate(R.layout.guide_tab3, null);

        //将四个Tab添加到集合中
        mTabs.add(tab1);
        mTabs.add(tab3);

        tv_center = (TextView) tab3.findViewById(R.id.guide3_enter);
        //调整通知栏高度
        statusBar = (LinearLayout) findViewById(R.id.guide_status_bar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this);
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        statusBar.setLayoutParams(layoutParams);
    }

    void initEvent(){
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return mTabs.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View view = mTabs.get(position);
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mTabs.get(position));
            }
        });

        //添加ViewPager的切换Tab的监听事件
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tv_center.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.guide3_enter){
            SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(GuideActivity.this);
            sharedPreferencesHelper.put(SharedPref.IS_FIRST,false);
            Intent intent = new Intent(GuideActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {

    }
}
