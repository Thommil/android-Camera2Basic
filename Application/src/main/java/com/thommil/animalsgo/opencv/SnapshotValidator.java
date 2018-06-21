package com.thommil.animalsgo.opencv;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.Pools;
import android.util.Log;

import com.thommil.animalsgo.fragments.AGCameraFragment;

import java.nio.ByteBuffer;

/**
 * OpenCV analyzer to validate a camera snaphot
 */
public class SnapshotValidator extends HandlerThread implements Handler.Callback {

    private static final String TAG = "A_GO/SnapshotValidator";
    private static final String THREAD_NAME = "SnapshotValidator";

    public static final int ANALYZE = 1;

    private Handler mHandler;

    /**
     * Constructor
     */
    public SnapshotValidator() {
        super(THREAD_NAME);
        //Log.d(TAG, "SnapshotValidator");
    }

    @Override
    protected void onLooperPrepared() {
        //Log.d(TAG, "onLooperPrepared");
        //TODO error here if OpenCV is KO
        OpenCVUtils.init();
        mHandler = new Handler(getLooper(), this);
    }

    @Override
    public boolean handleMessage(Message message) {
        //Log.d(TAG, "handleMessage - " + message);
        final Snapshot snapshot = (Snapshot) message.obj;
        validateSnaphot(snapshot);
        return true;
    }

    //TODO Implementation
    protected void validateSnaphot(final Snapshot snapshot){
        //Log.d(TAG, "validateSnaphot");
        snapshot.callBackHandler.sendMessage(snapshot.callBackHandler.obtainMessage(ANALYZE, 100, 100));
    }

    public Handler getHandler(){
        return mHandler;
    }

    public void shutdown(){
        //Log.d(TAG, "shutdown");
        mHandler.getLooper().quit();
    }

    /**
     * Message payload for Snapshot request analysis
     */
    public static class Snapshot {
         public Handler callBackHandler;
         public int width;
         public int height;
         public final float[] gravity = new float[3];
         public ByteBuffer data;
    }
}
