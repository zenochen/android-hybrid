package com.NewCenturyHotels.NewCentury.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.bean.TradeList;
import com.NewCenturyHotels.NewCentury.util.TimeUtil;

import java.util.List;

public class OrderAdapter extends BaseAdapter{

    LayoutInflater inflater;
    private List<TradeList> orders;
    private Context context;

    public OrderAdapter(){
        super();
    }

    public OrderAdapter(Context context,List<TradeList> datas){
        this.context = context;
        this.orders = datas;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return this.orders.size();
    }

    @Override
    public Object getItem(int i) {
        return orders.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        view = inflater.inflate(R.layout.tab3_lv_item, viewGroup, false); //加载布局
        holder = new ViewHolder();
        holder.hotelName_tv = (TextView) view.findViewById(R.id.order_hotel_name);
        holder.roomType_tv = (TextView) view.findViewById(R.id.order_room_type);
        holder.roomNum_tv = (TextView) view.findViewById(R.id.order_room_num);
        holder.status_tv = (TextView) view.findViewById(R.id.order_status);

        holder.startDate_tv = (TextView) view.findViewById(R.id.order_start_date);
        holder.endDate_tv = (TextView) view.findViewById(R.id.order_end_date);
        holder.days_tv = (TextView) view.findViewById(R.id.order_days);
        holder.price_tv = (TextView) view.findViewById(R.id.order_price);

        holder.cancel_btn = (Button) view.findViewById(R.id.order_cancel);
        holder.del_btn = (Button) view.findViewById(R.id.order_del);
        holder.pay_btn = (Button) view.findViewById(R.id.order_pay);
        holder.comment_btn = (Button) view.findViewById(R.id.order_comment);
        holder.commentDetail_btn = (Button) view.findViewById(R.id.order_comment_detail);

        view.setTag(holder);

        TradeList bean = orders.get(i);
        holder.hotelName_tv.setText(bean.getChineseName());
        holder.roomType_tv.setText(bean.getRoomTypeName());
        holder.roomNum_tv.setText("("+String.valueOf(bean.getRoomNum())+"间)");

        holder.startDate_tv.setText(bean.getArrDate()+"/");
        holder.endDate_tv.setText(bean.getDepDate());
        TimeUtil util = new TimeUtil();
        long days = util.getDaySpan(util.getTimeSpan(bean.getDepDate(),bean.getArrDate(),TimeUtil.YYYYMMDD));
        holder.days_tv.setText("("+String.valueOf(days)+"晚)");
        holder.status_tv.setText(bean.getTradeStateName());

        if("CHG2".equals(bean.getRateCode())){
            holder.price_tv.setText(String.valueOf(bean.getPoints()+"积分"));
        }else{
            holder.price_tv.setText("¥ "+String.valueOf(bean.getPriceTotal()));
        }

        if(bean.getTradeStateEnum().equals("RESERVE") && bean.getTradePayStateEnum().equals("NOTPAY")
                && bean.getTradePayWayEnum().equals("WAITPAY")){
            holder.status_tv.setText("待支付");
        }

        TradeList.OrderOperates[] orderOperates = bean.getOrderOperates();
        for (TradeList.OrderOperates o : orderOperates) {
            if (o.getName().equals("tradeDetail")) {
                holder.pay_btn.setVisibility(View.VISIBLE);
            }
            if (o.getName().equals("addComment")) {
                holder.comment_btn.setVisibility(View.VISIBLE);
            }
            if (o.getName().equals("cancel")) {
                holder.cancel_btn.setVisibility(View.VISIBLE);
            }
            if(o.getName().equals("commentDetail")){
                holder.commentDetail_btn.setVisibility(View.VISIBLE);
            }
            if(o.getName().equals("del")){
                holder.del_btn.setVisibility(View.VISIBLE);
            }
        }

        holder.pay_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemDeleteListener.onPayClick(i);
            }
        });

        holder.cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemDeleteListener.onCancelClick(i);
            }
        });

        holder.comment_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemDeleteListener.onCommentClick(i);
            }
        });
        holder.commentDetail_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemDeleteListener.onCommentDetailClick(i);
            }
        });
        holder.del_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemDeleteListener.onDeleteClick(i);
            }
        });

        return view;
    }

    /**
     * 删除按钮的监听接口
     */
    public interface onItemHandleListener {
        void onPayClick(int i);
        void onCancelClick(int i);
        void onCommentClick(int i);
        void onCommentDetailClick(int i);
        void onDeleteClick(int i);
    }

    private onItemHandleListener mOnItemDeleteListener;

    public void setOnItemHandleClickListener(onItemHandleListener mOnItemDeleteListener) {
        this.mOnItemDeleteListener = mOnItemDeleteListener;
    }

    public class ViewHolder{
        TextView hotelName_tv;
        TextView roomType_tv;
        TextView roomNum_tv;
        TextView status_tv;
        TextView startDate_tv;
        TextView endDate_tv;
        TextView days_tv;
        TextView price_tv;
        Button pay_btn;
        Button cancel_btn;
        Button del_btn;
        Button comment_btn;
        Button commentDetail_btn;
    }
}
