package com.thommil.animalsgo.fragments;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.support.v4.util.Pools;
import android.view.MotionEvent;
import android.view.View;

import com.thommil.animalsgo.data.CapturePreview;
import com.thommil.animalsgo.data.Settings;

/**
 * Decicated CameraCaptureSession.CaptureCallback used for QoS and event dispatch to Renderer
 *
 */
public class CapturePreviewBuilder implements View.OnTouchListener, SensorEventListener {

    private static final int POOL_SIZE = 10;
    private static final Pools.SimplePool<CapturePreview> sCapturePreviewPool = new Pools.SimplePool<>(POOL_SIZE );

    static{
        for(int i =0; i < POOL_SIZE; i++){
            sCapturePreviewPool.release(new CapturePreview());
        }
    }

    private boolean mIsTouched = false;
    private boolean mIsmoving = false;
    final private float[] mGravity = new float[3];
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    private final static CapturePreviewBuilder sCapturePreviewBuilder = new CapturePreviewBuilder();

    private CapturePreviewBuilder() {
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }

    public static CapturePreviewBuilder getInstance(){
        return sCapturePreviewBuilder;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        System.arraycopy(sensorEvent.values, 0, mGravity, 0, 3); ;
        final float x = mGravity[0];
        final float y = mGravity[1];
        final float z = mGravity[2];
        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float)Math.sqrt(x*x + y*y + z*z);
        final float delta = Math.abs(mAccelCurrent - mAccelLast);
        mAccel = mAccel * 0.9f + delta;
        mIsmoving = (mAccel > Settings.MOVEMENT_THRESHOLD);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //PASS
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        mIsTouched = !(motionEvent.getActionMasked() == MotionEvent.ACTION_UP && motionEvent.getPointerCount() == 1);
        return true;
    }

    public CapturePreview buildCapturePreview(final TotalCaptureResult result) {
        //Listener
        final CapturePreview capturePreview = sCapturePreviewPool.acquire();

        //Camera state
        final Integer afValue = result.get(CaptureResult.CONTROL_AF_STATE);
        if (afValue != null) {
            switch (afValue) {
                case CaptureResult.CONTROL_AF_STATE_INACTIVE:
                case CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED:
                case CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED:
                    final Integer aeValue = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeValue != null) {
                        switch (aeValue) {
                            case CaptureResult.CONTROL_AE_STATE_INACTIVE:
                            case CaptureResult.CONTROL_AE_STATE_LOCKED:
                            case CaptureResult.CONTROL_AE_STATE_CONVERGED:
                                capturePreview.cameraState = CapturePreview.STATE_READY;
                                capturePreview.lightState = CapturePreview.STATE_READY;
                                break;
                            case CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED:
                                capturePreview.cameraState = CapturePreview.STATE_NOT_READY;
                                capturePreview.lightState = CapturePreview.STATE_NOT_READY;
                                break;
                            default:
                                capturePreview.cameraState = CapturePreview.STATE_READY;
                                capturePreview.lightState = CapturePreview.STATE_NOT_AVAILABLE;
                        }
                    } else {
                        capturePreview.cameraState = CapturePreview.STATE_READY;
                        capturePreview.lightState = CapturePreview.STATE_NOT_AVAILABLE;
                    }

                    if (capturePreview.cameraState == CapturePreview.STATE_READY) {
                        final Integer awbValue = result.get(CaptureResult.CONTROL_AWB_STATE);
                        if (awbValue != null) {
                            switch (awbValue) {
                                case CaptureResult.CONTROL_AWB_STATE_INACTIVE:
                                case CaptureResult.CONTROL_AWB_STATE_LOCKED:
                                case CaptureResult.CONTROL_AWB_STATE_CONVERGED:
                                    capturePreview.cameraState = CapturePreview.STATE_READY;
                                    break;
                                default:
                                    capturePreview.cameraState = CapturePreview.STATE_NOT_READY;
                            }
                        } else {
                            capturePreview.cameraState = CapturePreview.STATE_READY;
                        }
                    }

                    if (capturePreview.cameraState == CapturePreview.STATE_READY) {
                        final Integer lensValue = result.get(CaptureResult.LENS_STATE);
                        if (lensValue != null) {
                            switch (lensValue) {
                                case CaptureResult.LENS_STATE_STATIONARY:
                                    capturePreview.cameraState = CapturePreview.STATE_READY;
                                    break;
                                default:
                                    capturePreview.cameraState = CapturePreview.STATE_NOT_READY;
                            }
                        } else {
                            capturePreview.cameraState = CapturePreview.STATE_READY;
                        }
                    }

                    break;
                default:
                    capturePreview.cameraState = CapturePreview.STATE_NOT_READY;
            }
        } else {
            capturePreview.cameraState = CapturePreview.STATE_NOT_AVAILABLE;
        }

        //Movement
        capturePreview.movementState = mIsmoving ? CapturePreview.STATE_NOT_READY : CapturePreview.STATE_READY;

        //Touch
        capturePreview.touchState = mIsTouched ? CapturePreview.STATE_NOT_READY : CapturePreview.STATE_READY;

        //Gravity
        System.arraycopy(mGravity, 0, capturePreview.gravity, 0, 3); ;

        return capturePreview;
    }

    public void releaseCapturePreview(final CapturePreview capturePreview){
        sCapturePreviewPool.release(capturePreview);
    }
}
