package com.pl.acceptorrejectcalldemo;

import android.app.Activity;
import android.os.Bundle;

import com.jd.wly.intercom.InterAudioManager;

import playcontrol.DpnConnectManager;
import playcontrol.utils.GlobalConstants;


/**
 * Created by 20170418003 on 2017/7/11.
 */

public class BaseActivity extends Activity {
    public DpnConnectManager connectManager;
    protected InterAudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectManager = MyApplication.getInstance().getConnectManager();
        audioManager = MyApplication.getInstance().getAudioManager();
    }
}
