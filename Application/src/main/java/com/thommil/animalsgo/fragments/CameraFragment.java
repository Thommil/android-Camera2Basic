package com.thommil.animalsgo.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.thommil.animalsgo.R;
import com.thommil.animalsgo.data.CapturePreview;
import com.thommil.animalsgo.data.Messaging;
import com.thommil.animalsgo.data.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Fragment for operating the camera, it doesnt have any UI elements, just controllers
 */
public class CameraFragment extends Fragment {

    private static final String TAG = "A_GO/CameraFragment";

    // A SurfaceView for camera preview.
    private SurfaceView mSurfaceView;

    // A reference to the opened CameraDevice.
    private CameraDevice mCameraDevice;

    // A reference to the current CameraCaptureSession for preview.
    private CameraCaptureSession mPreviewSession;

    // Surface to render preview of camera
    private SurfaceTexture mPreviewSurface;

    //The Size of camera preview.
    private Size mPreviewSize;

    // Camera preview.
    private CaptureRequest.Builder mPreviewBuilder;

    // A Handler for running tasks in the renderer thread
    private Handler mRendererHandler;

    // Main handler
    private Handler mMainHandler;

    // Listener for when openCamera is called and a proper video size is created
    private OnViewportSizeUpdatedListener mOnViewportSizeUpdatedListener;

    // Current aspect ratio of preview
    private float mPreviewSurfaceAspectRatio;

    // A Semaphore to prevent the app from exiting before closing the camera.
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    // Indicates if camera is open
    private boolean mCameraIsOpen = false;

    // Indicates if preview is running
    private boolean mIsPaused = false;

    // Reference to the SensorManager
    private SensorManager mSensorManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
    }


    public void openCamera()
    {
        Log.d(TAG, "openCamera()");
        final Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }

        //sometimes openCamera gets called multiple times, so lets not get stuck in our semaphore lock
        if(mCameraDevice != null && mCameraIsOpen)
            return;

        final CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                showError(R.string.error_camera_timeout);
            }

            String cameraId = null;
            final String[] cameraList = manager.getCameraIdList();

            for (final String id : cameraList) {
                final CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);

                final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id;
                    break;
                }
            }

            if(cameraId == null){
                showError(R.string.error_camera_not_found);
                mCameraOpenCloseLock.release();
                return;
            }

            final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            final StreamConfigurationMap streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            mPreviewSize = choosePreviewSize(streamConfigurationMap.getOutputSizes(SurfaceHolder.class));

            if(mOnViewportSizeUpdatedListener != null) {
                mOnViewportSizeUpdatedListener.onViewportSizeUpdated(new Size(mSurfaceView.getWidth(), mSurfaceView.getHeight()),mPreviewSize);
            }

            manager.openCamera(cameraId, mStateCallback, mMainHandler);
        }
        catch (CameraAccessException e) {
            mCameraOpenCloseLock.release();
            showError(R.string.error_camera_generic);
        }
        catch (NullPointerException e){
            mCameraOpenCloseLock.release();
            showError(R.string.error_camera_generic);
        }
        catch (InterruptedException e) {
            mCameraOpenCloseLock.release();
            showError(R.string.error_camera_generic);
        }
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            Log.d(TAG, "onOpened("+cameraDevice+")");
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            mCameraIsOpen = true;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            Log.d(TAG, "onDisconnected("+cameraDevice+")");
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            mCameraIsOpen = false;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            Log.d(TAG, "onError("+cameraDevice+", "+error+")");
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            mCameraIsOpen = false;
            showError(R.string.error_camera_generic);
        }
    };

    // TODO Find fix for android < 6 & allow quality choice
    private Size choosePreviewSize(Size[] choices)
    {
        Log.d(TAG, "chooseVideoSize("+Arrays.toString(choices)+")");
        final int sw = mSurfaceView.getWidth(); //surface width
        final int sh = mSurfaceView.getHeight(); //surface height

        Log.d(TAG, "Surface size : "+sw+"x"+sh);

        mPreviewSurfaceAspectRatio = (float)sw / sh;

        Log.d(TAG, "chooseVideoSize() for landscape:" + (mPreviewSurfaceAspectRatio > 1.f) + " aspect: " + mPreviewSurfaceAspectRatio);

        //rather than in-lining returns, use this size as placeholder so we can calc aspectratio upon completion
        Size sizeToReturn = null;

        //video is 'landscape' if over 1.f
        if(mPreviewSurfaceAspectRatio > 1.f) {
            for (Size size : choices) {
                if (size.getHeight() == size.getWidth() * 9 / 16 && size.getHeight() <= 1080) {
                    sizeToReturn = size;
                }
            }

            //final check
            if(sizeToReturn == null)
                sizeToReturn = choices[0];

        }
        else //portrait or square
        {
            /**
             * find a potential aspect ratio match so that our video on screen is the same
             * as what we record out - what u see is what u get
             */
            ArrayList<Size> potentials = new ArrayList<>();
            for (Size size : choices)
            {
                // height / width because we're portrait
                float aspect = (float)size.getHeight() / size.getWidth();
                if(aspect == mPreviewSurfaceAspectRatio)
                    potentials.add(size);
            }
            Log.i(TAG, "---potentials: " + potentials.size() + " : " + potentials);

            if(potentials.size() > 0)
            {
                //check for potential perfect matches (usually full screen surfaces)
                for(Size potential : potentials)
                    if(potential.getHeight() == sw) {
                        Log.d(TAG, "potential : " + potential);
                        sizeToReturn = potential;
                        break;
                    }
                if(sizeToReturn == null) {
                    Log.i(TAG, "---no perfect match, check for 'normal'");


                    //if that fails - check for largest 'normal size' video
                    for (Size potential : potentials) {
                        if (potential.getHeight() == 1080 || potential.getHeight() == 720) {
                            sizeToReturn = potential;
                            break;
                        }
                    }

                    if (sizeToReturn == null) {
                        Log.i(TAG, "---no 'normal' match, return largest ");
                        sizeToReturn = potentials.get(0);
                    }
                }
            }

            //final check
            if(sizeToReturn == null)
                sizeToReturn = choices[0];

        }

        Log.i(TAG, "Final choice : " + sizeToReturn);

        return sizeToReturn;
    }

    public void closeCamera() {
        Log.d(TAG, "closeCamera()");
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
                mCameraIsOpen = false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mCameraOpenCloseLock.release();
        }
    }


    private final CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        // Current frame count
        private int mFrameCount = 0;

        // Reference to the CapturePreviewBuilder instance
        private final CapturePreviewBuilder mCapturePreviewBuilder = CapturePreviewBuilder.getInstance();

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            if(mFrameCount > Settings.CAPTURE_UPDATE_FREQUENCY){
                final CapturePreview capturePreview = mCapturePreviewBuilder.buildCapturePreview(result);
                //TODO get data from image reader and send to snapshotvalidator (set STATE_VALIDATE)
                //Log.d(TAG, capturePreview.toString());
                //stopPreview();
                mCapturePreviewBuilder.releaseCapturePreview(capturePreview);
                mFrameCount = 0;
            }
            mFrameCount++;
        }

    };

    protected void startPreview(){
        Log.d(TAG, "startPreview()");
        if (null == mCameraDevice || null == mPreviewSize || !mSurfaceView.getHolder().getSurface().isValid()) {
            return;
        }
        try {
            mPreviewSurface.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            List<Surface> surfaces = new ArrayList<>();

            assert mPreviewSurface != null;
            Surface previewSurface = new Surface(mPreviewSurface);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            // Settings
            mPreviewBuilder.set(CaptureRequest.CONTROL_CAPTURE_INTENT, CaptureRequest.CONTROL_CAPTURE_INTENT_VIDEO_SNAPSHOT); // High quality video
            mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF); // No Flash (don't bother animals)
            mPreviewBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF); // Faces using OpenCV outside
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_OFF); // Outside purpose

            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Activity activity = getActivity();
                    Log.e(TAG, "config failed: " + cameraCaptureSession);
                    if (null != activity) {
                        Toast.makeText(activity, "CaptureSession Config Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }, mRendererHandler);
        }
        catch (CameraAccessException e) {
            showError(R.string.error_camera_generic);
        }
    }

    protected void updatePreview() {
        Log.d(TAG, "updatePreview()");
        if (null == mCameraDevice) {
            return;
        }
        try {
            if(!mIsPaused) {
                final Activity activity = getActivity();
                if (null == activity || activity.isFinishing()) {
                    return;
                }

                // Events & Sensors
                final Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                if(accelerometer != null) {
                    mSensorManager.registerListener(CapturePreviewBuilder.getInstance(), accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                }
                this.mSurfaceView.setOnTouchListener(CapturePreviewBuilder.getInstance());

                mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), mCaptureCallback, mRendererHandler);
            }
            else{
                mSensorManager.unregisterListener(CapturePreviewBuilder.getInstance());
                mPreviewSession.abortCaptures();
            }
        }
        catch (CameraAccessException e) {
            showError(R.string.error_camera_generic);
        }
    }

    public void setPaused(boolean isPaused){
        Log.d(TAG, "setPaused("+isPaused+")");
        mIsPaused = isPaused;
        updatePreview();
    }

    public boolean isPaused(){
        return mIsPaused;
    }

    public void setRendererHandler(final Handler rendererHandler) {
        Log.d(TAG, "setRendererHandler("+rendererHandler+")");
        mRendererHandler = rendererHandler;
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        Log.d(TAG, "setSurfaceView("+surfaceView+")");
        mSurfaceView = surfaceView;
    }

    public SurfaceView getSurfaceView(){
        return mSurfaceView;
    }

    public void setMainHandler(final Handler mainHandler){
        Log.d(TAG, "setMainHandler("+mainHandler+")");
        mMainHandler = mainHandler;
    }

    private void showError(final int messageResourceId){
        if(mMainHandler != null){
            mMainHandler.sendMessage(mMainHandler.obtainMessage(Messaging.SYSTEM_ERROR, messageResourceId));
        }
    }

    public void setPreviewTexture(SurfaceTexture previewSurface) {
        Log.d(TAG, "setPreviewTexture()");
        this.mPreviewSurface = previewSurface;
    }

    public void setOnViewportSizeUpdatedListener(OnViewportSizeUpdatedListener listener) {
        Log.d(TAG, "setOnViewportSizeUpdatedListener()");
        this.mOnViewportSizeUpdatedListener = listener;
    }

    public interface OnViewportSizeUpdatedListener {
        void onViewportSizeUpdated(Size surfaceSize, Size previewSize);
    }

}
