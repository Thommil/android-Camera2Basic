package com.thommil.animalsgo.data;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * POJO class used to transit capture preview data
 */
public class Capture {

    public static final int STATE_NOT_AVAILABLE = 0x00;
    public static final int STATE_NOT_READY = 0x01;
    public static final int STATE_READY = 0x02;

    public static final int VALIDATION_WAIT = 0x00;
    public static final int VALIDATION_IN_PROGRESS = 0x01;
    public static final int VALIDATION_SUCCEED = 0x02;
    public static final int VALIDATION_FAILED = 0x04;

    //Camera
    public int cameraState = STATE_NOT_AVAILABLE;
    public int lightState = STATE_NOT_AVAILABLE;
    public int faceState = STATE_NOT_AVAILABLE;

    //Sensors
    public float[] gravity = new float[3];
    public float movement = 1f;

    //Data
    public String pluginId;
    public int width;
    public int height;
    public ByteBuffer mCameraBuffer;

    //State
    public int validationState = VALIDATION_WAIT;

    public String toString(){
        return "[State : "+validationState+"][Camera - CAM:" +cameraState+", LGT:"+lightState+", FCE:"+faceState+"]"
                    + "[Sensors - GRAV : "+Arrays.toString(gravity)+", MVT: "+movement+"]"
                    + "[Data - PLUG: "+pluginId+", SIZE: "+width+"x"+height+", MEM : "+mCameraBuffer+"]";
    }
}