package com.dpvr.droidplaycontroller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.List;

/**
 * Created by vivian on 2017/6/27.
 */

public class WifiUtils {
    private static WifiUtils mWifiUtils = null;
    private List<WifiConfiguration> mWifiConfiguration;
    private WifiInfo mWifiInfo;
    private DhcpInfo mDhcpInfo;
    private List<ScanResult> mWifiList;
    private WifiManager.WifiLock mWifiLock;
    public WifiManager mWifiManager;
    private NetworkInfo mNetworkInfo;

    private WifiUtils(Context paramContext) {
        mWifiManager = (WifiManager) paramContext.getSystemService(Context.WIFI_SERVICE);
        mDhcpInfo = mWifiManager.getDhcpInfo();
        mWifiInfo = mWifiManager.getConnectionInfo();
        mNetworkInfo = ((ConnectivityManager) paramContext
                .getSystemService(Context.CONNECTIVITY_SERVICE))
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    }

    public void setNewWifiManagerInfo() {
        mWifiInfo = mWifiManager.getConnectionInfo();
        mDhcpInfo = mWifiManager.getDhcpInfo();
    }

    public static WifiUtils getInstance(Context paramContext) {
        if (mWifiUtils == null)
            mWifiUtils = new WifiUtils(paramContext);
        return mWifiUtils;
    }

    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    public void creatWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("DroidPlayController");
    }

    public void releaseWifiLock() {
        if (mWifiLock.isHeld())
            mWifiLock.release();
    }

    public String getServerIPAddress() {
        if (mDhcpInfo == null)
            return "NULL";
        return intToIp(mDhcpInfo.serverAddress);
    }

    public String intToIp(int paramIntip) {
        return (paramIntip & 0xFF) + "." + ((paramIntip >> 8) & 0xFF) + "."
                + ((paramIntip >> 16) & 0xFF) + "." + ((paramIntip >> 24) & 0xFF);
    }
}
