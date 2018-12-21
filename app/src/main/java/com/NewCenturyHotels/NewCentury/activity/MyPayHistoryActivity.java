package com.NewCenturyHotels.NewCentury.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.util.StatusBarUtils;

import java.util.List;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class MyPayHistoryActivity extends SwipeBackActivity implements View.OnClickListener{

    RelativeLayout back;
    TextView phisDate1;
    TextView phisDate2;
    TextView phisDate3;
    ListView phisLv;
    LinearLayout statusBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_pay_history);
        StatusBarUtils.with(this).init();
        initViews();
    }

    void initViews(){
        back = (RelativeLayout) findViewById(R.id.phis_back);
        phisDate1 = (TextView) findViewById(R.id.phis_date1);
        phisDate2 = (TextView) findViewById(R.id.phis_date2);
        phisDate3 = (TextView) findViewById(R.id.phis_date3);
        phisLv = (ListView) findViewById(R.id.phis_lv);

        //调整通知栏高度
        statusBar = (LinearLayout) findViewById(R.id.phis_status_bar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this);
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        statusBar.setLayoutParams(layoutParams);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.phis_back:
                finish();
                break;
            case R.id.phis_date1:
                break;
            case R.id.phis_date2:
                break;
            case R.id.phis_date3:
                break;
        }
    }
}
