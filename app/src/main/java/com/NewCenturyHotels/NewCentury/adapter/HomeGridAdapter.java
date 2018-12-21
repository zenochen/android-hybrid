package com.NewCenturyHotels.NewCentury.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.bean.HomeGrid;
import com.bumptech.glide.Glide;

import java.util.List;

public class HomeGridAdapter extends BaseAdapter {

    List<HomeGrid> datas;
    LayoutInflater layoutInflater;
    Context context;

    public HomeGridAdapter(Context context,List<HomeGrid> datas){
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.datas = datas;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int i) {
        return datas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        view = layoutInflater.inflate(R.layout.tab1_gv_item,viewGroup,false);
        TextView text = view.findViewById(R.id.tab1_gv_text);
        ImageView img = view.findViewById(R.id.tab1_gv_img);

        text.setText(datas.get(i).getName());
        Glide.with(context).load(datas.get(i).getImg()).into(img);

        return view;
    }
}
