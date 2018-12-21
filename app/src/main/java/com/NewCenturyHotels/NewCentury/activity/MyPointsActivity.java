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

public class MyPointsActivity extends SwipeBackActivity implements View.OnClickListener {

    RelativeLayout back;
    TextView pointDate1;
    TextView pointDate2;
    TextView pointDate3;
    TextView pointExchange;
    TextView pointSum;
    ListView pointLv;
    LinearLayout statusBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_points);
        StatusBarUtils.with(this).init();
        initViews();
    }

    void initViews(){
        back = (RelativeLayout) findViewById(R.id.point_back);
        pointDate1 = (TextView) findViewById(R.id.point_date1);
        pointDate2 = (TextView) findViewById(R.id.point_date2);
        pointDate3 = (TextView) findViewById(R.id.point_date3);
        pointExchange = (TextView) findViewById(R.id.point_exchange);
        pointSum = (TextView) findViewById(R.id.point_sum);
        pointLv = (ListView) findViewById(R.id.point_lv);

        //调整通知栏高度
        statusBar = (LinearLayout) findViewById(R.id.point_status_bar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this);
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        statusBar.setLayoutParams(layoutParams);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.point_back:
                finish();
                break;
            case R.id.point_date1:
                break;
            case R.id.point_date2:
                break;
            case R.id.point_date3:
                break;
            case R.id.point_exchange:
                break;
        }
    }
}
