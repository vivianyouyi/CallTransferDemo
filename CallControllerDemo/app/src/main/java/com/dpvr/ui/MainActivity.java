package com.dpvr.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dpvr.droidplaycontroller.ConnectClientService;
import com.dpvr.droidplaycontroller.ConnectListener;
import com.dpvr.droidplaycontroller.DeviceUtil;
import com.dpvr.droidplaycontroller.GlobalConstants;
import com.dpvr.droidplaycontroller.R;


public class MainActivity extends BaseActivity implements ConnectListener {
    EditText et_phone;
    Button bt_confirm;
    TextView device_name;

    private TextView versionTextView;

    Button btn_start_test;
    Button btn_end_test;

    private MyHandler myHandler;

    public class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectManager.setServerName(GlobalConstants.DEFAULT_SERVER_NAME);
        initView();
        btn_start_test = (Button) findViewById(R.id.btn_start_test);
        btn_end_test = (Button) findViewById(R.id.btn_end_test);

        btn_start_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioManager.startRecord();
            }
        });
        btn_end_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                audioManager.stopRecord();
            }
        });

        myHandler = new MyHandler();

        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                connectManager.setConnectListener(MainActivity.this);
            }
        }, 2000);
    }

    private void initView() {

        versionTextView = (TextView) findViewById(R.id.version_tv);
        device_name = (TextView) findViewById(R.id.device_name);
        String versionName = DeviceUtil.getVersionName(this);
        versionTextView.setText(versionName);

        et_phone = (EditText) findViewById(R.id.edittext_phone);

        bt_confirm = (Button) findViewById(R.id.btn_confirm);
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phonenum = et_phone.getText().toString();
                if (phonenum.length() > 0) {
                    connectManager.callout(phonenum);
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void connect(String status) {
        device_name.setText(status);
    }
}
