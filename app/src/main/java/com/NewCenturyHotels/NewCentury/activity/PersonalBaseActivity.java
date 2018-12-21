package com.NewCenturyHotels.NewCentury.activity;

import android.app.DatePickerDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.NewCenturyHotels.NewCentury.R;
import com.NewCenturyHotels.NewCentury.bean.Dict;
import com.NewCenturyHotels.NewCentury.bean.UserCenter;
import com.NewCenturyHotels.NewCentury.cons.Const;
import com.NewCenturyHotels.NewCentury.req.ChangeUserInfoReq;
import com.NewCenturyHotels.NewCentury.req.DictListReq;
import com.NewCenturyHotels.NewCentury.util.HttpHelper;
import com.NewCenturyHotels.NewCentury.util.StatusBarUtils;
import com.NewCenturyHotels.NewCentury.util.TimeUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 个人基本信息
 */
public class PersonalBaseActivity extends SwipeBackActivity implements View.OnClickListener{

    RelativeLayout loading;
    RelativeLayout pbase_back;
    TextView pbase_finish;
    EditText pbase_et_name;
    EditText pbase_et_cardno;
    //性别选择
    RadioButton rb_male;
    RadioButton rb_female;

    //证件数据
    Spinner pbase_sp_cardtype;
    SimpleAdapter cardAdapter;

    //时间日期选择
    TextView pbase_date;
    String date = "";
    int _year = 0;
    int _month;
    int _day;

    LinearLayout statusBar;

    List<Map<String,Object>> spData;

    //用户填写的值
    String name;
    String cardNo;
    String birthday;
    Integer cardIndex;
    String sex;

    //页面传递的值
    String _name;
    String _sex;
    String _birthday;
    String _idType;
    String _idNo;

    final static String TAG = PersonalBaseActivity.class.getName();

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try{
                stopLoading();
                if(msg.what == 1){//修改个人信息
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        Toast.makeText(PersonalBaseActivity.this,message,Toast.LENGTH_LONG).show();
                        finish();
                    }else{
                        Toast.makeText(PersonalBaseActivity.this,message,Toast.LENGTH_LONG).show();
                    }
                }else if(msg.what == 2){//证件类型列表
                    String ret = msg.getData().getString("ret");
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(ret);
                    int code = jo.get("code").getAsInt();
                    String message = jo.get("msg").getAsString();
                    if(code == 200){
                        JsonArray cardArr = jo.getAsJsonArray("data");
                        Gson gson = new Gson();
                        spData.clear();
                        for(int i = 0;i < cardArr.size(); i++){
                            Dict dict = gson.fromJson(cardArr.get(i),Dict.class);
                            Map<String,Object> map = new HashMap<>();
                            map.put("name",dict.getName());
                            map.put("code",dict.getCode());
                            spData.add(map);
                        }

                        cardAdapter.notifyDataSetChanged();
                        for(int j = 0;j < spData.size();j++){
                            if(spData.get(j).get("code").toString().equals(_idType)){
                                pbase_sp_cardtype.setSelection(j);
                            }
                        }
                    }else{
                        Toast.makeText(PersonalBaseActivity.this,message,Toast.LENGTH_LONG).show();
                    }
                }
            }catch (Exception e){
                Log.e(TAG, "Exception: " + e.getMessage());
                Toast.makeText(getApplicationContext(),"连接超时",Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_base);
        StatusBarUtils.with(this).init();
        initComponent();
        initEvent();
    }

    void initComponent(){
        loading = (RelativeLayout) findViewById(R.id.pbase_loading);
        pbase_back = (RelativeLayout) findViewById(R.id.pbase_back);
        pbase_finish = (TextView) findViewById(R.id.pbase_finish_tv);
        pbase_date = (TextView) findViewById(R.id.pbase_tv_birthday);
        pbase_et_name = (EditText) findViewById(R.id.pbase_et_name);
        pbase_et_cardno = (EditText) findViewById(R.id.pbase_et_cardno);
        pbase_sp_cardtype = (Spinner) findViewById(R.id.pbase_sp_cardtype);
        rb_male = (RadioButton) findViewById(R.id.pbase_male);
        rb_female = (RadioButton) findViewById(R.id.pbase_female);

        //调整通知栏高度
        statusBar = (LinearLayout) findViewById(R.id.pbase_status_bar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        layoutParams.height = StatusBarUtils.getStatusBarHeight(this);
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        statusBar.setLayoutParams(layoutParams);

        //设置radiobutton图片大小
        Drawable drawable_news = getResources().getDrawable(R.drawable.radio_select);
        drawable_news.setBounds(0, 0, 50, 50);
        rb_male.setCompoundDrawables(drawable_news, null, null, null);

        Drawable drawable_female = getResources().getDrawable(R.drawable.radio_select);
        drawable_female.setBounds(0, 0, 50, 50);
        rb_female.setCompoundDrawables(drawable_female, null, null, null);

        _name = getIntent().getStringExtra("name");
        _sex = getIntent().getStringExtra("sex");
        _birthday = getIntent().getStringExtra("birthday");
        _idNo = getIntent().getStringExtra("idNo");
        _idType = getIntent().getStringExtra("idType");

        pbase_et_name.setText(_name);
        pbase_et_name.setSelection(pbase_et_name.getText().length());
        if(_sex.equals("M")){
            rb_male.setChecked(true);
        }else{
            rb_female.setChecked(true);
        }
        pbase_date.setText(_birthday);
        pbase_et_cardno.setText(_idNo);

        spData= new ArrayList<>();

        String[] from = new String[]{"name"};
        int[] to = new int[]{R.id.spitem_name};

        cardAdapter = new SimpleAdapter(this,spData,R.layout.sp_item,from,to);
        pbase_sp_cardtype.setAdapter(cardAdapter);
    }

    void startLoading(){
        loading.setVisibility(View.VISIBLE);
        loading.setOnClickListener(null);
    }

    void stopLoading(){
        loading.setVisibility(View.GONE);
    }

    void initEvent(){
        pbase_date.setOnClickListener(this);
        pbase_back.setOnClickListener(this);
        pbase_finish.setOnClickListener(this);

        startLoading();
        new Thread(){
            @Override
            public void run() {
                String url = Const.DICT_LIST;
                Gson gson = new Gson();
                DictListReq req = new DictListReq();
                req.setGroupCode("IDTYPE");
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
                        msg.what = 2;
                        handler.sendMessage(msg);
                    }
                });
            }
        }.start();
    }

    void changeUserInfo(){
        new Thread(){
            @Override
            public void run() {
                String url = Const.UPDATE_USER_INFO;
                Gson gson = new Gson();
                ChangeUserInfoReq req = new ChangeUserInfoReq();
                req.setBirthday(birthday);
                req.setNameCN(name);
                req.setSex(sex);
                req.setIdCode(cardNo);
                req.setIdType(spData.get(cardIndex).get("code").toString());
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pbase_back:
                finish();
                break;
            case R.id.pbase_tv_birthday:
                //初始化日期对话框
                if(_year == 0){
                    Calendar ca = Calendar.getInstance();
                    TimeUtil util = new TimeUtil();
                    ca.setTime(util.parseDate(_birthday, TimeUtil.YYYYMMDD));
                    _year = ca.get(Calendar.YEAR);
                    _month = ca.get(Calendar.MONTH);
                    _day = ca.get(Calendar.DAY_OF_MONTH);
                }

                //日期选择
                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                _year = year;
                                _month = month;
                                _day = dayOfMonth;
                                date =  year + "-" + String.format("%02d",(month+1)) + "-" + String.format("%02d",dayOfMonth);
                                pbase_date.setText(date);
                            }
                        },
                        _year, _month,_day);
                datePickerDialog.show();

                break;
            case R.id.pbase_finish_tv:
                name = pbase_et_name.getText().toString().trim();
                cardNo = pbase_et_cardno.getText().toString().trim();
                birthday = pbase_date.getText().toString().trim();
                cardIndex = pbase_sp_cardtype.getSelectedItemPosition();
                sex = rb_male.isChecked() ? "M":"F";

                if(name.isEmpty()){
                    Toast.makeText(PersonalBaseActivity.this,"请输入姓名",Toast.LENGTH_LONG).show();
                    return;
                }
                if(cardNo.isEmpty()){
                    Toast.makeText(PersonalBaseActivity.this,"请输入证件信息",Toast.LENGTH_LONG).show();
                    return;
                }

                startLoading();
                changeUserInfo();
                break;
        }
    }
}
