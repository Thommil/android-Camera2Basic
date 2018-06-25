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

import com.thommil.animalsgo.data.Messaging;
import com.thommil.animalsgo.fragments.CameraFragment;
import com.thommil.animalsgo.gl.CameraRenderer;
import com.thommil.animalsgo.gl.ShaderUtils;


/**
 * Written by Anthony Tripaldi
 *
 * Very basic implemention of shader camera.
 *
 * // TODO handle error in global way
 * // TODO display permission on cam start only
 */
public class CameraActivity extends FragmentActivity implements CameraRenderer.OnRendererReadyListener, Handler.Callback
{
    private static final String TAG = "A_GO/CameraActivity";
    private static final String TAG_CAMERA_FRAGMENT = "tag_camera_frag";

    SurfaceView mSurfaceView;

    /**
     * Custom fragment used for encapsulating all the {@link android.hardware.camera2} apis.
     */
    private CameraFragment mCameraFragment;

    /**
     * Our custom renderer for this example, which extends {@link CameraRenderer} and then adds custom
     * shaders, which turns shit green, which is easy.
     */
    private CameraRenderer mRenderer;

    // Main handler
    private Handler mMainHandler;

    /**
     * boolean for triggering restart of camera after completed rendering
     */
    private boolean mRestartCamera = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mSurfaceView = findViewById(R.id.surface_view);
        setupCameraFragment();
    }


    /**
     * create the camera fragment responsible for handling camera state and add it to our activity
     */
    private void setupCameraFragment()
    {
        //Log.d(TAG, "setupCameraFragment");
        if(mCameraFragment != null && mCameraFragment.isAdded())
            return;

        mCameraFragment = new CameraFragment();
        mCameraFragment.setRetainInstance(true);
        mCameraFragment.setCameraToUse(CameraFragment.CAMERA_PRIMARY); //pick which camera u want to use, we default to forward
        mCameraFragment.setSurfaceView(mSurfaceView);

        //add fragment to our setup and let it work its magic
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(mCameraFragment, TAG_CAMERA_FRAGMENT);
        transaction.commit();
    }



    @Override
    protected void onResume() {
        //Log.d(TAG, "onResume");
        super.onResume();
        ShaderUtils.goFullscreen(this.getWindow());
        mSurfaceView.getHolder().addCallback(mSurfaceHolderCallback);
        mMainHandler = new Handler(Looper.getMainLooper(), this);
    }

    @Override
    protected void onPause() {
        //Log.d(TAG, "onPause");
        super.onPause();
        mSurfaceView.getHolder().removeCallback(mSurfaceHolderCallback);
        shutdownCamera(false);
        finish();
    }

    /**
     * called whenever surface texture becomes initially available or whenever a camera restarts after
     * completed recording or resuming from onpause
     * @param surface {@link Surface} that we'll be drawing into
     * @param width width of the surface texture
     * @param height height of the surface texture
     */
    protected void setReady(Surface surface, int width, int height) {
        //Log.d(TAG, "setReady - "+width+", "+height);
        mRenderer = new CameraRenderer(this, surface, width, height);
        mCameraFragment.setOnViewportSizeUpdatedListener(mRenderer);
        mRenderer.setCameraFragment(mCameraFragment);
        mRenderer.setOnRendererReadyListener(this);
        mRenderer.setMainHandler(mMainHandler);
        mRenderer.start();
    }


    /**
     * kills the camera in camera fragment and shutsdown render thread
     * @param restart whether or not to restart the camera after shutdown is complete
     */
    private void shutdownCamera(boolean restart)
    {
        //Log.d(TAG, "shutdownCamera - "+restart);

        //check to make sure we've even created the cam and renderer yet
        if(mCameraFragment == null || mRenderer == null) return;

        mCameraFragment.closeCamera();

        mRestartCamera = restart;
        mRenderer.shutdown();
        mRenderer = null;
    }

    /**
     * Since these are being called from inside the CameraRenderer thread, we need to make sure
     * that we call our methods from the {@link #runOnUiThread(Runnable)} method, so that we don't
     * throw any exceptions about touching the UI from non-UI threads.
     *
     */
    @Override
    public void onRendererReady() {
        //Log.d(TAG, "onRendererReady");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCameraFragment.setPreviewTexture(mRenderer.getPreviewTexture());
                mCameraFragment.openCamera();
            }
        });
    }

    @Override
    public void onRendererFinished() {
        //Log.d(TAG, "onRendererFinished");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRestartCamera) {
                    setReady(mSurfaceView.getHolder().getSurface(), mSurfaceView.getWidth(), mSurfaceView.getHeight());
                    mRestartCamera = false;
                }
            }
        });
    }

    @Override
    public boolean handleMessage(Message message) {
        //Log.d(TAG, "handleMessage(" + message+ ")");
        switch (message.what){
            case Messaging.SYSTEM_ERROR :
                ErrorDialog.newInstance((String)message.obj)
                        .show(getSupportFragmentManager(), ErrorDialog.FRAGMENT_DIALOG);
                break;
        }
        return true;
    }

    private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            //Log.d(TAG, "surfaceCreated");
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
            //Log.d(TAG, "surfaceChanged - "+format+", "+width+", "+height);
            setReady(surfaceHolder.getSurface(), width, height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            //Log.d(TAG, "surfaceDestroyed");
        }
    };

}
