package com.NewCenturyHotels.NewCentury.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.NewCenturyHotels.NewCentury.R;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.indicator.PtrIndicator;

public class JdRefreshHeader extends FrameLayout implements PtrUIHandler {

    /**
     * 提醒文本
     */
    private TextView mTvRemind;

    /**
     * 快递员logo
     */
    private ImageView mIvMan;

    /**
     * 商品logo
     */
    private ImageView mIvGoods;

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

    public static final int MARGIN_RIGHT = 100;

    /**
     * 动画
     */
    private AnimationDrawable mAnimationDrawable;

    public JdRefreshHeader(Context context) {
        this(context, null);
    }

    public JdRefreshHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JdRefreshHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    /**
     * 初始化view
     */
    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.jd_refresh_header, this, false);
        mTvRemind = (TextView) view.findViewById(R.id.tv_remain);
        mIvMan = (ImageView) view.findViewById(R.id.iv_man);
        mIvGoods = (ImageView) view.findViewById(R.id.iv_goods);
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
        //隐藏商品logo,开启跑步动画
        mIvGoods.setVisibility(View.GONE);
        mIvMan.setBackgroundResource(R.drawable.jd_running);
        mAnimationDrawable = (AnimationDrawable) mIvMan.getBackground();
        if (!mAnimationDrawable.isRunning()) {
            mAnimationDrawable.start();
        }
    }

    @Override
    public void onUIRefreshComplete(PtrFrameLayout frame, boolean isHeader) {
        mState = STATE_FINISH;
        mIvGoods.setVisibility(View.VISIBLE);
        //停止动画
        if (mAnimationDrawable.isRunning()) {
            mAnimationDrawable.stop();
        }
        mIvMan.setBackgroundResource(R.drawable.jd_people_0);
    }

    @Override
    public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {
        //处理提醒字体
        switch (mState) {
            case STATE_PREPARE:
                //logo设置
                mIvMan.setAlpha(ptrIndicator.getCurrentPercent());
                mIvGoods.setAlpha(ptrIndicator.getCurrentPercent());
                LayoutParams params = (LayoutParams) mIvMan.getLayoutParams();
                if (ptrIndicator.getCurrentPercent() <= 1) {
                    mIvMan.setScaleX(ptrIndicator.getCurrentPercent());
                    mIvMan.setScaleY(ptrIndicator.getCurrentPercent());
                    mIvGoods.setScaleX(ptrIndicator.getCurrentPercent());
                    mIvGoods.setScaleY(ptrIndicator.getCurrentPercent());
                    int marginRight = (int) (MARGIN_RIGHT - MARGIN_RIGHT * ptrIndicator.getCurrentPercent());
                    params.setMargins(0, 0, marginRight, 0);
                    mIvMan.setLayoutParams(params);
                }

                if (ptrIndicator.getCurrentPercent() < 1.2) {
                    mTvRemind.setText("下拉刷新...");
                } else {
                    mTvRemind.setText("松开刷新...");
                }

                break;

            case STATE_BEGIN:

                mTvRemind.setText("更新中...");

                break;

            case STATE_FINISH:

                mTvRemind.setText("加载完成...");

                break;

        }

    }

}