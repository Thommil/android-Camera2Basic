package com.thommil.animalsgo.cv;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.thommil.animalsgo.data.Capture;
import com.thommil.animalsgo.data.Messaging;

/**
 * OpenCV analyzer to validate a camera snaphot
 */
public class CaptureValidator extends HandlerThread implements Handler.Callback {

    private static final String TAG = "A_GO/CaptureValidator";
    private static final String THREAD_NAME = "CaptureValidator";

    // Machine states
    private final static int STATE_ERROR = 0x00;
    private final static int STATE_WAITING = 0x01;
    private final static int STATE_VALIDATING = 0x02;

    // Current Thread state
    private int mState = STATE_WAITING;

    // Thread Handler
    private Handler mHandler;

    // Main handler
    private Handler mMainHandler;

    private static CaptureValidator sSnapshotValidatorInstance;

    private final ImageProcessor mImageProcessor = new OpenCVProcessor();

    private CaptureValidator() {
        super(THREAD_NAME);
    }

    public static CaptureValidator getInstance(){
        if(sSnapshotValidatorInstance == null){
            sSnapshotValidatorInstance = new CaptureValidator();
        }
        return sSnapshotValidatorInstance;
    }

    @Override
    protected void onLooperPrepared() {
        //Log.d(TAG, "onLooperPrepared()");

        mHandler = new Handler(getLooper(), this);
        if (mMainHandler != null) {
            mMainHandler.sendMessage(mMainHandler.obtainMessage(Messaging.SYSTEM_CONNECT_VALIDATOR, mHandler));
        } else {
            throw new RuntimeException("Main UI handler reference must be set before start()");
        }

        mState = STATE_WAITING;
    }

    @Override
    public boolean handleMessage(Message message) {
        //Log.d(TAG, "handleMessage(" + message+ ")");
        switch (message.what){
            case Messaging.VALIDATION_REQUEST :
                final Capture capture = (Capture) message.obj;
                try {
                    if(mState == STATE_WAITING) {
                        mState = STATE_VALIDATING;
                        mImageProcessor.validateCapture(capture);
                    }
                }catch (Exception e){
                    capture.validationState = Capture.VALIDATION_FAILED;
                    //Log.d(TAG, "Validation error : "+e);
                }
                finally {
                    mMainHandler.sendMessage(mMainHandler.obtainMessage(Messaging.VALIDATION_RESULT, capture));
                    mState = STATE_WAITING;
                }
                break;
            case Messaging.SYSTEM_SHUTDOWN:
                shutdown();
                break;
        }
        return true;
    }

    private void showError(final int messageResourceId){
        mState = STATE_ERROR;
        if(mMainHandler != null) {
            mMainHandler.sendMessage(mMainHandler.obtainMessage(Messaging.SYSTEM_ERROR, messageResourceId));
        }

    }

    protected void shutdown(){
        //Log.d(TAG, "shutdown()");
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
