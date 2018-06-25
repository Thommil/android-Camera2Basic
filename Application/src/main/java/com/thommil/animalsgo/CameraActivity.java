package com.thommil.animalsgo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.thommil.animalsgo.data.Messaging;
import com.thommil.animalsgo.fragments.CameraFragment;
import com.thommil.animalsgo.gl.CameraRenderer;
import com.thommil.animalsgo.opencv.SnapshotValidator;


public class CameraActivity extends FragmentActivity implements CameraRenderer.OnRendererReadyListener, Handler.Callback {

    private static final String TAG = "A_GO/CameraActivity";
    private static final String TAG_CAMERA_FRAGMENT = "tag_camera_frag";

    // Reference to the target SurfaceView
    private SurfaceView mSurfaceView;

    // Custom fragment used for encapsulating all the {@link android.hardware.camera2} apis.
    private CameraFragment mCameraFragment;

    // Custom renderer
    private CameraRenderer mRenderer;

    // Main handler
    private Handler mMainHandler;

    // Renderer handler
    private Handler mRendererHandler;

    // OpenCV handler
    private Handler mValidatorHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mSurfaceView = findViewById(R.id.surface_view);
        setupCameraFragment();
    }


    private void setupCameraFragment(){
        Log.d(TAG, "setupCameraFragment()");
        if(mCameraFragment != null && mCameraFragment.isAdded())
            return;

        mCameraFragment = new CameraFragment();
        mCameraFragment.setRetainInstance(true);
        mCameraFragment.setSurfaceView(mSurfaceView);

        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(mCameraFragment, TAG_CAMERA_FRAGMENT);
        transaction.commit();
    }



    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        getWindow().getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );

        mSurfaceView.getHolder().setKeepScreenOn(true);
        mSurfaceView.getHolder().addCallback(mSurfaceHolderCallback);
        mMainHandler = new Handler(Looper.getMainLooper(), this);
        mCameraFragment.setMainHandler(mMainHandler);

        SnapshotValidator.getInstance().setMainHandler(mMainHandler);
        SnapshotValidator.getInstance().start();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
        mSurfaceView.getHolder().removeCallback(mSurfaceHolderCallback);
        shutdownCamera();
        finish();
    }

    protected void setReady(final Surface surface, final int width, final int height) {
        Log.d(TAG, "setReady("+width+", "+height+")");
        mRenderer = new CameraRenderer(this, surface, width, height);
        mCameraFragment.setOnViewportSizeUpdatedListener(mRenderer);
        mRenderer.setCameraFragment(mCameraFragment);
        mRenderer.setOnRendererReadyListener(this);
        mRenderer.setMainHandler(mMainHandler);
        mRenderer.start();
    }


    private void shutdownCamera() {
        Log.d(TAG, "shutdownCamera()");

        if(mCameraFragment == null || mRenderer == null) return;
        if(mCameraFragment != null){
            mCameraFragment.closeCamera();
        }
        if(mRendererHandler != null){
            mRendererHandler.sendMessage(mRendererHandler.obtainMessage(Messaging.SYSTEM_SHUTDOWN));
        }
        if(mValidatorHandler != null){
            mValidatorHandler.sendMessage(mValidatorHandler.obtainMessage(Messaging.SYSTEM_SHUTDOWN));
        }
    }

    @Override
    public void onRendererReady() {
        Log.d(TAG, "onRendererReady()");
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mCameraFragment.setPreviewTexture(mRenderer.getPreviewTexture());
                mCameraFragment.openCamera();
            }
        });
    }

    @Override
    public void onRendererFinished() {
        Log.d(TAG, "onRendererFinished()");
    }

    @Override
    public boolean handleMessage(final Message message) {
        Log.d(TAG, "handleMessage(" + message+ ")");
        switch (message.what){
            case Messaging.SYSTEM_ERROR :
                ErrorDialog.newInstance((String)message.obj)
                        .show(getSupportFragmentManager(), ErrorDialog.FRAGMENT_DIALOG);
                break;
            case Messaging.SYSTEM_CONNECT_RENDERER:
                mRendererHandler = (Handler) message.obj;
                break;
            case Messaging.SYSTEM_CONNECT_VALIDATOR:
                mValidatorHandler = (Handler) message.obj;
                break;
        }
        return true;
    }

    private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            Log.d(TAG, "surfaceCreated("+surfaceHolder+")");
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
            Log.d(TAG, "surfaceChanged("+format+", "+width+", "+height+")");
            setReady(surfaceHolder.getSurface(), width, height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            Log.d(TAG, "surfaceDestroyed("+surfaceHolder+")");
        }
    };

}
