package playcontrol;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import playcontrol.utils.GlobalConstants;
import playcontrol.utils.JsonUtils;
import playcontrol.utils.LogUtil;
import playcontrol.utils.NetworkUtils;
import playcontrol.utils.PreferenceUtil;
import playcontrol.utils.WifiUtils;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


/**
 * Created by liweiwei on 2017/5/27 0003.
 */
public class ConnectService extends Service {
    private String TAG = "ConnectService";

    private final int MSG_REINIT_P2P = 001;
    private final int MSG_RECEIVE_CLIENT = 002;
    private final int MSG_CLOSED_CLIENT = 003;
    private final int MSG_EXCEPTION_CLIENT = 004;
    private final int MSG_RECEIVE_HEARTBEAT = 005;
    private final int MSG_RECEIVE_COMMAND = 006;

    private static final String CONTROL_TYPE_CALLCOMMAND = "callcommand";

    IoAcceptor acceptor = null;

    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.ActionListener mSetDevNameListener;

    ArrayList<DpnDeviceInfo> deviceList;
    DevicesDetectListener devicesDetectListener;

    private final LocalBinder binder = new LocalBinder();
    private TimeHandler timeHandler;

    private Boolean hasConnect = false;

    protected WifiUtils mWifiUtils;
    private String serverIPaddres;
    // private Boolean isHotSpot = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d(TAG, "onStartCommand");

        initMinaServer();

        if (!getHotSpot()) {
            if (NetworkUtils.isWifi(ConnectService.this)) {
                startDiscovery();
            }
        }

        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        deviceList = new ArrayList<>();
        timeHandler = new TimeHandler();

        initWifiP2pService();
        new Thread(new TimeRunnable()).start();
    }

    public Boolean getHotSpot() {
        Boolean isHotSpot;
        mWifiUtils = WifiUtils.getInstance(this);
        mWifiUtils.setNewWifiManagerInfo(); // 更新connection信息


        if (mWifiUtils.getWifiApState()) {
            serverIPaddres = "192.168.43.1"; // android默认AP地址
            isHotSpot = true;
        } else {
            serverIPaddres = NetworkUtils.getIPAddress("wlan0");
            isHotSpot = false;
        }

        LogUtil.d(TAG, " serverIPaddres:" + serverIPaddres);
        return isHotSpot;
    }

    public class TimeHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_REINIT_P2P:
                    if (!getHotSpot()) {
                        stopWifiDiscovery();
                        startDiscovery();
                        LogUtil.d(TAG, "reset initial p2p after 200s");
                    }
                    //每隔200s检查存活的session数目并同步devices
                    checkSessionAndResetDevices();
                    //检查Server是否存活
                    initMinaServer();
                    break;

                //收到新的连接，关闭连接，服务器异常的时候，检查存活的session数目并同步devices
                case MSG_RECEIVE_CLIENT:
                case MSG_CLOSED_CLIENT:
                case MSG_RECEIVE_HEARTBEAT:
                    checkSessionAndResetDevices();
                    break;
                case MSG_RECEIVE_COMMAND:
                    String data = (String) msg.obj;

                    LogUtil.d(TAG, "11111MSG_RECEIVE_COMMAND = " + data);
                    dataReceive(data);
                    break;
                case MSG_EXCEPTION_CLIENT:
                    acceptor.unbind();
                    break;
                default:
                    break;
            }
        }
    }

    private class TimeRunnable implements Runnable {

        @Override
        public void run() {

            while (true) {
                try {
                    //每隔200秒重新开启一次discoverPeers
                    Thread.sleep(200000);
                    LogUtil.d(TAG, "isWifi....= " + NetworkUtils.isWifi(ConnectService.this));
                    Message msg = Message.obtain();
                    msg.what = MSG_REINIT_P2P;
                    timeHandler.sendMessage(msg);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initWifiP2pService() {
        if (mWifiP2pManager == null) {
            mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
            mChannel = mWifiP2pManager.initialize(this, getMainLooper(), null);
        }
    }

    public void startDiscovery() {
        mSetDevNameListener = new DpnSetDevNameListener();
        changeP2pDeviceName();
        mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                LogUtil.d(TAG, "Discovery Initiated");
            }

            @Override
            public void onFailure(int reasonCode) {
                LogUtil.d(TAG, "Discovery Failed");
            }
        });
    }

    public void stopWifiDiscovery() {
        mWifiP2pManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                LogUtil.d(TAG, "stopPeerDiscovery Initiated");
            }

            @Override
            public void onFailure(int reason) {
                LogUtil.d(TAG, "stopPeerDiscovery Failed");
            }
        });
    }

    public void changeP2pDeviceName() {

        String serverName = PreferenceUtil.getString(ConnectService.this, GlobalConstants.SERVER_NAME, GlobalConstants.DEFAULT_SERVER_NAME);
        String mDevName = serverName + "-" + NetworkUtils.getIPAddress("wlan0") + "-" + GlobalConstants.PORT;
        setDeviceName(mChannel, mDevName, mSetDevNameListener);
    }

    private class DpnSetDevNameListener implements WifiP2pManager.ActionListener {
        @Override
        public void onSuccess() {
            LogUtil.d(TAG, "Set WIFIP2P P2P devname done");
        }

        @Override
        public void onFailure(int arg0) {
            LogUtil.e(TAG, "Failed to set WIFIP2P devname!");
        }
    }

    private void setDeviceName(WifiP2pManager.Channel c, String name, WifiP2pManager.ActionListener listener) {
        try {
            Class cWifiP2pManager = WifiP2pManager.class;
            Method method = cWifiP2pManager.getMethod("setDeviceName",
                    new Class[]{WifiP2pManager.Channel.class, String.class, WifiP2pManager.ActionListener.class});
            method.invoke(mWifiP2pManager, c, name, listener);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        LogUtil.d(TAG, "Set setDeviceName:  " + name);
    }

    private void initMinaServer() {
        try {
            if (acceptor == null) {

                LogUtil.d(TAG, "server init...");
                acceptor = new NioSocketAcceptor();
                acceptor.getFilterChain().addLast("codec",
                        new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"),
                                LineDelimiter.WINDOWS.getValue(), LineDelimiter.WINDOWS.getValue())));
                acceptor.setHandler(new ServerHandler());
            }

            LogUtil.d(TAG, "server bind....  hasConnect = " + hasConnect);

            if (!hasConnect /*|| shouldCheckServer*/) {
                acceptor.unbind();
                acceptor.bind(new InetSocketAddress(GlobalConstants.PORT));
                hasConnect = true;
            }

        } catch (Exception e) {
            LogUtil.d(TAG, "服务器启动异常...");
            hasConnect = false;
            e.printStackTrace();
        }
    }

    /**
     * 检查存活的session，并重置deviceList
     */
    private synchronized void checkSessionAndResetDevices() {

        //ArrayList<DpnDeviceInfo> tempList = new ArrayList<>();
        deviceList.clear();
        Map conMap = acceptor.getManagedSessions();
        Iterator iter = conMap.keySet().iterator();
        LogUtil.d(TAG, "conMap = " + conMap.size());
        while (iter.hasNext()) {
            Object key = iter.next();
            IoSession session = (IoSession) conMap.get(key);
            if (session != null) {
                long current = System.currentTimeMillis();
                LogUtil.d(TAG, "session.getLastReadTime() = " + session.getLastReadTime());
                LogUtil.d(TAG, "session.heartbeat = " + (current - session.getLastReadTime()));

                long heartBeat = current - session.getLastReadTime();
                if (heartBeat > (long) 60000) {
                    session.closeNow();
                    continue;
                }
                InetSocketAddress address = (InetSocketAddress) session.getRemoteAddress();
                if (address != null) {
                    String ip = address.getHostString();
                    String serialNumber = "";
                    if (session.getAttribute(GlobalConstants.ATTR_SERIAL_NUMBER) != null) {

                        serialNumber = session.getAttribute(GlobalConstants.ATTR_SERIAL_NUMBER).toString();
                    }
                    DpnDeviceInfo info = new DpnDeviceInfo();
                    info.deviceAddress = ip;
                    info.serialNumber = serialNumber;
                    info.deviceName = session.getId() + "";
                    LogUtil.d(TAG, "info" + info.toString());
                    deviceList.add(info);
                }
            }
        }

        if (devicesDetectListener != null) {
            devicesDetectListener.devicesChanged(deviceList);
        }

    }

    private void dataReceive(String msg) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(msg);
            String controltype = jsonObject.optString("controltype");
            String operation = jsonObject.optString("operation");
            JSONObject content = jsonObject.optJSONObject("content");

            LogUtil.d(TAG, "11111controltype = " + controltype);
            LogUtil.d(TAG, "11111operation = " + operation);
            if (controltype != null && controltype.equals(CONTROL_TYPE_CALLCOMMAND)) {
                operationByCallCommand(operation, content);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void operationByCallCommand(String opt, JSONObject content) {
        final String phonenum = content.optString("phonenum");

        LogUtil.d(TAG, "operationByCallCommand opt = " + opt);
        switch (opt) {
            case "callaccept":
                acceptCall();
                break;
            case "callreject":
                rejectCall();
                break;
            case "callout":
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_CALL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("tel:" + phonenum));
                startActivity(intent);
                //TODO:
                break;
        }
    }

    public void acceptCall() {
        try {
            Method method = Class.forName("android.os.ServiceManager")
                    .getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{Context.TELEPHONY_SERVICE});
            ITelephony telephony = ITelephony.Stub.asInterface(binder);
            telephony.answerRingingCall();
        } catch (Exception e) {
            Log.e(TAG, "for version 4.1 or larger");
        }
    }

    public void rejectCall() {
        try {
            Method method = Class.forName("android.os.ServiceManager")
                    .getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{Context.TELEPHONY_SERVICE});
            ITelephony telephony = ITelephony.Stub.asInterface(binder);
            telephony.endCall();
        } catch (NoSuchMethodException e) {
            Log.d(TAG, "", e);
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "", e);
        } catch (Exception e) {
        }
    }

    public void getDeviceSessions() {
        IoSession session;

        Map conMap = acceptor.getManagedSessions();
        Iterator iter = conMap.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            session = (IoSession) conMap.get(key);
            LogUtil.d(TAG, "session.isActive() = " + session.isActive() +
                    ";session.isBothIdle() = " + session.isBothIdle() +
                    ";session.isClosing() = " + session.isClosing() +
                    ";session.isConnected() = " + session.isConnected() +
                    ";session.isReaderIdle() = " + session.isReaderIdle() +
                    ";session.isReadSuspended() = " + session.isReadSuspended() +
                    ";session.isSecured() = " + session.isSecured() +
                    ";session.isWriterIdle() = " + session.isWriterIdle() +
                    ";session.isWriteSuspended() = " + session.isWriteSuspended());
            long current = System.currentTimeMillis();
            LogUtil.d(TAG, "session.getLastReadTime() = " + session.getLastReadTime());
            LogUtil.d(TAG, "session.heartbeat = " + (current - session.getLastReadTime()));

            long heartBeat = current - session.getLastReadTime();


            if (heartBeat > (long) 30000) {
                session.closeNow();
            }
            InetSocketAddress address = (InetSocketAddress) session.getRemoteAddress();
            String ip = address.getHostString();
            String deviceID = session.getAttribute(GlobalConstants.ATTR_SERIAL_NUMBER).toString();
            LogUtil.d(TAG, "id = " + session.getId() + ", ip = " + ip + ". deviceID = " + deviceID);
        }
    }

    public void setDevicesListener(final DevicesDetectListener listener) {
        devicesDetectListener = listener;
    }

    public ArrayList<DpnDeviceInfo> getDeviceInfoList() {
        return deviceList;
    }

    public void sendCallCommand(String operation, String phonenum) {
        IoSession session;

        Map conMap = acceptor.getManagedSessions();

        Iterator iter = conMap.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            session = (IoSession) conMap.get(key);
            if (session != null) {
                LogUtil.d(TAG, "session.isActive() = " + session.isActive());
                long current = System.currentTimeMillis();
                LogUtil.d(TAG, "session.getLastReadTime() = " + session.getLastReadTime());
                LogUtil.d(TAG, "session.heartbeat = " + (current - session.getLastReadTime()));

                long heartBeat = current - session.getLastReadTime();
                if (heartBeat > (long) 30000) {
                    session.closeNow();
                    continue;
                }
                InetSocketAddress address = (InetSocketAddress) session.getRemoteAddress();
                String ip = address.getHostString();
                //TODO:
                String callJson = JsonUtils.requestCallCommand(operation, phonenum);
                session.write(callJson);
                break;
            }
        }
    }

    public class ServerHandler extends IoHandlerAdapter {
        // 从端口接受消息，会响应此方法来对消息进行处理
        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            super.messageReceived(session, message);
            LogUtil.d(TAG, "messageReceived = " + message.toString());
            if (message.toString().contains("SerialNumber:")) {

                String[] result = message.toString().split(":");
                if (result != null && result[1] != null) {
                    session.setAttribute(GlobalConstants.ATTR_SERIAL_NUMBER, result[1]);

                    Message msg = Message.obtain();
                    msg.what = MSG_RECEIVE_CLIENT;
                    timeHandler.sendMessage(msg);
                }
            } else if (message.toString().contains("IsAlive")) {

                String[] str = message.toString().split("-");

                String sp = PreferenceUtil.getString(ConnectService.this, GlobalConstants.SERVER_NAME, GlobalConstants.DEFAULT_SERVER_NAME);

                if (sp != null && str[0].equals(sp)) {
                    String result = JsonUtils.requestHeartBeat(str[0]);
                    session.write(result);
                    System.out.println("I am alive...");
                } else {
                    String result = JsonUtils.requestRefuseHeartBeat();
                    session.write(result);
                    System.out.println("I am alive...refuse");
                }

                Message msg = Message.obtain();
                msg.what = MSG_RECEIVE_HEARTBEAT;
                timeHandler.sendMessage(msg);
            } else {

                LogUtil.d(TAG, "11111messageReceived 1111= " + message.toString());
                Message msg = Message.obtain();
                msg.what = MSG_RECEIVE_COMMAND;
                msg.obj = message.toString();
                timeHandler.sendMessage(msg);
            }
        }

        // 向客服端发送消息后会调用此方法
        @Override
        public void messageSent(IoSession session, Object message) throws Exception {
            super.messageSent(session, message);
            //session.close(true);//加上这句话实现短连接的效果，向客户端成功发送数据后断开连接

            LogUtil.d(TAG, "messageSent = " + message.toString());
        }

        // 服务器与客户端连接打开
        @Override
        public void sessionOpened(IoSession session) throws Exception {
            super.sessionOpened(session);
            LogUtil.d(TAG, "sessionOpened = " + session.getId());

        }

        // 关闭与客户端的连接时会调用此方法
        @Override
        public void sessionClosed(IoSession session) throws Exception {
            super.sessionClosed(session);

            LogUtil.d(TAG, "sessionClosed = " + session.getId());
            LogUtil.d(TAG, "sessionClosed deviceList.size() = " + deviceList.size());

            Message msg = Message.obtain();
            msg.what = MSG_CLOSED_CLIENT;
            timeHandler.sendMessage(msg);
        }

        // 服务器与客户端创建连接
        @Override
        public void sessionCreated(IoSession session) throws Exception {
            super.sessionCreated(session);
            String clientIP = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();

            LogUtil.d(TAG, "sessionCreated, client IP: " + clientIP);
            LogUtil.d(TAG, "sessionCreated, session.getId(): " + session.getId());

            System.out.println("服务器与客户端创建连接...");
        }


        @Override
        public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
            super.sessionIdle(session, status);
            LogUtil.d(TAG, "服务器进入空闲状态...");
        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
            super.exceptionCaught(session, cause);

            String clientIP = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();

            LogUtil.d(TAG, "exceptionCaught, client IP: " + clientIP);
            LogUtil.d(TAG, "exceptionCaught, session.getId(): " + session.getId());
            LogUtil.d(TAG, "服务器发送异常...");
            /*hasConnect = false;

            Message msg = Message.obtain();
            msg.what = MSG_EXCEPTION_CLIENT;
            timeHandler.sendMessage(msg);*/
        }
    }

    public class LocalBinder extends Binder {
        public ConnectService getService() {
            return ConnectService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy");
        if (acceptor != null) {
            acceptor.unbind();
            acceptor.dispose(true);
        }
    }
}
