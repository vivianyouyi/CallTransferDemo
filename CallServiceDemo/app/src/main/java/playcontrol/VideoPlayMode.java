package playcontrol;

/**
 * Created by liweiwei on 2017/6/6.
 */

public class VideoPlayMode {

    public static final int VIDEO_PLAY_MODE_2D = 0x0;

    public static final int VIDEO_PLAY_MODE_3D_LR = 0x1;
    public static final int VIDEO_PLAY_MODE_3D_RL = 0x2;
    public static final int VIDEO_PLAY_MODE_3D_TB = 0x4;
    public static final int VIDEO_PLAY_MODE_3D_BT = 0x8;

    public static final int VIDEO_PLAY_MODE_360 = 0x10;
    public static final int VIDEO_PLAY_MODE_360_LR = VIDEO_PLAY_MODE_360 | VIDEO_PLAY_MODE_3D_LR;
    public static final int VIDEO_PLAY_MODE_360_RL = VIDEO_PLAY_MODE_360 | VIDEO_PLAY_MODE_3D_RL;
    public static final int VIDEO_PLAY_MODE_360_TB = VIDEO_PLAY_MODE_360 | VIDEO_PLAY_MODE_3D_TB;
    public static final int VIDEO_PLAY_MODE_360_BT = VIDEO_PLAY_MODE_360 | VIDEO_PLAY_MODE_3D_BT;
    public static final int VIDEO_PLAY_MODE_180 = 0x100;
    public static final int VIDEO_PLAY_MODE_180_LR = VIDEO_PLAY_MODE_180 | VIDEO_PLAY_MODE_3D_LR;
    public static final int VIDEO_PLAY_MODE_180_RL = VIDEO_PLAY_MODE_180 | VIDEO_PLAY_MODE_3D_RL;
    public static final int VIDEO_PLAY_MODE_180_TB = VIDEO_PLAY_MODE_180 | VIDEO_PLAY_MODE_3D_TB;
    public static final int VIDEO_PLAY_MODE_180_BT = VIDEO_PLAY_MODE_180 | VIDEO_PLAY_MODE_3D_BT;

}
