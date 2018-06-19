package com.thommil.animalsgo.gl;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Surface;

import com.androidexperiments.shadercam.gl.CameraRenderer;
import com.thommil.animalsgo.fragments.AGCameraFragment;

/**
 * Dedicated CameraRenderer with additional features :
 *  - HUD
 *
 */
public class AGCameraRenderer extends CameraRenderer implements AGCameraFragment.OnCaptureCompletedListener {

    private static final String TAG = "A_GO/AGCameraRenderer";

    public AGCameraRenderer(Context context, Surface surface, int width, int height) {
        super(context, surface, width, height);
    }

    @Override
    public void draw() {
        logFPS();
        super.draw();
    }

    @Override
    public void onCaptureDataReceived(AGCameraFragment.CaptureData captureData) {
        //Log.d(TAG, "onCaptureDataReceived - "+captureData);
        if(captureData.lightState && captureData.movementState && captureData.touchState
                && captureData.cameraState && captureData.facesState){
            Log.d(TAG, "GO GO GO");
        }
    }
}
