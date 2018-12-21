package com.NewCenturyHotels.NewCentury.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.util.StatusBarUtils;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class MyBalanceActivity extends SwipeBackActivity implements View.OnClickListener{

    RelativeLayout back;
    TextView balaSum;
    TextView balaCharge;
    TextView balaDate1;
    TextView balaDate2;
    TextView balaDate3;
    ListView balaLv;
    LinearLayout statusBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_balance);
        StatusBarUtils.with(this).init();
        initView();
    }

    void initView(){
        back = (RelativeLayout) findViewById(R.id.bala_back);
        balaSum = (TextView) findViewById(R.id.bala_sum);
        balaCharge = (TextView) findViewById(R.id.bala_charge);
        balaDate1 = (TextView) findViewById(R.id.bala_date1);
        balaDate2 = (TextView) findViewById(R.id.bala_date2);
        balaDate3 = (TextView) findViewById(R.id.bala_date3);
        balaLv = (ListView) findViewById(R.id.bala_lv);
        back.setOnClickListener(this);
        balaCharge.setOnClickListener(this);
        balaDate1.setOnClickListener(this);
        balaDate2.setOnClickListener(this);
        balaDate3.setOnClickListener(this);

        //调整通知栏高度
        statusBar = (LinearLayout) findViewById(R.id.bala_status_bar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this);
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        statusBar.setLayoutParams(layoutParams);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bala_back:
                finish();
                break;
            case R.id.bala_date1:
                break;
            case R.id.bala_date2:
                break;
            case R.id.bala_date3:
                break;
            case R.id.bala_charge:
                break;
        }
    }
}
