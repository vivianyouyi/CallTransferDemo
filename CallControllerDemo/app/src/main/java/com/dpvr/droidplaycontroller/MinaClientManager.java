package com.dpvr.droidplaycontroller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created by liweiwei on 2017/5/26.
 */

public class MinaClientManager implements ServiceConnection {

    private Context context;
    private ConnectClientService service = null;
    private boolean isBinded = false;

    public MinaClientManager(Context context) {
        this.context = context;
        openServiceIfNeed();
    }

    public void openServiceIfNeed() {

        LogUtil.d("", "this.service = " + this.service);
        if (this.context != null && this.service == null) {
            Intent service = new Intent(this.context, ConnectClientService.class);
            this.context.startService(service);

            if (this.isBinded) {
                this.context.unbindService(this);
            }

            this.context.bindService(service, this, Context.BIND_AUTO_CREATE);
        }
    }


    public void setServerName(String name) {

        PreferenceUtil.putString(this.context, GlobalConstants.SP_SERVER_NAME, name);

    }

    public void setConnectListener(ConnectListener listener) {

        if (this.service != null) {
            this.service.setConnectListener(listener);
        }
    }

    private void closeServiceForce() {
        LogUtil.d("", "closeServiceForce = " + this.service);
        if (this.context != null) {
            Intent intent = new Intent(this.context, ConnectClientService.class);

            if (this.isBinded) {
                this.context.unbindService(this);
            }
            this.context.stopService(intent);
            this.service = null;

        }
    }

    public void callin(boolean isAccept) {

        System.out.println("liweiwei...111111 = " + this.service);
        if (this.service != null) {
            this.service.callin(isAccept);
        }
    }

    public void callout(String phonenum) {
        if (this.service != null) {
            this.service.callout(phonenum);
        }
    }

    public void stopWifiDiscovery() {
        if (this.service != null) {
            //this.service.stopWifiDiscovery();
        }
    }

    public void getConnectStatus() {
        if (this.service != null) {
            // this.service.stopWifiDiscovery();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((ConnectClientService.LocalBinder) binder).getService();
        LogUtil.d("", "closeServiceForce = " + this.service);
        isBinded = true;

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
        isBinded = false;
    }
}
