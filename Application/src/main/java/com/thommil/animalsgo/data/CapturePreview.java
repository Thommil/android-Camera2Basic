package com.thommil.animalsgo.data;

import java.util.Arrays;

/**
 * POJO class used to transit capture preview data
 */
public class CapturePreview {

    public static final int STATE_NOT_AVAILABLE = 0x00;
    public static final int STATE_NOT_READY = 0x01;
    public static final int STATE_READY = 0x02;

    public int cameraState = STATE_NOT_AVAILABLE;
    public int movementState = STATE_NOT_AVAILABLE;
    public int lightState = STATE_NOT_AVAILABLE;
    public int touchState = STATE_NOT_AVAILABLE;
    public int landscapeState = STATE_NOT_AVAILABLE;
    public float[] gravity = new float[3];

    public String toString(){
        return "[CAM:" +cameraState+", MVT:"+movementState+", LGT:"+lightState+", TCH:"+touchState+", GRV :"+ Arrays.toString(gravity)+"]";
    }
}