package com.NewCenturyHotels.NewCentury.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.bean.MemberCard;

import java.util.List;

public class MemberCardAdapter extends BaseAdapter {

    LayoutInflater layoutInflater;
    List<MemberCard> cards;

    public MemberCardAdapter(){
        super();
    }

    public MemberCardAdapter(Context context,List<MemberCard> cards){
        layoutInflater = LayoutInflater.from(context);
        this.cards = cards;
    }

    @Override
    public int getCount() {
        return cards.size();
    }

    @Override
    public Object getItem(int i) {
        return cards.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        view = layoutInflater.inflate(R.layout.member_card_lv_item,viewGroup,false);

        TextView cardType = view.findViewById(R.id.accitem_tv_card);
        TextView cardNo = view.findViewById(R.id.accitem_tv_cardno);
        TextView expiredDate = view.findViewById(R.id.accitem_tv_date);
        ImageView isSelected = view.findViewById(R.id.accitem_iv);

        MemberCard card = cards.get(i);
        cardType.setText(card.getCardLevelName());
        cardNo.setText(card.getCardNo());
        expiredDate.setText(card.getExpiryDate());
        if(card.getSelected()){
            isSelected.setVisibility(View.VISIBLE);
        }else{
            isSelected.setVisibility(View.GONE);
        }

        return view;
    }
}
