package com.dpvr.droidplaycontroller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * 检测网络
 *
 */
public class NetworkUtils {

    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;

    /**
     * 判断网络连接状态
     *
     * @param context
     * @return 如果当前网络是联通状态，返回true 否则 false.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null && info.length != 0) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * wifi是否打开
     */
    public static boolean isWifiEnabled(Context context) {
        ConnectivityManager mgrConn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager mgrTel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return ((mgrConn.getActiveNetworkInfo() != null && mgrConn.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED) || mgrTel.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS);
    }

    /**
     * 判断当前网络是否是wifi网络 if(activeNetInfo.getType()==ConnectivityManager.TYPE_MOBILE) { //判断3G�?
     *
     * @param context
     * @return boolean
     */
    public static boolean isWifi(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * 判断当前G网络是否连接，true表示连接，反之则不是
     *
     * @param context
     * @return boolean
     */
    public static boolean is3G(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return true;
        }
        return false;
    }

    /**
     * 判断网络连接状态
     *
     * @param context
     * @return 如果当前网络是联通信状态返回true 否则 false.
     */
    public static boolean checkNetwork(Context context) {

        try {
            ConnectivityManager connectivity =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {

                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {

                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static void showNetWorkError(Context context) {
        // Toast.makeText(context, context.getResources().getString(R.string.net_fail), Toast.LENGTH_SHORT).show();
    }

    /**
     * 获取wifi名称
     *
     * @param context
     * @return
     */
    public static String getWifiSSID(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        return info.getSSID();
    }

    public static int getConnectivityStatus(Context context) {

        if (isWifi(context))
            return TYPE_WIFI;

        if (is3G(context))
            return TYPE_MOBILE;

        return TYPE_NOT_CONNECTED;
    }

    public static String getIPAddress(String intfName) {
        NetworkInterface intf = null;
        try {
            intf = NetworkInterface.getByName(intfName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (intf != null) {
            List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
            for (InetAddress addr : addrs) {
                if (!addr.isLoopbackAddress()) {
                    String sAddr = addr.getHostAddress();
                    boolean isIPv4 = sAddr.indexOf(':') < 0;
                    Log.d("IPAddress", sAddr);
                    if (isIPv4)
                        return sAddr;
                }
            }
        }
        return null;
    }
}
