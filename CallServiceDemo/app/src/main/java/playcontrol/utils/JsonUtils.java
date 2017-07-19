package playcontrol.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * JsonUtils
 */
public class JsonUtils {

    /*{"controltype":"phonecall","operation":"callin","content":{"phonenum":"0","filename":"http:wwe.jjj.cn","playmode":"3D","mediatype":"video"}}*/
    public static String requestCallCommand(String operation, String phonenum) {
        String result = null;
        try {
            JSONObject jsonObjSon = new JSONObject();
            jsonObjSon.put("controltype", "callcommand");
            jsonObjSon.put("operation", operation);
            JSONObject content = new JSONObject();

            if (phonenum != null) {
                content.put("phonenum", phonenum);
            }
            jsonObjSon.putOpt("content", content);

            result = jsonObjSon.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("JsonUtils", result);
        return result;
    }

    public static String requestPlayCommand(String operation, String playUrl, int playMode, double seek) {
        String result = null;
        try {
            JSONObject jsonObjSon = new JSONObject();
            jsonObjSon.put("controltype", "playcommand");
            jsonObjSon.put("operation", operation);
            JSONObject content = new JSONObject();
            content.put("mediatype", "video");
            if (seek != -1) {
                content.put("time", seek);
            }
            if (playUrl != null) {
                content.put("filename", playUrl);
            }
            if (playMode != -1) {
                content.put("playmode", playMode);
            }

            jsonObjSon.putOpt("content", content);

            result = jsonObjSon.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("JsonUtils", result);
        return result;
    }

    public static String requestHeartBeat(String serverName) {
        String result = null;
        try {
            JSONObject jsonObjSon = new JSONObject();
            jsonObjSon.put("controltype", "heartbeat");
            jsonObjSon.put("operation", serverName);

            result = jsonObjSon.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("JsonUtils", result);
        return result;
    }

    public static String requestRefuseHeartBeat() {
        String result = null;
        try {
            JSONObject jsonObjSon = new JSONObject();
            jsonObjSon.put("controltype", "heartbeat");
            jsonObjSon.put("operation", "refuse");

            result = jsonObjSon.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("JsonUtils", result);
        return result;
    }
}
