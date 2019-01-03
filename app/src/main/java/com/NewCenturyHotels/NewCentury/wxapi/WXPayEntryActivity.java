package com.NewCenturyHotels.NewCentury.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.activity.Html5Activity;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.cons.SharedPref;
import com.NewCenturyHotels.NewCentury.util.SharedPreferencesHelper;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
	
	private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";
	
    private IWXAPI api;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result);
        
    	api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
        api.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {
		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
		    switch (resp.errCode){
                case 0:
                    Toast.makeText(WXPayEntryActivity.this,"支付成功",Toast.LENGTH_LONG).show();
                    SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this);
                    String tradeNo = sharedPreferencesHelper.get(SharedPref.TRADE_NO,"").toString();
                    if(!tradeNo.isEmpty()){
                        Intent intent = new Intent(WXPayEntryActivity.this,Html5Activity.class);
                        intent.putExtra("url", Const.ORDER_DETAIL + tradeNo);
                        startActivity(intent);
                    }
                    break;
                case -1:
                    Toast.makeText(WXPayEntryActivity.this,"支付失败",Toast.LENGTH_LONG).show();
                    break;
                case -2:
                    Toast.makeText(WXPayEntryActivity.this,"已取消",Toast.LENGTH_LONG).show();
                    break;
            }
			finish();
		}
	}

}