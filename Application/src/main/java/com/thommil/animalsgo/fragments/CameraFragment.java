package com.thommil.animalsgo.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;

import com.thommil.animalsgo.R;
import com.thommil.animalsgo.data.Messaging;
import com.thommil.animalsgo.opencv.SnapshotValidator;

/**
 * Fragment for :
 *  - handling camera lifecycle
 *  - bind camera and CameraSurfaceView
 *  - handling SnapshotValidator lifecycle
 */
public class CameraFragment extends Fragment implements Handler.Callback, SurfaceHolder.Callback{

    private static final String TAG = "A_GO/CameraFragment";

    // List of states
    public static final int STATE_ERROR = 0x00;
    public static final int STATE_INIT = 0x01;
    public static final int STATE_PREVIEW = 0x02;
    public static final int STATE_VALIDATE = 0x04;
    public static final int STATE_CONFIRM = 0x08;
    public static final int STATE_SHUTDOWN = 0x0F;

    // List of messages

    // Current fragment state
    private int mState;

    // Thread Handler (main UI handler)
    private Handler mHandler;

    // Main single view
    private CameraSurfaceView mCameraSurfaceView;

    // Main single view thread
    private Thread mCameraSurfaceViewThread;

    // View thread handler
    private Handler mCameraSurfaceViewHandler;

    // Snapshot validator instance
    private SnapshotValidator mSnapshotValidator;

    // Snapshot validator handler
    private Handler mSnapshotValidatorHandler;

    // VIEW

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated()");
        mCameraSurfaceView = view.findViewById(R.id.camera_surface_view);
        // TODO Check content needs here
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated()");
        super.onActivityCreated(savedInstanceState);
        // TODO Check content needs here
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();

        mHandler = new Handler(Looper.getMainLooper(), this);

        // View
        mCameraSurfaceViewThread = new Thread(mCameraSurfaceView);
        mCameraSurfaceView.setMainHandler(mHandler);
        mCameraSurfaceViewThread.start();

        // SnapshotValidator
        mSnapshotValidator = new SnapshotValidator();
        mSnapshotValidator.setMainHandler(mHandler);
        mSnapshotValidator.start();

        if (mCameraSurfaceView.getHolder().getSurface().isValid()) {
            openCamera(mCameraSurfaceView.getWidth(), mCameraSurfaceView.getHeight());
        } else {
            mCameraSurfaceView.getHolder().addCallback(this);
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
        closeCamera();
        if(mSnapshotValidatorHandler != null){
            mSnapshotValidatorHandler.sendMessage(mSnapshotValidatorHandler.obtainMessage(Messaging.SYSTEM_SHUTDOWN));
        }
        if(mCameraSurfaceViewHandler!= null){
            mCameraSurfaceViewHandler.sendMessage(mCameraSurfaceViewHandler.obtainMessage(Messaging.SYSTEM_SHUTDOWN));
        }
    }

    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceCreated()");
        openCamera(mCameraSurfaceView.getWidth(), mCameraSurfaceView.getHeight());
    }

    @Override
    public void surfaceChanged(final SurfaceHolder surfaceHolder, final int format, final int width, final int height) {
        Log.d(TAG, "surfaceChanged("+format+","+width+","+height+")");
        //TODO set matrix ?
    }

    @Override
    public void surfaceDestroyed(final SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceDestroyed()");
    }

    // CAMERA

    protected void openCamera(final int width, final int height) {
        Log.d(TAG, "openCamera("+width+","+height+")");
    }

    protected void closeCamera(){
        Log.d(TAG, "closeCamera()");
    }

    // THREAD

    @Override
    public boolean handleMessage(final Message message) {
        Log.d(TAG, "handleMessage(" + message+ ")");
        switch (message.what){
            case Messaging.SYSTEM_ERROR :
                // TODO generic error handler
                break;
            case Messaging.SYSTEM_CONNECT_VIEW :
                mCameraSurfaceViewHandler = (Handler) message.obj;
                break;
            case Messaging.SYSTEM_CONNECT_VALIDATOR :
                mSnapshotValidatorHandler = (Handler) message.obj;
                break;
        }
        return true;
    }

    public int getState() {
        return mState;
    }

    public Handler getHandler(){
        return mHandler;
    }
}
