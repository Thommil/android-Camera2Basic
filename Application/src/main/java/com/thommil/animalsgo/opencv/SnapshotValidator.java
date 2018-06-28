package com.thommil.animalsgo.opencv;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.thommil.animalsgo.R;
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

    private static SnapshotValidator sSnapshotValidatorInstance;

    private SnapshotValidator() {
        super(THREAD_NAME);
    }

    public static SnapshotValidator getInstance(){
        if(sSnapshotValidatorInstance == null){
            sSnapshotValidatorInstance = new SnapshotValidator();
        }
        return sSnapshotValidatorInstance;
    }

    @Override
    protected void onLooperPrepared() {
        Log.d(TAG, "onLooperPrepared()");

        mHandler = new Handler(getLooper(), this);
        if (mMainHandler != null) {
            mMainHandler.sendMessage(mMainHandler.obtainMessage(Messaging.SYSTEM_CONNECT_VALIDATOR, mHandler));
        } else {
            throw new RuntimeException("Main UI handler reference must be set before start()");
        }

        OpenCVUtils.init();

        if(!OpenCVUtils.isAvailable()) {
            showError(R.string.error_opencv_init);
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        Log.d(TAG, "handleMessage(" + message+ ")");
        switch (message.what){
            case Messaging.OPENCV_REQUEST :
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

    private void showError(final int messageResourceId){
        Log.d(TAG, "showError(" + messageResourceId+ ")");
        if(mMainHandler != null) {
            mMainHandler.sendMessage(mMainHandler.obtainMessage(Messaging.SYSTEM_ERROR, messageResourceId));
        }

    }

    protected void shutdown(){
        Log.d(TAG, "shutdown()");
        quitSafely();
        sSnapshotValidatorInstance = null;
    }

    public void setMainHandler(final Handler mainHandler) {
        this.mMainHandler = mainHandler;
    }

    public Handler getHandler(){
        return mHandler;
    }
}
