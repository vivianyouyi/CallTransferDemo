package com.dpvr.ui;

import android.app.Activity;
import android.os.Bundle;

import com.dpvr.BaseApplication;
import com.dpvr.droidplaycontroller.MinaClientManager;
import com.jd.wly.intercom.InterAudioManager;

/**
 * Created by 20170418003 on 2017/7/11.
 */

public class BaseActivity extends Activity {
    MinaClientManager connectManager;
    protected InterAudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectManager = BaseApplication.getInstance().getConnectManager();
        audioManager = BaseApplication.getInstance().getAudioManager();
    }
}
