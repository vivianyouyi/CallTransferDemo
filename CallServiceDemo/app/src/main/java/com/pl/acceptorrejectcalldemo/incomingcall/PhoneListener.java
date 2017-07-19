package com.pl.acceptorrejectcalldemo.incomingcall;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.pl.acceptorrejectcalldemo.MyApplication;

import playcontrol.DpnConnectManager;
import playcontrol.utils.GlobalConstants;

public class PhoneListener extends BroadcastReceiver {

    DpnConnectManager connectManager;

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e("PhoneListener", action);
        if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
        } else {

            connectManager = MyApplication.getInstance().getConnectManager(); //DpnConnectManager.getInstance(context, GlobalConstants.DEFAULT_SERVER_NAME);

            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Service.TELEPHONY_SERVICE);
            String incoming_number = "";
            switch (tm.getCallState()) {
                case TelephonyManager.CALL_STATE_RINGING:

                    Log.e("PhoneListener ... ", "TelephonyManager.CALL_STATE_RINGING");
                    incoming_number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                   /* try {
                        //3s后再开启activity，是为了挡在系统的接听界面之前
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/

                    connectManager.callin(incoming_number);

                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
            }
        }
    }
}