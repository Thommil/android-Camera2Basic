package com.thommil.animalsgo.opencv;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.thommil.animalsgo.data.Messaging;

/**
 * OpenCV analyzer to validate a camera snaphot
 */
public class SnapshotValidator extends HandlerThread implements Handler.Callback {

    private static final String TAG = "A_GO/SnapshotValidator";
    private static final String THREAD_NAME = "SnapshotValidator";

    // Thread Handler
    private Handler mHandler;

    // Main handler
    private Handler mMainHandler;

    /**
     * Constructor
     */
    public SnapshotValidator() {
        super(THREAD_NAME);
    }

    @Override
    protected void onLooperPrepared() {
        Log.d(TAG, "onLooperPrepared()");
        //TODO error here if OpenCV is KO
        OpenCVUtils.init();
        mHandler = new Handler(getLooper(), this);
        if(mMainHandler != null){
            mMainHandler.sendMessage(mMainHandler.obtainMessage(Messaging.SYSTEM_CONNECT_VALIDATOR, mHandler));
        }
        else{
            Log.e(TAG, "Main UI handler reference must be set before start()");
            this.quit();
        }

    }

    @Override
    public boolean handleMessage(Message message) {
        Log.d(TAG, "handleMessage(" + message+ ")");
        switch (message.what){
            case Messaging.VALIDATOR_REQUEST :
                validateSnaphot((Messaging.Snapshot) message.obj);
                break;
            case Messaging.SYSTEM_SHUTDOWN:
                shutdown();
                break;
        }
        return true;
    }

    //TODO Implementation
    protected void validateSnaphot(final Messaging.Snapshot snapshot){
        Log.d(TAG, "validateSnaphot("+snapshot+")");
    }

    protected void shutdown(){
        Log.d(TAG, "shutdown()");
        quitSafely();
        // TODO free resources
    }

    public void setMainHandler(final Handler mainHandler) {
        this.mMainHandler = mainHandler;
    }

    public Handler getHandler(){
        return mHandler;
    }
}
