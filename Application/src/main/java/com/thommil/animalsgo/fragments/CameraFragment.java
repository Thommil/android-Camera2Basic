package com.thommil.animalsgo.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pools;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.thommil.animalsgo.data.CapturePreview;
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

    //TODO test settings when recognition is OK
    // Number of frames between 2 updates
    private static final int CAPTURE_UPDATE_FREQUENCY = 10;

    // Mvt detection sensibility (more = less sensible)
    private static final float MOVEMENT_THRESHOLD = 1f;

    /**
     * A {@link SurfaceView} for camera preview.
     */
    protected SurfaceView mSurfaceView;

    /**
     * A refernce to the opened {@link CameraDevice}.
     */
    protected CameraDevice mCameraDevice;

    /**
     * A reference to the current {@link CameraCaptureSession} for preview.
     */
    protected CameraCaptureSession mPreviewSession;

    /**
     * Surface to render preview of camera
     */
    protected SurfaceTexture mPreviewSurface;

    /**
     * The {@link Size} of camera preview.
     */
    protected Size mPreviewSize;

    /**
     * Camera preview.
     */
    protected CaptureRequest.Builder mPreviewBuilder;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    protected Handler mBackgroundHandler;

    /**
     * Use these for changing which camera to use on start
     */
    public static final int CAMERA_PRIMARY = 0;

    /**
     * The id of what is typically the forward facing camera.
     * If this fails, use {@link #CAMERA_PRIMARY}, as it might be the only camera registered.
     */
    public static final int CAMERA_FORWARD = 1;

    /**
     * Default Camera to use
     */
    protected int mCameraToUse = CAMERA_PRIMARY;


    /**
     * Listener for when openCamera is called and a proper video size is created
     */
    protected OnViewportSizeUpdatedListener mOnViewportSizeUpdatedListener;

    protected float mPreviewSurfaceAspectRatio;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    protected Semaphore mCameraOpenCloseLock = new Semaphore(1);

    protected boolean mCameraIsOpen = false;

    private boolean bIsPaused = false;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private float mMaxZoom;
    private float mCurrentZoom;
    private Rect mActiveArraySize;
    private Rect mCurrentZoomRect;

    private Sensor mAccelerometer;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Switch between the back(primary) camera and the front(selfie) camera
     */
    public void swapCamera()
    {
        //Log.d(TAG, "swapCamera");
        closeCamera();

        if(mCameraToUse == CAMERA_FORWARD)
            mCameraToUse = CAMERA_PRIMARY;
        else
            mCameraToUse = CAMERA_FORWARD;

        openCamera();
    }

    /**
     * Tries to open a CameraDevice. The result is listened by `mStateCallback`.
     */
    public void openCamera()
    {
        //Log.d(TAG, "openCamera");
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
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            String[] cameraList = manager.getCameraIdList();

            //make sure we dont get array out of bounds error, default to primary [0] if thats the case
            if(mCameraToUse >= cameraList.length)
                mCameraToUse = CAMERA_PRIMARY;

            String cameraId = cameraList[mCameraToUse];

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            //typically these are identical
            mPreviewSize = chooseVideoSize(streamConfigurationMap.getOutputSizes(SurfaceHolder.class));

            //send back for updates to renderer if needed
            if(mOnViewportSizeUpdatedListener != null) {
                mOnViewportSizeUpdatedListener.onViewportSizeUpdated(new Size(mSurfaceView.getWidth(), mSurfaceView.getHeight()),mPreviewSize);
            }

            manager.openCamera(cameraId, mStateCallback, null);
        }
        catch (CameraAccessException e) {
            Toast.makeText(activity, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
            activity.finish();
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();

        }
        catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its status.
     */
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            //Log.d(TAG, "onOpened");
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            mCameraIsOpen = true;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            //Log.d(TAG, "onDisconnected");
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            mCameraIsOpen = false;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            //Log.d(TAG, "onError - "+error);
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            mCameraIsOpen = false;

            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }
    };

    /**
     * chooseVideoSize makes a few assumptions for the sake of our use-case.
     *
     * @param choices The list of available sizes
     * @return The video size
     */
    private Size chooseVideoSize(Size[] choices)
    {
        //Log.d(TAG, "chooseVideoSize");
        int sw = mSurfaceView.getWidth(); //surface width
        int sh = mSurfaceView.getHeight(); //surface height

        //Log.d(TAG, "Surface size : "+sw+"x"+sh);

        mPreviewSurfaceAspectRatio = (float)sw / sh;

        //Log.d(TAG, "chooseVideoSize() for landscape:" + (mPreviewSurfaceAspectRatio > 1.f) + " aspect: " + mPreviewSurfaceAspectRatio + " : " + Arrays.toString(choices) );

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
                        //Log.d(TAG, "potential : " + potential);
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

    /**
     * close camera when not in use/pausing/leaving
     */
    public void closeCamera() {
        //Log.d(TAG, "closeCamera");
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
                mCameraIsOpen = false;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }


    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        private int mFrameCount = 0;

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

    /**
     * Start the camera preview.
     */
    protected void startPreview()
    {
        try {
            final CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraDevice.getId());

            final SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            if(mAccelerometer == null) {
                mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }

            mMaxZoom =  characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
            mCurrentZoom = 1.0f;
            mActiveArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            mCurrentZoomRect = new Rect(mActiveArraySize);

            this.setCaptureCallback(mCaptureCallback);

            if(mAccelerometer != null) {
                sensorManager.registerListener(CapturePreviewBuilder.getInstance(), mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }

        }catch (CameraAccessException cae){
            cae.printStackTrace();
        }

        //Log.d(TAG, "startPreview");
        if (null == mCameraDevice || null == mPreviewSize) {
            //if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
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
            }, mBackgroundHandler);
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    /**
     * Update the camera preview. {@link #startPreview()} needs to be called in advance.
     */
    protected void updatePreview() {
        //Log.d(TAG, "updatePreview");
        if (null == mCameraDevice) {
            return;
        }
        try {
            if(!bIsPaused) {
                final Activity activity = getActivity();
                if (null == activity || activity.isFinishing()) {
                    return;
                }

                //Settings
                mPreviewBuilder.set(CaptureRequest.CONTROL_CAPTURE_INTENT, CaptureRequest.CONTROL_CAPTURE_INTENT_VIDEO_SNAPSHOT); // High quality video
                mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF); // No Flash (don't bother animals)
                mPreviewBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF); // Faces using OpenCV outside
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_OFF); // Outside purpose

                //Zoom
                if(mCurrentZoom != 1.0f) {
                    mCurrentZoomRect.left = mActiveArraySize.centerX() - (int) ((mActiveArraySize.right / mCurrentZoom) / 2);
                    mCurrentZoomRect.top = mActiveArraySize.centerY() - (int) ((mActiveArraySize.bottom / mCurrentZoom) / 2);
                    mCurrentZoomRect.right = mCurrentZoomRect.left + (int) (mActiveArraySize.right / mCurrentZoom);
                    mCurrentZoomRect.bottom = mCurrentZoomRect.top + (int) (mActiveArraySize.bottom / mCurrentZoom);
                    mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, mCurrentZoomRect);
                }

                this.mSurfaceView.setOnTouchListener(CapturePreviewBuilder.getInstance());

                if (mCaptureCallback == null) {
                    mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                            super.onCaptureCompleted(session, request, result);
                        }
                    };
                }
                mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), mCaptureCallback, mBackgroundHandler);
            }
            else{
                mPreviewSession.abortCaptures();
            }
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        final SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(CapturePreviewBuilder.getInstance());
    }

    public void setPaused(boolean isPaused){
        bIsPaused = isPaused;
        updatePreview();
    }

    public boolean isPaused(){
        return bIsPaused;
    }

    public void setZoom(float zoomFactor){
        mCurrentZoom = Math.abs(Math.min(zoomFactor, mMaxZoom));
        this.updatePreview();
    }

    /**
     * Allow to set custom CameraCaptureSession.CaptureCallback
     *
     * @param captureCallback The CameraCaptureSession.CaptureCallback  to set
     */
    public void setCaptureCallback(CameraCaptureSession.CaptureCallback captureCallback){
        mCaptureCallback = captureCallback;
    }

    /**
     * Set external BG handler for capture session callbacks
     *
     * @param backgroundHandler
     */
    public void setBackgroundHandler(Handler backgroundHandler) {
        this.mBackgroundHandler = backgroundHandler;
    }

    /**
     * set the surfaceView to render the camera preview inside
     * @param surfaceView
     */
    public void setSurfaceView(SurfaceView surfaceView) {
        //Log.d(TAG, "setSurfaceView");
        mSurfaceView = surfaceView;
    }

    public SurfaceView getSurfaceView(){
        return mSurfaceView;
    }

    /**
     * Get the current camera type. Either {@link #CAMERA_FORWARD} or {@link #CAMERA_PRIMARY}
     * @return current camera type
     */
    public int getCurrentCameraType(){
        //Log.d(TAG, "getCurrentCameraType");
        return mCameraToUse;
    }

    /**
     * Set which camera to use, defaults to {@link #CAMERA_PRIMARY}.
     * @param camera_id can also be {@link #CAMERA_FORWARD} for forward facing, but use primary if that fails.
     */
    public void setCameraToUse(int camera_id)
    {
        //Log.d(TAG, "setCameraToUse - " + camera_id);
        mCameraToUse = camera_id;
    }

    /**
     * Set the texture that we'll be drawing our camera preview to. This is created from our TextureView
     * in our Renderer to be used with our shaders.
     * @param previewSurface
     */
    public void setPreviewTexture(SurfaceTexture previewSurface) {
        //Log.d(TAG, "setPreviewTexture");
        this.mPreviewSurface = previewSurface;
    }

    public void setOnViewportSizeUpdatedListener(OnViewportSizeUpdatedListener listener) {
        //Log.d(TAG, "setOnViewportSizeUpdatedListener");
        this.mOnViewportSizeUpdatedListener = listener;
    }

    /**
     * Listener interface that will send back the newly created {@link Size} of our camera output
     */
    public interface OnViewportSizeUpdatedListener {
        void onViewportSizeUpdated(Size surfaceSize, Size previewSize);
    }

}
