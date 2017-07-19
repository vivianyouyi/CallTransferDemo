package com.pl.acceptorrejectcalldemo;

import android.app.Application;
import android.content.Context;
import android.os.PowerManager;

import com.jd.wly.intercom.InterAudioManager;
import com.jd.wly.intercom.app.App;

import playcontrol.DpnConnectManager;

/**
 * Created by liweiwei on 2017/7/3.
 */

public class MyApplication extends App {
    private static final String TAG = MyApplication.class.getSimpleName();
    private PowerManager.WakeLock mWakeLock = null;

    DpnConnectManager connectManager;
    protected InterAudioManager audioManager;
    public static Context mContext;
    private static MyApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        mContext = this.getApplicationContext();
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();
    }
    public static MyApplication getInstance() {
        return instance;
    }

    public InterAudioManager getAudioManager() {
        if (this.audioManager == null) {
            this.audioManager = new InterAudioManager(mContext);
        }
        return this.audioManager;
    }

    public DpnConnectManager getConnectManager() {
        if (this.connectManager == null) {
            connectManager = new DpnConnectManager(mContext);
        }
        return this.connectManager;
    }
    @Override
    public void onTerminate() {
        if (mWakeLock.isHeld())
            mWakeLock.release();
        super.onTerminate();
    }
}
