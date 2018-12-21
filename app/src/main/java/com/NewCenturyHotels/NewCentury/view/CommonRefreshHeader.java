package com.NewCenturyHotels.NewCentury.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.NewCenturyHotels.NewCentury.R;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.indicator.PtrIndicator;

public class CommonRefreshHeader extends FrameLayout implements PtrUIHandler {

    /**
     * 刷新
     */
    private ProgressBar progressBar;

    private TextView tv_notice;

    /**
     * 状态识别
     */
    private int mState;

    /**
     * 重置
     * 准备刷新
     * 开始刷新
     * 结束刷新
     */
    public static final int STATE_RESET = -1;
    public static final int STATE_PREPARE = 0;
    public static final int STATE_BEGIN = 1;
    public static final int STATE_FINISH = 2;

    public CommonRefreshHeader(Context context) {
        this(context, null);
    }

    public CommonRefreshHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonRefreshHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    /**
     * 初始化view
     */
    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.common_refresh_header, this, false);
        progressBar = view.findViewById(R.id.cref_pb);
        tv_notice = view.findViewById(R.id.cref_notice);
        addView(view);
    }

    @Override
    public void onUIReset(PtrFrameLayout frame) {
        mState = STATE_RESET;
    }

    @Override
    public void onUIRefreshPrepare(PtrFrameLayout frame) {
        mState = STATE_PREPARE;
    }

    @Override
    public void onUIRefreshBegin(PtrFrameLayout frame) {
        mState = STATE_BEGIN;
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUIRefreshComplete(PtrFrameLayout frame, boolean isHeader) {
        mState = STATE_FINISH;
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {
        //处理提醒字体
        switch (mState) {
            case STATE_PREPARE:

                if (ptrIndicator.getCurrentPercent() < 1.2) {
                    tv_notice.setText("下拉刷新");
                } else {
                    tv_notice.setText("松开刷新");
                }

                break;

            case STATE_BEGIN:

                tv_notice.setText("");

                break;

            case STATE_FINISH:

                tv_notice.setText("加载完成");

                break;

        }

    }

}