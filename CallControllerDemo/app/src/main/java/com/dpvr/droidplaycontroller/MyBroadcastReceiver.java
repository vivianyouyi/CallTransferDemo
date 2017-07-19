package com.dpvr.droidplaycontroller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by mm on 2016/7/29.
 */
public class MyBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "MyBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        LogUtil.d(TAG, intent.getAction());
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            LogUtil.d("TAG", "开机");

            MinaClientManager clientManager = new MinaClientManager(context);
            clientManager.openServiceIfNeed();
        }
    }
}
