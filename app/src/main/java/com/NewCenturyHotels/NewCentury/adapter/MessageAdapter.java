package com.NewCenturyHotels.NewCentury.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.bean.MessageList;
import com.NewCenturyHotels.NewCentury.util.TimeUtil;

import java.util.List;

public class MessageAdapter extends BaseAdapter {

    LayoutInflater layoutInflater;
    List<MessageList> msgs;

    public MessageAdapter(){
        super();
    }

    public MessageAdapter(Context context,List<MessageList> msgs){
        this.layoutInflater = LayoutInflater.from(context);
        this.msgs = msgs;
    }

    @Override
    public int getCount() {
        return msgs.size();
    }

    @Override
    public Object getItem(int i) {
        return msgs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {

        view = layoutInflater.inflate(R.layout.message_lv_item,viewGroup,false);

        TextView title = view.findViewById(R.id.msg_lv_tv_title);
        TextView content = view.findViewById(R.id.msg_lv_tv_content);
        TextView date = view.findViewById(R.id.msg_lv_tv_days);
        TextView toDetail = view.findViewById(R.id.msg_lv_to_detail);
        TextView isRead = view.findViewById(R.id.msg_lv_is_read);
        RelativeLayout showTime = view.findViewById(R.id.msg_lv_rl_days);

        MessageList msg = msgs.get(i);
        title.setText(msg.getTitle());
        content.setText(msg.getText());
        if(msg.getIsRead() == "1"){
            isRead.setText("已读");
            isRead.setTextColor(Color.parseColor("#B3B3B3"));
        }else{
            isRead.setText("未读");
        }

        toDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemDetailListener.onItemDetailClick(i);
            }
        });

        String timeStr = msg.getCreateDateTime();
        TimeUtil util = new TimeUtil();
        long daySpan = util.getDaySpan(util.getTimeSpan(timeStr));
        String timeShow = "";
        if(daySpan > 0){
            timeShow = util.getDateStr(util.parseDate(timeStr),TimeUtil.MMDD);
        }else{
            timeShow = "今天"+util.getDateStr(util.parseDate(timeStr),TimeUtil.HHMM);
        }

        date.setText(timeShow);

        return view;
    }

    public interface OnItemDetailListener{
        void onItemDetailClick(int i);
    }

    private OnItemDetailListener onItemDetailListener;

    public void setOnItemDetailListener(OnItemDetailListener onItemDetailListener){
        this.onItemDetailListener = onItemDetailListener;
    }

}
