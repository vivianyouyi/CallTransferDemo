package com.dpvr.droidplaycontroller;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.dpvr.ui.IncomingCallActivity;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by liweiwei on 2017/5/26.
 */

public class ConnectClientService extends Service implements Runnable, SearchStatusListener {

    private String TAG = "ConnectService";

    private final int MSG_RECEIVE_DATA = 001;
    private final int MSG_SESSION_CREATE = 002;
    private final int MSG_SESSION_CLOSE = 003;

    private Context mContext;

    private static final String CONTROL_TYPE_CAllCOMMAND = "callcommand";
    private static final String CONTROL_TYPE_HEARTBEAT = "heartbeat";

    ConnectListener connectListener;
    private final LocalBinder binder = new LocalBinder();

    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mWifiP2pManager;
    private WiFiDirectBroadcastReceiver receiver = null;
    private IntentFilter intentFilter = new IntentFilter();

    private String serverIp = null;
    private int serverPort = 0;

    Boolean serverChanged = false;
    private Boolean isInit = false;
    private IoConnector connector = null;
    ConnectFuture future;
    IoSession session = null;

    private Thread thread;
    private TimeHandler timeHandler;
    private Boolean hasConnect = false;

    private long checkHeartBeatTime;
    private long receiveHeartBeatTime;
    protected WifiUtils mWifiUtils;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d(TAG, "onStartCommand");

        if (connector == null && !isInit && !hasConnect) {
            isInit = true;
            thread.start();
        }
        if (!getHotSpot()) {
            startDiscovery();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d(TAG, "onCreate");
        mContext = ConnectClientService.this;
        thread = new Thread(ConnectClientService.this);
        timeHandler = new TimeHandler();

        initWifiDirectReceiver();
        initWifiP2pService();

        mWifiUtils = WifiUtils.getInstance(this);
        mWifiUtils.creatWifiLock();
        mWifiUtils.acquireWifiLock();

    }

    private void initWifiP2pService() {
        if (mWifiP2pManager == null) {
            mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
            mChannel = mWifiP2pManager.initialize(this, getMainLooper(), null);
        }
    }

    public Boolean getHotSpot() {
        Boolean isHotSpot;
        mWifiUtils.setNewWifiManagerInfo(); // 更新connection信息

        if ("192.168.43.1".equals(mWifiUtils.getServerIPAddress())) {
            serverIp = mWifiUtils.getServerIPAddress();
            serverPort = 1234;
            isHotSpot = true;
        } else {
            isHotSpot = false;
        }

        return isHotSpot;
    }

    public class TimeHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SESSION_CREATE:
                    if(connectListener !=null) {
                        connectListener.connect("已经连接");
                    }
                    break;
                case MSG_SESSION_CLOSE:
                    if(connectListener !=null) {
                        connectListener.connect("连接断开");
                    }
                    break;
                case MSG_RECEIVE_DATA:
                    LogUtil.d(TAG, " MSG_RECEIVE_DATA " + ";Process.myTid:" + android.os.Process.myTid());
                    String data = (String) msg.obj;
                    dataReceive(data);
                    break;
                default:
                    break;
            }
        }
    }

    public void initWifiDirectReceiver() {
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        receiver = new WiFiDirectBroadcastReceiver();
        registerReceiver(receiver, intentFilter);
    }

    public void unRegisterReceiver() {
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    /**
     * initiates a service discovery
     */
    private void startDiscovery() {
        receiver.setListener(this);
        mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                LogUtil.d(TAG, "Success init discoverPeers");
            }

            @Override
            public void onFailure(int reason) {
                LogUtil.d(TAG, "Failed to discoverPeers");
            }
        });
    }

    private void stopWifiDiscovery() {
        receiver.setListener(null);
        mWifiP2pManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                LogUtil.d(TAG, "Success stopPeerDiscovery");
            }

            @Override
            public void onFailure(int reason) {
                LogUtil.d(TAG, "Failed to stopPeerDiscovery");
            }
        });
    }

    @Override
    public void run() {

        LogUtil.d("TEST", "first time connect...");
        while (true) {

            LogUtil.d(TAG, "isWifi = " + NetworkUtils.isWifi(mContext));
            if (NetworkUtils.isWifi(mContext)) {

                if (connector == null) {
                    connector = new NioSocketConnector();
                    //设置链接超时时间
                    connector.setConnectTimeoutMillis(30000);
                    //添加过滤器
                    connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new
                            TextLineCodecFactory(Charset.forName("UTF-8"), LineDelimiter.WINDOWS.getValue(), LineDelimiter.WINDOWS.getValue())));
                    connector.setHandler(new ClientHandler());
                    //connector.addListener(new HeartBeatListener());
                }

                try {
                    checkHeartBeat();

                    boolean ipIsNull = this.serverIp == null &&
                            this.serverPort == 0;
                    LogUtil.d(TAG, "check connect state every 30s...hasConnect = "
                            + hasConnect.toString() + ";ipIsNull = " + ipIsNull + ";Process.myTid:" + android.os.Process.myTid());

                    if (serverChanged) {
                        LogUtil.d(TAG, "change server...");

                        if (!getHotSpot()) {
                            this.stopWifiDiscovery();
                            this.startDiscovery();
                        }
                        if (future != null && session != null) {
                            future.cancel();
                            future = null;
                            session.closeNow();
                            session = null;
                        }
                    } else if (!ipIsNull) {
                        if (!hasConnect) {
                            LogUtil.d(TAG, "reconnect...");
                            if (future != null && session != null) {
                                future.cancel();
                                future = null;
                                session.closeNow();
                                session = null;
                            }

                            if (!getHotSpot()) {
                                this.stopWifiDiscovery();
                                this.startDiscovery();
                            }

                            future = connector.connect(new InetSocketAddress(this.serverIp, this.serverPort));//创建链接
                            future.awaitUninterruptibly();// 等待连接创建完成
                            session = future.getSession();//获得session

                            String serverName = PreferenceUtil.getString(ConnectClientService.this, GlobalConstants.SP_SERVER_NAME, GlobalConstants.DEFAULT_SERVER_NAME);

                            String serialNumber = android.os.Build.SERIAL;
                            session.write("SerialNumber:" + serialNumber);
                            session.write(serverName + "-" + "IsAlive");
                        } else {
                            if (session != null) {
                                String serverName = PreferenceUtil.getString(ConnectClientService.this, GlobalConstants.SP_SERVER_NAME, GlobalConstants.DEFAULT_SERVER_NAME);
                                session.write(serverName + "-" + "IsAlive");
                            }
                        }
                    } else {
                        if (!getHotSpot()) {
                            this.stopWifiDiscovery();
                            this.startDiscovery();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    hasConnect = false;
                    LogUtil.d(TAG, "mina client exception...");
                }
            }

            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void checkHeartBeat() {
        checkHeartBeatTime = getTimeStamp();
        long time = checkHeartBeatTime - receiveHeartBeatTime;
        LogUtil.d(TAG, "checkHeartBeat = " + time);
        if (time > (long) 30000) {
            hasConnect = false;
        }
    }

    @Override
    public void searchSuccess(ArrayList<String> deviceList) {

        LogUtil.d(TAG, "searchSuccess");
        stopWifiDiscovery();

        for (String serverInfo : deviceList) {

            LogUtil.d(TAG, "serverInfo = " + serverInfo);
            String[] result = serverInfo.split("-");
            if (result != null && result.length > 1) {
                String serverName = PreferenceUtil.getString(ConnectClientService.this, GlobalConstants.SP_SERVER_NAME, GlobalConstants.DEFAULT_SERVER_NAME);

                if (result[0].equals(serverName)) {
                    this.serverIp = result[1];
                    this.serverPort = Integer.parseInt(result[2]);
                    break;
                }
            }
        }

       /* if (connector == null && !isInit && !hasConnect && this.serverIp != null && this.serverPort != 0) {
            isInit = true;
            thread.start();
        }*/

    }

    class ClientHandler extends IoHandlerAdapter {

        @Override
        public void sessionCreated(IoSession session) throws Exception {
            super.sessionCreated(session);

            hasConnect = true;
            Message message1 = Message.obtain();
            message1.what = MSG_SESSION_CREATE;
            timeHandler.sendMessage(message1);
        }

        @Override
        public void sessionClosed(IoSession session) throws Exception {
            LogUtil.i(TAG, "sessionClosed = " + session.getId());
            hasConnect = false;
            super.sessionClosed(session);

            Message message1 = Message.obtain();
            message1.what = MSG_SESSION_CLOSE;
            timeHandler.sendMessage(message1);

        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
            LogUtil.i(TAG, "exceptionCaught = " + session.getId());
            hasConnect = false;
            super.exceptionCaught(session, cause);
        }

        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            super.messageReceived(session, message);
            hasConnect = true;
            String msg = message.toString();

            LogUtil.i(TAG, "messageReceived:" + msg + ";Process.myTid:" + android.os.Process.myTid());
            Message message1 = Message.obtain();
            message1.what = MSG_RECEIVE_DATA;
            message1.obj = msg;
            timeHandler.sendMessage(message1);
        }

        @Override
        public void messageSent(IoSession session, Object msg) throws Exception {
            super.messageSent(session, msg);
            LogUtil.i(TAG, "messageSent:" + msg);
        }
    }

    public void setConnectListener(final ConnectListener listener) {
        connectListener = listener;
    }

    public void callout(String phonenum) {
        if (session != null) {
            session.write(JsonUtils.requestCallCommand("callout", phonenum));
        }
    }

    private void dataReceive(String msg) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(msg);
            String controltype = jsonObject.optString("controltype");
            String operation = jsonObject.optString("operation");
            JSONObject content = jsonObject.optJSONObject("content");

            if (controltype != null && controltype.equals(CONTROL_TYPE_CAllCOMMAND)) {
                operationByCallCommand(operation, content);
            } else if (controltype != null && controltype.equals(CONTROL_TYPE_HEARTBEAT)) {
                operationByHeartBeat(operation);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void operationByCallCommand(String opt, JSONObject content) {
        final String phonenum = content.optString("phonenum");
        switch (opt) {
            case "callin":
                LogUtil.d(TAG, " callin " + phonenum);

                Intent dialogIntent = new Intent(getBaseContext(), IncomingCallActivity.class);
                dialogIntent.putExtra("phonenum", phonenum);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplication().startActivity(dialogIntent);
                break;
        }
    }

    public void callin(boolean isAccept) {
        if (isAccept) {

            System.out.println("liweiwei...2222222");
            session.write(JsonUtils.requestCallCommand(OperationType.CALLACCEPT, ""));
        } else {
            session.write(JsonUtils.requestCallCommand(OperationType.CALLREJECT, ""));
        }
    }

    private void operationByHeartBeat(String opt) {
        String serverName = PreferenceUtil.getString(ConnectClientService.this, GlobalConstants.SP_SERVER_NAME,
                GlobalConstants.DEFAULT_SERVER_NAME);

        if (opt.equals(serverName)) {
            serverChanged = false;
            receiveHeartBeatTime = getTimeStamp();
        } else if (opt.equals("refuse")) {
            serverChanged = true;
            this.serverIp = null;
            this.serverPort = 0;
            //TODO:
            if (future != null && session != null) {
                future.cancel();
                future = null;
                session.closeNow();
                session = null;
            }
        }
    }

    /**
     *    * 获取时间戳
     *    * 输出结果:1438692801766
     *    
     */
    public long getTimeStamp() {
        return System.currentTimeMillis();
    }

    @Override
    public void searchFailed(String err) {

    }

    public class LocalBinder extends Binder {
        public ConnectClientService getService() {
            return ConnectClientService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "Service onDestroy");
        //TODO:mHandlerThread.getLooper().quit();
        unRegisterReceiver();

        mWifiUtils.releaseWifiLock();
    }
}
