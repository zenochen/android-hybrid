package com.NewCenturyHotels.NewCentury.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.NewCenturyHotels.NewCentury.R;

public class AppVersionDialog extends Dialog implements View.OnClickListener{
    private TextView contentTxt;
    private TextView titleTxt;
    private TextView submitTxt;
    private TextView cancelTxt;

    private Context mContext;
    private String content;
    private OnCloseListener listener;
    private String positiveName;
    private String negativeName;
    private String title;

    public TextView getSubmitTxt() {
        return submitTxt;
    }

    public TextView getCancelTxt() {
        return cancelTxt;
    }

    public AppVersionDialog(Context context) {
        super(context);
        this.mContext = context;
    }

    public AppVersionDialog(Context context, int themeResId, String content) {
        super(context, themeResId);
        this.mContext = context;
        this.content = content;
    }

    public AppVersionDialog(Context context, int themeResId, OnCloseListener listener) {
        super(context, themeResId);
        this.mContext = context;
        this.listener = listener;
    }

    public AppVersionDialog(Context context, int themeResId, String content, OnCloseListener listener) {
        super(context, themeResId);
        this.mContext = context;
        this.content = content;
        this.listener = listener;
    }

    protected AppVersionDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.mContext = context;
    }

    public AppVersionDialog setTitle(String title){
        this.title = title;
        return this;
    }

    public AppVersionDialog setContent(String content){
        this.content = content;
        return this;
    }

    public AppVersionDialog setPositiveButton(String name){
        this.positiveName = name;
        return this;
    }

    public AppVersionDialog setNegativeButton(String name){
        this.negativeName = name;

        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_version);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView(){
        contentTxt = (TextView)findViewById(R.id.diav_content);
        titleTxt = (TextView)findViewById(R.id.diav_title);
        submitTxt = (TextView)findViewById(R.id.diav_confirm_tv);
        submitTxt.setOnClickListener(this);
        cancelTxt = (TextView)findViewById(R.id.diav_cancel_tv);
        cancelTxt.setOnClickListener(this);

        contentTxt.setText(content);
        if(!TextUtils.isEmpty(positiveName)){
            submitTxt.setText(positiveName);
        }

        if(!TextUtils.isEmpty(negativeName)){
            cancelTxt.setText(negativeName);
        }

        if(!TextUtils.isEmpty(title)){
            titleTxt.setText(title);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.diav_cancel_tv:
                if(listener != null){
                    listener.onClick(this, false);
                }
                break;
            case R.id.diav_confirm_tv:
                if(listener != null){
                    listener.onClick(this, true);
                }
                break;
        }
    }

    public interface OnCloseListener{
        void onClick(Dialog dialog, boolean confirm);
    }
}
