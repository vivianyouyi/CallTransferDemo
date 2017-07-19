package playcontrol;

import java.util.ArrayList;

/**
 * Created by liweiwei on 2017/6/6.
 */

public interface DevicesDetectListener {

    void devicesChanged(ArrayList<DpnDeviceInfo> deviceList);
}
