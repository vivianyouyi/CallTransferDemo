package playcontrol;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;

import playcontrol.utils.GlobalConstants;
import playcontrol.utils.PreferenceUtil;

/**
 * Created by liweiwei on 2017/5/26.
 */

public class DpnConnectManager implements ServiceConnection {


    private Context context;
    private ConnectService service;
    private boolean isBinded = false;

    public DpnConnectManager(Context context) {
        this.context = context;
        openServiceIfNeed();
    }

    private void openServiceIfNeed() {
        if (this.context != null /*&& this.service == null*/) {
            Intent service = new Intent(this.context, ConnectService.class);
            this.context.startService(service);

            if (this.isBinded) {
                this.context.unbindService(this);
                this.isBinded = false;
            }
            this.context.bindService(service, this, Context.BIND_AUTO_CREATE);
        }
    }

    public void closeServiceForce() {

        if (this.context != null) {
            Intent service = new Intent(this.context, ConnectService.class);

            if (this.isBinded) {
                this.context.unbindService(this);
                this.isBinded = false;
            }
            this.context.stopService(service);
            this.service = null;
        }
    }

    public void setControllerName(String name) {

        String sp = PreferenceUtil.getString(this.context, GlobalConstants.SERVER_NAME, GlobalConstants.DEFAULT_SERVER_NAME);

        if (sp != null && this.service != null) {

            PreferenceUtil.putString(this.context, GlobalConstants.SERVER_NAME, name);

            if (!service.getHotSpot()) {
                this.service.stopWifiDiscovery();
                this.service.startDiscovery();
            }
        }
    }

    public void setDevicesListener(DevicesDetectListener listener) {

        if (this.service != null) {
            this.service.setDevicesListener(listener);
        }
    }

    public ArrayList<DpnDeviceInfo> getDeviceInfoList() {

        if (this.service != null) {
            return this.service.getDeviceInfoList();
        }
        return null;
    }

    public void callin(String phonenum) {
        if (this.service != null) {
            this.service.sendCallCommand(OperationType.CALLIN, phonenum);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((ConnectService.LocalBinder) binder).getService();
        isBinded = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
        isBinded = false;
    }
}
