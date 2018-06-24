package com.thommil.animalsgo.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pools;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.thommil.animalsgo.ErrorDialog;
import com.thommil.animalsgo.R;
import com.thommil.animalsgo.data.CapturePreview;
import com.thommil.animalsgo.data.Messaging;
import com.thommil.animalsgo.data.Settings;
import com.thommil.animalsgo.opencv.SnapshotValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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

    // The used camera ID
    private String mCameraId;

    // The used camera ID
    private CameraDevice mCameraDevice;

    // Preview size sent by camera
    private Size mPreviewSize;

    // Max preview width that is guaranteed by Camera2 API
    private static final int MAX_PREVIEW_WIDTH = 1920;

    // Max preview height that is guaranteed by Camera2 API
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    // A Semaphore to prevent the app from exiting before closing the camera.
    private final Semaphore mCameraOpenCloseLock = new Semaphore(1);

    //CaptureRequest.Builder for the camera preview
    private CaptureRequest.Builder mPreviewRequestBuilder;

    // CaptureSession from camera
    protected CameraCaptureSession mPreviewSession;

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

        mCameraSurfaceView.getHolder().addCallback(this);
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
        getActivity().finish();
    }

    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceCreated()");
        mCameraSurfaceView.getHolder().setKeepScreenOn(true);
        openCamera(mCameraSurfaceView.getWidth(), mCameraSurfaceView.getHeight());
    }

    @Override
    public void surfaceChanged(final SurfaceHolder surfaceHolder, final int format, final int width, final int height) {
        Log.d(TAG, "surfaceChanged("+format+","+width+","+height+")");
        configureTransform(width, height);
    }

    @Override
    public void surfaceDestroyed(final SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceDestroyed()");
    }

    // TODO Full impl with quality support
    private Size choosePreviewSize(Size[] choices) {
        Log.d(TAG, "chooseVideoSize("+Arrays.toString(choices)+")");
        final int sw = mCameraSurfaceView.getWidth(); //surface width
        final int sh = mCameraSurfaceView.getHeight(); //surface height

        Log.d(TAG, "Surface size : "+sw+"x"+sh);

        final float previewSurfaceAspectRatio = (float)sw / sh;

        Log.d(TAG, "chooseVideoSize() for landscape:" + (previewSurfaceAspectRatio > 1.f) + " aspect: " + previewSurfaceAspectRatio );

        //rather than in-lining returns, use this size as placeholder so we can calc aspectratio upon completion
        Size sizeToReturn = null;

        //video is 'landscape' if over 1.f
        if(previewSurfaceAspectRatio > 1.f) {
            for (Size size : choices) {
                if (size.getHeight() == size.getWidth() * 9 / 16 && size.getHeight() <= MAX_PREVIEW_WIDTH) {
                    sizeToReturn = size;
                }
            }

            //final check
            if(sizeToReturn == null)
                sizeToReturn = choices[0];

        }

        Log.i(TAG, "Final choice : " + sizeToReturn);

        return sizeToReturn;
    }

    private void configureTransform(final int width, final int height){
        Log.d(TAG, "configureTransform(");
        //TODO implement matrix transform
    }

    // CAMERA

    protected void openCamera(final int width, final int height) {
        Log.d(TAG, "openCamera("+width+","+height+")");
        final Activity activity = getActivity();
        final CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

        try {
            for (final String cameraId : manager.getCameraIdList()) {
                final CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                final StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // TODO Image capture settings & handler
                // For still image captures, we use the largest available size.
                /*
                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, 2); ///2= maxImages
                mImageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener, mBackgroundHandler);
                */

                mPreviewSize = choosePreviewSize(map.getOutputSizes(SurfaceHolder.class));
                mCameraSurfaceView.getHolder().setFixedSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

                mCameraId = cameraId;
                break;
            }
        }catch(CameraAccessException cae){
            ErrorDialog.newInstance(getString(R.string.error_camera_generic))
                    .show(getChildFragmentManager(), ErrorDialog.FRAGMENT_DIALOG);
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.error_camera_not_supported))
                    .show(getChildFragmentManager(), ErrorDialog.FRAGMENT_DIALOG);
        }

        configureTransform(width, height);

        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                ErrorDialog.newInstance(getString(R.string.error_camera_timeout))
                        .show(getChildFragmentManager(), ErrorDialog.FRAGMENT_DIALOG);
            }
            manager.openCamera(mCameraId, mStateCallback, mCameraSurfaceViewHandler);
        }catch(CameraAccessException cae){
            ErrorDialog.newInstance(getString(R.string.error_camera_generic))
                    .show(getChildFragmentManager(), ErrorDialog.FRAGMENT_DIALOG);
        }catch(InterruptedException ie){
            ErrorDialog.newInstance(getString(R.string.error_camera_generic))
                    .show(getChildFragmentManager(), ErrorDialog.FRAGMENT_DIALOG);
        }
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                ErrorDialog.newInstance(getString(R.string.error_camera_generic))
                        .show(getChildFragmentManager(), ErrorDialog.FRAGMENT_DIALOG);
            }
        }
    };

    private void createCameraPreviewSession() {
        Log.d(TAG, "createCameraPreviewSession()");

        try{
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(mCameraSurfaceView.getHolder().getSurface());

            mCameraDevice.createCaptureSession(Arrays.asList(mCameraSurfaceView.getHolder().getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mPreviewSession = cameraCaptureSession;
                            startPreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            ErrorDialog.newInstance(getString(R.string.error_camera_generic))
                                    .show(getChildFragmentManager(), ErrorDialog.FRAGMENT_DIALOG);
                        }
                    }, null
            );


        } catch (CameraAccessException e) {
            ErrorDialog.newInstance(getString(R.string.error_camera_generic))
                    .show(getChildFragmentManager(), ErrorDialog.FRAGMENT_DIALOG);
        }
    }

    private void startPreview(){
        try {
            mCameraSurfaceView.setOnTouchListener(CapturePreviewBuilder.getInstance());
            final SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            final Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if(accelerometer != null) {
                sensorManager.registerListener(CapturePreviewBuilder.getInstance(), accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }

            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_CAPTURE_INTENT, CaptureRequest.CONTROL_CAPTURE_INTENT_VIDEO_SNAPSHOT); // High quality video
            mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF); // No Flash (don't bother animals)
            mPreviewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF); // Faces using OpenCV outside
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_OFF); // Outside purpose
            mPreviewSession.setRepeatingRequest(mPreviewRequestBuilder.build(),mCaptureCallback, mCameraSurfaceViewHandler);
        } catch (CameraAccessException e) {
            ErrorDialog.newInstance(getString(R.string.error_camera_generic))
                    .show(getChildFragmentManager(), ErrorDialog.FRAGMENT_DIALOG);
        }
    }

    private void stopPreview(){
        try {
            mPreviewSession.abortCaptures();
            final SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            sensorManager.unregisterListener(CapturePreviewBuilder.getInstance());
        } catch (CameraAccessException e) {
            ErrorDialog.newInstance(getString(R.string.error_camera_generic))
                    .show(getChildFragmentManager(), ErrorDialog.FRAGMENT_DIALOG);
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


    protected void closeCamera(){
        Log.d(TAG, "closeCamera()");
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mPreviewSession) {
                mPreviewSession.close();
                mPreviewSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            //TODO end image reader
            //if (null != mImageReader) {
            //    mImageReader.close();
            //    mImageReader = null;
            //}
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    // THREAD

    @Override
    public boolean handleMessage(final Message message) {
        Log.d(TAG, "handleMessage(" + message+ ")");
        switch (message.what){
            case Messaging.SYSTEM_ERROR :
                ErrorDialog.newInstance((String)message.obj)
                        .show(getChildFragmentManager(), ErrorDialog.FRAGMENT_DIALOG);
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
