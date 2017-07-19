package playcontrol;

/**
 * Created by liweiwei on 2017/5/27.
 */

public class DpnDeviceInfo {
    public String deviceName = "";
    public String deviceAddress = "";
    public String serialNumber = "";

    @Override
    public String toString() {
        return "DpnDeviceInfo{" +
                "deviceName='" + deviceName + '\'' +
                ", deviceAddress='" + deviceAddress + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                '}';
    }
}
