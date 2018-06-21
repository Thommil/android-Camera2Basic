package com.thommil.animalsgo.gl;

import android.content.Context;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;

import com.androidexperiments.shadercam.gl.CameraRenderer;
import com.androidexperiments.shadercam.gl.GlUtil;
import com.thommil.animalsgo.fragments.AGCameraFragment;
import com.thommil.animalsgo.opencv.SnapshotValidator;

import java.nio.ByteBuffer;

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

    private final SnapshotValidator.Snapshot snapshotInstance;
    private boolean bIsAnalyzing;

    public AGCameraRenderer(Context context, Surface surface, int width, int height) {
        super(context, surface, width, height);
        //Log.d(TAG, "AGCameraRenderer");
        bIsAnalyzing = false;
        mainHandler = new Handler(Looper.getMainLooper());
        snapshotValidator = new SnapshotValidator();
        snapshotInstance = new SnapshotValidator.Snapshot();
        snapshotValidator.start();
    }

    @Override
    public boolean handleMessage(Message message) {
        //Log.d(TAG, "handleMessage - " + message);

        switch(message.what){
            case SnapshotValidator.ANALYZE :
                if(bIsAnalyzing) {
                    bIsAnalyzing = false;
                    if (message.arg1 > SNAPSHOT_SCORE_THRESHOLD) {
                        startValidateSnapshotAnimation();
                    }
                }
                break;
        }
        return true;
    }

    @Override
    public synchronized void onCaptureDataReceived(final AGCameraFragment.CaptureData captureData) {
        ////Log.d(TAG, "onCaptureDataReceived - "+captureData);
        //TODO add HUD state and drawing
        if(!bIsAnalyzing) {
            bIsAnalyzing = captureData.lightState & captureData.movementState & captureData.touchState & captureData.cameraState & captureData.facesState;
            if (bIsAnalyzing) {
                final Handler handler = snapshotValidator.getHandler();
                snapshotInstance.callBackHandler = mHandler;
                snapshotInstance.data.rewind();
                //TODO only capture square inside HUD
                GLES20.glReadPixels(0, 0, snapshotInstance.width, snapshotInstance.height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, snapshotInstance.data);
                GlUtil.checkGlError("glReadPixels");
                snapshotInstance.data.rewind();
                snapshotInstance.gravity = captureData.gravity;
                handler.sendMessage(handler.obtainMessage(SnapshotValidator.ANALYZE, snapshotInstance));
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // TODO Remove mock for UI events
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                mCameraFragment.setPaused(false);
            }
        });
        return true;
    }

    //TODO define and implement Card creation
    protected void startValidateSnapshotAnimation(){
        //Log.d(TAG, "startValidateSnapshotAnimation");
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                mCameraFragment.setPaused(true);
                mCameraFragment.getSurfaceView().setOnTouchListener(AGCameraRenderer.this);
            }
        });
    }

    @Override
    public void onViewportSizeUpdated(Size surfaceSize, Size previewSize) {
        super.onViewportSizeUpdated(surfaceSize, previewSize);
        snapshotInstance.width = surfaceSize.getWidth();
        snapshotInstance.height = surfaceSize.getHeight();
        snapshotInstance.data = ByteBuffer.allocateDirect(snapshotInstance.width * snapshotInstance.height * 4);
    }

    @Override
    public void draw() {
        logFPS();
        super.draw();
    }

    @Override
    public void shutdown() {
        //Log.d(TAG, "shutdown");
        snapshotValidator.shutdown();
        bIsAnalyzing = false;
        super.shutdown();
    }
}
