package com.thommil.animalsgo.gl;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;

import com.androidexperiments.shadercam.gl.CameraRenderer;
import com.thommil.animalsgo.OpenCVUtils;
import com.thommil.animalsgo.fragments.AGCameraFragment;

/**
 * Dedicated CameraRenderer with additional features :
 *  - HUD
 *
 */
public class AGCameraRenderer extends CameraRenderer implements AGCameraFragment.OnCaptureCompletedListener, View.OnTouchListener {

    private static final String TAG = "A_GO/AGCameraRenderer";

    public AGCameraRenderer(Context context, Surface surface, int width, int height) {
        super(context, surface, width, height);
        OpenCVUtils.init();
    }

    @Override
    public void draw() {
        logFPS();
        super.draw();
    }

    @Override
    public void onCaptureDataReceived(final AGCameraFragment.CaptureData captureData) {
        //Log.d(TAG, "onCaptureDataReceived - "+captureData);
        if(captureData.lightState && captureData.movementState && captureData.touchState
                && captureData.cameraState && captureData.facesState){
            Log.d(TAG, "CAPTURE TRIGGERED");
            mCameraFragment.setPaused(true);
            mCameraFragment.getSurfaceView().setOnTouchListener(this);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        mCameraFragment.setPaused(false);
        return true;
    }
}
