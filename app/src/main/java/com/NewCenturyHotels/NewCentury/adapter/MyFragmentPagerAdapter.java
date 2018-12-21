package com.NewCenturyHotels.NewCentury.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.NewCenturyHotels.NewCentury.fragment.Fragment1;
import com.NewCenturyHotels.NewCentury.fragment.Fragment2;
import com.NewCenturyHotels.NewCentury.fragment.Fragment3;
import com.NewCenturyHotels.NewCentury.fragment.Fragment4;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

    private String[] mTitles = new String[]{"全部", "待付款", "待出行","待评价"};

    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 1) {
            return new Fragment2();
        } else if (position == 2) {
            return new Fragment3();
        }else if (position==3){
            return new Fragment4();
        }
        return new Fragment1();
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    //ViewPager与TabLayout绑定后，这里获取到PageTitle就是Tab的Text
    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }
}