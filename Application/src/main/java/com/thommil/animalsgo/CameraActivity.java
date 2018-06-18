package com.thommil.animalsgo;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.androidexperiments.shadercam.fragments.CameraFragment;
import com.androidexperiments.shadercam.fragments.PermissionsHelper;
import com.androidexperiments.shadercam.gl.CameraRenderer;
import com.androidexperiments.shadercam.utils.ShaderUtils;
import com.thommil.animalsgo.fragments.AGCameraFragment;
import com.thommil.animalsgo.gl.AGCameraRenderer;


/**
 * Written by Anthony Tripaldi
 *
 * Very basic implemention of shader camera.
 */
public class CameraActivity extends FragmentActivity implements CameraRenderer.OnRendererReadyListener, PermissionsHelper.PermissionsListener
{
    private static final String TAG = "A_GO/CameraActivity";
    private static final String TAG_CAMERA_FRAGMENT = "tag_camera_frag";

    SurfaceView mSurfaceView;

    /**
     * Custom fragment used for encapsulating all the {@link android.hardware.camera2} apis.
     */
    private AGCameraFragment mCameraFragment;

    /**
     * Our custom renderer for this example, which extends {@link CameraRenderer} and then adds custom
     * shaders, which turns shit green, which is easy.
     */
    private AGCameraRenderer mRenderer;

    /**
     * boolean for triggering restart of camera after completed rendering
     */
    private boolean mRestartCamera = false;

    private PermissionsHelper mPermissionsHelper;
    private boolean mPermissionsSatisfied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mSurfaceView = findViewById(R.id.surface_view);
        setupCameraFragment();

        //setup permissions for M or start normally
        if(PermissionsHelper.isMorHigher())
            setupPermissions();
    }

    private void setupPermissions() {
        Log.d(TAG, "setupPermissions");
        mPermissionsHelper = PermissionsHelper.attach(this);
        mPermissionsHelper.setRequestedPermissions(
                Manifest.permission.CAMERA
        );
    }

    /**
     * create the camera fragment responsible for handling camera state and add it to our activity
     */
    private void setupCameraFragment()
    {
        Log.d(TAG, "setupCameraFragment");
        if(mCameraFragment != null && mCameraFragment.isAdded())
            return;

        mCameraFragment = new AGCameraFragment();
        mCameraFragment.setRetainInstance(true);
        mCameraFragment.setCameraToUse(CameraFragment.CAMERA_PRIMARY); //pick which camera u want to use, we default to forward
        mCameraFragment.setSurfaceView(mSurfaceView);

        //add fragment to our setup and let it work its magic
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(mCameraFragment, TAG_CAMERA_FRAGMENT);
        transaction.commit();
    }

    /**
     * Things are good to go and we can continue on as normal. If this is called after a user
     * sees a dialog, then onResume will be called next, allowing the app to continue as normal.
     */
    @Override
    public void onPermissionsSatisfied() {
        Log.d(TAG, "onPermissionsSatisfied");
        mPermissionsSatisfied = true;
    }

    /**
     * User did not grant the permissions needed for out app, so we show a quick toast and kill the
     * activity before it can continue onward.
     * @param failedPermissions string array of which permissions were denied
     */
    @Override
    public void onPermissionsFailed(String[] failedPermissions) {
        Log.d(TAG, "onPermissionsFailed");
        mPermissionsSatisfied = false;
        Toast.makeText(this, "Animal-GO needs all permissions to function, please try again.", Toast.LENGTH_LONG).show();
        this.finish();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        if(PermissionsHelper.isMorHigher() && !mPermissionsSatisfied) {
            if (!mPermissionsHelper.checkPermissions())
                return;
            else
                mPermissionsSatisfied = true; //extra helper as callback sometimes isnt quick enough for future results
        }

        ShaderUtils.goFullscreen(this.getWindow());

        mSurfaceView.getHolder().addCallback(mSurfaceHolderCallback);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();

        shutdownCamera(false);
        mSurfaceView.getHolder().removeCallback(mSurfaceHolderCallback);

        if(!((PowerManager) getSystemService(Context.POWER_SERVICE)).isInteractive()){
            finish();
        }
    }

    /**
     * called whenever surface texture becomes initially available or whenever a camera restarts after
     * completed recording or resuming from onpause
     * @param surface {@link Surface} that we'll be drawing into
     * @param width width of the surface texture
     * @param height height of the surface texture
     */
    protected void setReady(Surface surface, int width, int height) {
        Log.d(TAG, "setReady - "+width+", "+height);
        mRenderer = new AGCameraRenderer(this, surface, width, height);
        mCameraFragment.setOnViewportSizeUpdatedListener(mRenderer);
        mCameraFragment.setOnCaptureCompletedListener(mRenderer);
        mRenderer.setCameraFragment(mCameraFragment);
        mRenderer.setOnRendererReadyListener(this);
        mRenderer.start();
    }


    /**
     * kills the camera in camera fragment and shutsdown render thread
     * @param restart whether or not to restart the camera after shutdown is complete
     */
    private void shutdownCamera(boolean restart)
    {
        Log.d(TAG, "shutdownCamera - "+restart);
        //make sure we're here in a working state with proper permissions when we kill the camera
        if(PermissionsHelper.isMorHigher() && !mPermissionsSatisfied) return;

        //check to make sure we've even created the cam and renderer yet
        if(mCameraFragment == null || mRenderer == null) return;

        mCameraFragment.closeCamera();

        mRestartCamera = restart;
        mRenderer.getRenderHandler().sendShutdown();
        mRenderer = null;
    }

    /**
     * Interface overrides from our {@link com.androidexperiments.shadercam.gl.CameraRenderer.OnRendererReadyListener}
     * interface. Since these are being called from inside the CameraRenderer thread, we need to make sure
     * that we call our methods from the {@link #runOnUiThread(Runnable)} method, so that we don't
     * throw any exceptions about touching the UI from non-UI threads.
     *
     * Another way to handle this would be to create a Handler/Message system similar to how our
     * {@link com.androidexperiments.shadercam.gl.CameraRenderer.RenderHandler} works.
     */
    @Override
    public void onRendererReady() {
        Log.d(TAG, "onRendererReady");
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
        Log.d(TAG, "onRendererFinished");
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


    private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            Log.d(TAG, "surfaceCreated");
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
            Log.d(TAG, "surfaceChanged - "+format+", "+width+", "+height);
            setReady(surfaceHolder.getSurface(), width, height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            Log.d(TAG, "surfaceDestroyed");
        }
    };

}
