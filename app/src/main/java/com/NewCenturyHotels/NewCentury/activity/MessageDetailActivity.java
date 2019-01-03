package com.NewCenturyHotels.NewCentury.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.bean.MessageDetail;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.req.MessageReq;
import com.NewCenturyHotels.NewCentury.util.HttpHelper;
import com.NewCenturyHotels.NewCentury.util.StatusBarUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zzhoujay.richtext.ImageHolder;
import com.zzhoujay.richtext.RichText;

import java.io.IOException;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MessageDetailActivity extends SwipeBackActivity implements View.OnClickListener{

    RelativeLayout msgdet_back;
    TextView msgdet_date;
    TextView msgdet_content;
    TextView msgdet_title;
    TextView msgdet_pub;
    LinearLayout statusBar;

    Integer msgId;

    final String TAG = MessageDetailActivity.class.getName();

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try{
                if(msg.what == 1){
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        JsonObject data = jo.getAsJsonObject("data");
                        Gson gson = new Gson();
                        MessageDetail detail = gson.fromJson(data,MessageDetail.class);
                        RichText.from(detail.getComment()).bind(this)
                                .showBorder(false)
                                .size(ImageHolder.MATCH_PARENT, ImageHolder.WRAP_CONTENT).into(msgdet_content);
                        msgdet_title.setText(detail.getTitle());
                        msgdet_date.setText(detail.getCreateDateTime());
                        msgdet_pub.setVisibility(View.VISIBLE);

                        Toast.makeText(MessageDetailActivity.this,"加载成功",Toast.LENGTH_LONG).show();
                    }else if(code == 991 || code == 992 || code == 993 || code == 995){
                        HttpHelper.reLogin(MessageDetailActivity.this);
                    }else{
                        Toast.makeText(MessageDetailActivity.this,message,Toast.LENGTH_LONG).show();
                    }

                }
            }catch (Exception e){
                Log.e(TAG, "Exception: " + e.getMessage());
                Toast.makeText(MessageDetailActivity.this,"连接超时",Toast.LENGTH_LONG).show();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);
        StatusBarUtils.with(this).init();
        Intent intent = getIntent();
        msgId = intent.getIntExtra("msgId",-1);
        Log.i(TAG, "onCreate: " + msgId);
        initComponent();
        initEvent();
    }

    void initComponent(){
        msgdet_back = (RelativeLayout) findViewById(R.id.msgdet_back);
        msgdet_date = (TextView) findViewById(R.id.msgdet_tv_date);
        msgdet_content = (TextView) findViewById(R.id.msgdet_tv_content);
        msgdet_title = (TextView) findViewById(R.id.msgdet_title);
        msgdet_pub = (TextView) findViewById(R.id.msgdet_pub);

        RichText.initCacheDir(this);

        //调整通知栏高度
        statusBar = (LinearLayout) findViewById(R.id.msgdet_status_bar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this);
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        statusBar.setLayoutParams(layoutParams);
    }

    void initEvent(){
        msgdet_back.setOnClickListener(this);

        new Thread(){
            @Override
            public void run() {
                String url = Const.MSG_DETAIL;
                Gson gson = new Gson();
                MessageReq req = new MessageReq();
                req.setId(msgId);
                String json = gson.toJson(req);
                HttpHelper.sendOkHttpPost(url, json, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "onFailure: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String body = response.body().string();
                        Log.i(TAG, "onResponse: " + body);
                        Bundle bundle = new Bundle();
                        bundle.putString("ret",body);
                        Message msg = new Message();
                        msg.setData(bundle);
                        msg.what = 1;
                        handler.sendMessage(msg);
                    }
                });
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RichText.clear(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.msgdet_back:
                finish();
                break;
        }
    }
}
