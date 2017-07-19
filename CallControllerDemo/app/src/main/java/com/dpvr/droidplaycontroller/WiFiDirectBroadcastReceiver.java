
package com.dpvr.droidplaycontroller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import java.util.ArrayList;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    String TAG = "WiFiDirectBroadcastReceiver";
    SearchStatusListener listener;
    ArrayList<String> deviceList = new ArrayList<String>();

    public WiFiDirectBroadcastReceiver() {

    }

    public void setListener(SearchStatusListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtil.d("wifidirect", action);
        //Broadcast intent action to indicate whether Wi-Fi p2p is enabled or disabled
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
            } else {
                if (listener != null) {
                    this.listener.searchFailed("请检查网络配置");
                }
            }
            LogUtil.d(TAG, "P2P state changed - " + state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            WifiP2pDeviceList mPeers =
                    intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
            if (mPeers != null && mPeers.toString() != null) {
                if (mPeers.getDeviceList() != null && mPeers.getDeviceList().size() > 0) {

                    //重新开始搜索设备
                    deviceList.clear();
                    for (WifiP2pDevice d : mPeers.getDeviceList()) {
                        //过滤,并去重
                        String serverName = PreferenceUtil.getString(context, GlobalConstants.SP_SERVER_NAME,GlobalConstants.DEFAULT_SERVER_NAME);
                        if (d.deviceName.contains(serverName)) {
                            if (deviceList.size() < 1) {
                                deviceList.add(d.deviceName);
                            }
                            boolean isAdded = false;
                            for (int i = 0; i < deviceList.size(); i++) {
                                if (d.deviceName.equals(deviceList.get(i))) {
                                    isAdded = true;
                                    break;
                                }
                            }
                            if (!isAdded) {
                                deviceList.add(d.deviceName);
                            }
                        }
                    }
                    if (listener != null && deviceList != null && deviceList.size() > 0) {
                        this.listener.searchSuccess(deviceList);
                    }
                }
                LogUtil.d(TAG, mPeers.toString());
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            WifiP2pInfo mWifiP2pInfo = (WifiP2pInfo) (intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO));
            NetworkInfo mNetworkInfo = (NetworkInfo) (intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO));
            WifiP2pGroup mGroup = (WifiP2pGroup) (intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP));


            if (mWifiP2pInfo != null) LogUtil.d(TAG, mWifiP2pInfo.toString());
            if (mNetworkInfo != null) LogUtil.d(TAG, mNetworkInfo.toString());
            if (mGroup != null) LogUtil.d(TAG, mGroup.toString());


            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection
                // info to find group owner IP
                LogUtil.d(TAG,
                        "Connected to p2p network. Requesting network details");
            } else {

                LogUtil.d(TAG,
                        "Disconnected to p2p network. Requesting network details");
                // It's a disconnect
                if (listener != null) {
                    this.listener.searchFailed("网络异常，请检查网络");
                }
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
                .equals(action)) {

            WifiP2pDevice device = (WifiP2pDevice) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            LogUtil.d(TAG, "Device status -" + device.status);

        } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
            LogUtil.d(TAG, "Device changed -");
        }
    }
}
