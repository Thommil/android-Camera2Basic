package com.thommil.animalsgo.gl;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;

import com.androidexperiments.shadercam.gl.CameraRenderer;
import com.thommil.animalsgo.fragments.AGCameraFragment;
import com.thommil.animalsgo.opencv.SnapshotValidator;

/**
 * Dedicated CameraRenderer with additional features :
 *  - HUD
 *
 */
public class AGCameraRenderer extends CameraRenderer implements AGCameraFragment.OnCaptureCompletedListener, View.OnTouchListener {

    private static final String TAG = "A_GO/AGCameraRenderer";

    private static final int SNAPSHOT_SCORE_THRESHOLD = 70;

    private Handler mainHandler;

    private SnapshotValidator snapshotValidator;

    public AGCameraRenderer(Context context, Surface surface, int width, int height) {
        super(context, surface, width, height);
        Log.d(TAG, "AGCameraRenderer");
        mainHandler = new Handler(Looper.getMainLooper());
        snapshotValidator = new SnapshotValidator();
        snapshotValidator.start();
    }

    @Override
    public boolean handleMessage(Message message) {
        Log.d(TAG, "handleMessage - " + message);

        switch(message.what){
            case SnapshotValidator.ANALYZE :
                if(message.arg1 > SNAPSHOT_SCORE_THRESHOLD){
                    startValidateSnapshotAnimation();
                }
                break;
        }
        return true;
    }

    @Override
    public void onCaptureDataReceived(final AGCameraFragment.CaptureData captureData) {
        //Log.d(TAG, "onCaptureDataReceived - "+captureData);
        if(captureData.lightState && captureData.movementState && captureData.touchState
                && captureData.cameraState && captureData.facesState){
            Log.d(TAG, "CAPTURE TRIGGERED");

            final Handler handler = snapshotValidator.getHandler();
            final SnapshotValidator.Snapshot snapshot = snapshotValidator.getSnapShotInstance();
            snapshot.callBackHandler = mHandler;
            snapshot.data = 1;

            handler.sendMessage(handler.obtainMessage(SnapshotValidator.ANALYZE, snapshot));
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // TODO MOCK HERE
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                mCameraFragment.setPaused(false);
            }
        });
        return true;
    }

    //TODO
    protected void startValidateSnapshotAnimation(){
        Log.d(TAG, "startValidateSnapshotAnimation");
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                mCameraFragment.setPaused(true);
                mCameraFragment.getSurfaceView().setOnTouchListener(AGCameraRenderer.this);
            }
        });
    }

    @Override
    public void draw() {
        logFPS();
        super.draw();
    }

    @Override
    public void shutdown() {
        Log.d(TAG, "shutdown");
        snapshotValidator.shutdown();
        super.shutdown();
    }
}
