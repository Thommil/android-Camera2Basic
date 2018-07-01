package com.thommil.animalsgo.data;

import org.opencv.core.Size;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * POJO class used to transit capture preview data
 */
public class Capture {

    public static final int STATE_NOT_AVAILABLE = 0x00;
    public static final int STATE_NOT_READY = 0x01;
    public static final int STATE_READY = 0x02;

    public static final int VALIDATION_FAILED = 0x00;
    public static final int VALIDATION_IN_PROGRESS = 0x01;
    public static final int VALIDATION_SUCCEED = 0x02;

    public int cameraState = STATE_NOT_AVAILABLE;
    public int lightState = STATE_NOT_AVAILABLE;
    public float[] gravity = new float[3];

    public int width;
    public int height;
    public ByteBuffer mOriginalBuffer;
    public ByteBuffer mShadedBuffer;

    public int validationState = VALIDATION_IN_PROGRESS;

    public String toString(){
        return "[Size: "+width+"x"+height+"][CAM:" +cameraState+", LGT:"+lightState+", GRV :"+ Arrays.toString(gravity)+"][Validation : "+validationState+"]";
    }
}