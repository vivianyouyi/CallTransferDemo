package com.jd.wly.intercom;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.jd.wly.intercom.service.IIntercomCallback;
import com.jd.wly.intercom.service.IIntercomService;
import com.jd.wly.intercom.service.IntercomService;

/**
 * Created by vivian on 2017/7/19.
 */

public class InterAudioManager implements ServiceConnection {
    private static final String TAG = "InterAudioManager";
    private Context context;
    private IntercomService service;
    private IIntercomService intercomService;
    private boolean isBinded = false;

    public InterAudioManager(Context context) {
        this.context = context;
        openServiceIfNeed();
        initAudioManager();

    }

    public void openServiceIfNeed() {

        if (this.context != null && this.service == null) {
            Intent service = new Intent(this.context, IntercomService.class);
            this.context.startService(service);

            System.out.println("--this.isBinded-----" + this.isBinded);
            if (this.isBinded) {
                this.context.unbindService(this);
                this.isBinded = false;
            }
            this.context.bindService(service, this, Context.BIND_AUTO_CREATE);
        }
    }

    public void closeServiceForce() {


        if (intercomService != null && intercomService.asBinder().isBinderAlive()) {
            try {
                intercomService.unRegisterCallback(intercomCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        System.out.println("--this.closeServiceForce-----context" + this.context);
        if (this.context != null) {
            Intent service = new Intent(this.context, IntercomService.class);

            if (this.isBinded) {
                this.context.unbindService(this);
                this.isBinded = false;
            }
            this.context.stopService(service);
            this.service = null;
        }
    }

    /**
     * 初始化AudioManager配置
     */
    private void initAudioManager() {
        AudioManager audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(true);
    }

    public void startRecord() {

        try {
            if (intercomService != null)
                intercomService.startRecord();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void stopRecord() {

        try {
            if (intercomService != null)
                intercomService.stopRecord();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    /**
     * 被调用的方法运行在Binder线程池中，不能更新UI
     */
    private IIntercomCallback intercomCallback = new IIntercomCallback.Stub() {
        @Override
        public void findNewUser(String ipAddress) throws RemoteException {

            System.out.println("--findNewUser-----" + ipAddress);
        }

        @Override
        public void removeUser(String ipAddress) throws RemoteException {

            System.out.println("--removeUser-----" + ipAddress);
        }
    };

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        intercomService = IIntercomService.Stub.asInterface(service);
        try {
            intercomService.registerCallback(intercomCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        isBinded = true;
        Log.v(TAG, "onServiceConnected");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
        isBinded = false;
    }
}
