package com.dpvr;

import android.app.Application;
import android.content.Context;
import android.os.PowerManager;

import com.dpvr.droidplaycontroller.MinaClientManager;
import com.dpvr.ui.BaseActivity;
import com.jd.wly.intercom.InterAudioManager;
import com.jd.wly.intercom.app.App;


/**
 * @author
 */
public class BaseApplication extends App {
    private static final String TAG = BaseApplication.class.getSimpleName();

    public static Context mContext;
    protected InterAudioManager audioManager;
    protected MinaClientManager connectManager;
    private static BaseApplication instance;

    private PowerManager.WakeLock mWakeLock = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mContext = this.getApplicationContext();
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();
    }


    public static BaseApplication getInstance() {
        return instance;
    }


    public InterAudioManager getAudioManager() {
        if (this.audioManager == null) {
            this.audioManager = new InterAudioManager(mContext);
        }
        return this.audioManager;
    }

    public MinaClientManager getConnectManager() {
        if (this.connectManager == null) {
            connectManager = new MinaClientManager(mContext);
        }
        return this.connectManager;
    }

    public void killSelf() {
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
    }

    @Override
    public void onTerminate() {
        if (mWakeLock.isHeld())
            mWakeLock.release();
        super.onTerminate();
    }
}
