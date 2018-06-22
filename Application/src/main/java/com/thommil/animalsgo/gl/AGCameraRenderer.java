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

    private final Handler mainHandler;

    private final SnapshotValidator snapshotValidator;

    private final SnapshotValidator.Snapshot snapshotInstance;

    private final AGCameraFragment.CaptureData mCurrentCaptureData;

    public final static int STATE_PREVIEW = 0x00;
    public final static int STATE_START_ANALYZE = 0x01;
    public final static int STATE_ANALYZING = 0X02;
    public final static int STATE_CONFIRM_SNAPSHOT = 0X04;

    public final static int STATE_SHUTDOWN = 0X08;

    private int mState;

    public AGCameraRenderer(Context context, Surface surface, int width, int height) {
        super(context, surface, width, height);
        //Log.d(TAG, "AGCameraRenderer");
        mState = STATE_PREVIEW;
        mainHandler = new Handler(Looper.getMainLooper());
        snapshotValidator = new SnapshotValidator();
        snapshotInstance = new SnapshotValidator.Snapshot();
        mCurrentCaptureData = new AGCameraFragment.CaptureData();
        snapshotValidator.start();
    }

    @Override
    public boolean handleMessage(Message message) {
        //Log.d(TAG, "handleMessage - " + message);

        switch(message.what){
            case SnapshotValidator.ANALYZE :
                switch(mState){
                    case STATE_ANALYZING :
                        if (message.arg1 > SNAPSHOT_SCORE_THRESHOLD) {
                            mState = STATE_CONFIRM_SNAPSHOT;
                        }
                        else{
                            mState = STATE_PREVIEW;
                        }
                        break;
                }
                break;
        }
        return true;
    }

    @Override
    public void onCaptureDataReceived(final AGCameraFragment.CaptureData captureData) {
        //Log.d(TAG, "onCaptureDataReceived - "+captureData);
        //TODO add HUD state and drawing
        switch(mState){
            //Only in PREVIEW
            case STATE_PREVIEW :
                mCurrentCaptureData.lightState = captureData.lightState;
                mCurrentCaptureData.movementState = captureData.movementState;
                mCurrentCaptureData.touchState = captureData.touchState;
                mCurrentCaptureData.cameraState = captureData.cameraState;
                System.arraycopy(captureData.gravity, 0, mCurrentCaptureData.gravity, 0, 3);
                if(captureData.lightState & captureData.movementState & captureData.touchState & captureData.cameraState){
                    mState = STATE_START_ANALYZE;
                }
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // TODO Remove mock for UI events
        mState = STATE_PREVIEW;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                mCameraFragment.setPaused(false);
            }
        });
        return true;
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
        switch(mState){
            case STATE_PREVIEW :
                super.draw();
                drawHUD();
                break;
            case STATE_START_ANALYZE :
                super.draw();
                final Handler handler = snapshotValidator.getHandler();
                snapshotInstance.callBackHandler = mHandler;
                snapshotInstance.data.rewind();
                //TODO only capture square inside HUD
                GLES20.glReadPixels(0, 0, snapshotInstance.width, snapshotInstance.height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, snapshotInstance.data);
                GlUtil.checkGlError("glReadPixels");
                snapshotInstance.data.rewind();
                System.arraycopy(mCurrentCaptureData.gravity, 0, snapshotInstance.gravity, 0, 3);
                handler.sendMessage(handler.obtainMessage(SnapshotValidator.ANALYZE, snapshotInstance));
                drawHUD();
                mState = STATE_ANALYZING;
                break;
            case STATE_CONFIRM_SNAPSHOT :
                drawConfirmSnapshot();
                break;
        }
    }

    private void drawHUD(){

        //TODO HUD
    }

    private void drawConfirmSnapshot(){
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                mCameraFragment.setPaused(true);
                mCameraFragment.getSurfaceView().setOnTouchListener(AGCameraRenderer.this);
            }
        });
    }

    @Override
    public void shutdown() {
        //Log.d(TAG, "shutdown");
        snapshotValidator.shutdown();
        mState = STATE_SHUTDOWN;
        super.shutdown();
    }
}
