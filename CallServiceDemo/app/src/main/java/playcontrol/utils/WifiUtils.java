package playcontrol.utils;

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

    /**
     * 获取热点状态
     *
     * @return boolean值，对应热点的开启(true)和关闭(false)
     */
    public boolean getWifiApState() {
        try {
            int i = ((Integer) mWifiManager.getClass().getMethod("getWifiApState", new Class[0])
                    .invoke(mWifiManager, new Object[0])).intValue();
            //3:WIFI_STATE_ENABLED  13:WIFI_AP_STATE_ENABLED hide
            /*public static final int WIFI_AP_STATE_DISABLING = 10;
            public static final int WIFI_AP_STATE_DISABLED = 11;
            public static final int WIFI_AP_STATE_ENABLING = 12;
            public static final int WIFI_AP_STATE_ENABLED = 13;
            public static final int WIFI_AP_STATE_FAILED = 14;*/

            LogUtil.d("getWifiApState", "i = " + i);
            return/* (3 == i) ||*/ (13 == i);
        } catch (Exception localException) {
        }
        return false;
    }

    /**
     * 判断是否连接上wifi
     *
     * @return boolean值(isConnect), 对应已连接(true)和未连接(false)
     */
    public boolean isWifiConnect() {
        return mNetworkInfo.isConnected();
    }

    public String getLocalIPAddress() {
        if (mWifiInfo == null)
            return "NULL";
        return intToIp(mWifiInfo.getIpAddress());
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
