package com.thommil.animalsgo.fragments;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

import com.thommil.animalsgo.data.Messaging;
import com.thommil.animalsgo.data.Settings;

/**
 * SurfaceView implementation to display :
 *  - camera preview
 *  - camera HUD
 *  - snapshot validation screen
 */
public class CameraSurfaceView extends SurfaceView implements Runnable, Handler.Callback {

    private static final String TAG = "A_GO/CameraSurfaceView";

    //Preview Settings based on prefs_camera_preview_quality_entries
    public static final String PREVIEW_QUALITY_LOW = "low";
    public static final String PREVIEW_QUALITY_MEDIUM = "medium";
    public static final String PREVIEW_QUALITY_HIGH = "high";

    // HandlerThread bound to the view processing
    private HandlerThread mHandlerThread;

    // Handler of view thread
    private Handler mHandler;

    // Main handler
    private Handler mMainHandler;

    // List of messages

    public CameraSurfaceView(Context context) {
        super(context);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void run() {
        Log.d(TAG, "run()");
        Looper.prepare();

        mHandler = new Handler(Looper.myLooper(), this);

        if(mMainHandler != null){
            mMainHandler.sendMessage(mMainHandler.obtainMessage(Messaging.SYSTEM_CONNECT_VIEW, mHandler));
            Looper.loop();
        }
        else{
            Log.e(TAG, "Main UI handler reference must be set before start()");
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        Log.d(TAG, "handleMessage(" + message+ ")");
        switch (message.what){
            case Messaging.SYSTEM_SHUTDOWN:
                Log.d(TAG, "" + Settings.getInstance().getString(Settings.CAMERA_PREVIEW_QUALITY));
                shutdown();
                break;
        }
        return true;
    }

    protected void shutdown(){
        Log.d(TAG, "shutdown()");
        mHandler.getLooper().quitSafely();
        // TODO free resources
    }

    public void setMainHandler(final Handler mainHandler) {
        this.mMainHandler = mainHandler;
    }

    public Handler getHandler(){
        return mHandler;
    }
}
