package com.thommil.animalsgo.fragments;

import android.app.Activity;
import android.content.Context;
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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.thommil.animalsgo.R;
import com.thommil.animalsgo.capture.CaptureBuilder;
import com.thommil.animalsgo.capture.Capture;
import com.thommil.animalsgo.data.Messaging;
import com.thommil.animalsgo.data.Orientation;
import com.thommil.animalsgo.Settings;
import com.thommil.animalsgo.gl.CameraPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Fragment for operating the camera, it doesnt have any UI elements, just controllers
 */
public class CameraFragment extends Fragment implements View.OnTouchListener, SensorEventListener{

    private static final String TAG = "A_GO/CameraFragment";

    // Max preview width that is guaranteed by Camera2 API
    private static final int MAX_PREVIEW_WIDTH = 1920;

    // Max preview height that is guaranteed by Camera2 API
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    // Machine states
    private final static int STATE_ERROR = 0x00;
    private final static int STATE_PREVIEW = 0x01;

    // Current Thread state
    private int mState = STATE_PREVIEW;

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

    // Indicates if view is in touch down state
    private boolean mIsTouched = false;

    // Indicates if device is moving
    private boolean mIsmoving = false;

    // Storaga for movt tests
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    // Gravity sensor
    final private float[] mGravity = new float[]{0,SensorManager.GRAVITY_EARTH,0};

    // Current orientation
    private final Orientation mOrientation = new Orientation();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mState = STATE_PREVIEW;
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }

    @Override
    public void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    public void openCamera()
    {
        //Log.d(TAG, "openCamera()");
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

            if(mRendererHandler != null) {
                mRendererHandler.sendMessage(mRendererHandler.obtainMessage(Messaging.CHANGE_PREVIEW_SIZE, mPreviewSize));
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
            //Log.d(TAG, "onOpened("+cameraDevice+")");
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            mCameraIsOpen = true;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            //Log.d(TAG, "onDisconnected("+cameraDevice+")");
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            mCameraIsOpen = false;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            //Log.d(TAG, "onError("+cameraDevice+", "+error+")");
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            mCameraIsOpen = false;
            showError(R.string.error_camera_generic);
        }
    };

    private Size choosePreviewSize(Size[] choices)
    {
        //Log.d(TAG, "chooseVideoSize("+Arrays.toString(choices)+")");
        final int sw = mSurfaceView.getWidth(); //surface width
        final int sh = mSurfaceView.getHeight(); //surface height

        //Log.d(TAG, "Surface size : "+sw+"x"+sh);

        mPreviewSurfaceAspectRatio = (float)sw / sh;

        //Log.d(TAG, "chooseVideoSize() for landscape:" + (mPreviewSurfaceAspectRatio > 1.f) + " aspect: " + mPreviewSurfaceAspectRatio);

        Size sizeToReturn = null;

        final String qualitySettings = Settings.getInstance().getString(Settings.CAMERA_PREVIEW_QUALITY);
        final String[] qualityValues = getResources().getStringArray(R.array.prefs_camera_preview_quality_entries_values);
        final List<Size> choicesList = new LinkedList<>(Arrays.asList(choices));
        //Log.d(TAG, "All valid choices :" + choicesList);

        final List<Size> bestChoicesList = new ArrayList<>();
        for (int i = 0; i < choicesList.size(); i++) {
            final Size size = choicesList.get(i);
            if (size.getWidth() > MAX_PREVIEW_WIDTH || size.getHeight() > MAX_PREVIEW_HEIGHT
                    || (size.getWidth() * size.getHeight() > sh * sw)) {
                choicesList.remove(i);
                i--;
            } else {
                if (((float) size.getHeight() / size.getWidth()) == mPreviewSurfaceAspectRatio) {
                    bestChoicesList.add(size);
                    choicesList.remove(i);
                    i--;
                }
            }
        }
        //Log.d(TAG, "Best available choices :" + bestChoicesList);

        //Auto
        if(Settings.getInstance().getBoolean(Settings.CAMERA_PREVIEW_QUALITY_AUTO)){
            if(!bestChoicesList.isEmpty()){
                // TODO if opencv is laggy, select last one
                sizeToReturn = bestChoicesList.get(0);
            }
            else {
                for (final Size size : choicesList) {
                    if (size.getHeight() >= sw) {
                        sizeToReturn = size;
                    } else {
                        break;
                    }
                }
            }
        }
        //Manual
        else {
            //Lowest setting
            if (qualitySettings.equals(qualityValues[0])) {
                sizeToReturn = choices[choices.length - 1];
            }
            // Other
            else {
                int qualityIndex = qualityValues.length - 1;
                for (final String quality : qualityValues) {
                    if (qualitySettings.equals(quality)) {
                        break;
                    }
                    qualityIndex--;
                }

                //Find in best choices
                if (!bestChoicesList.isEmpty() && qualityIndex < bestChoicesList.size()) {
                    sizeToReturn = bestChoicesList.get(qualityIndex);
                }

                //Find in other choices
                if (sizeToReturn == null) {
                    if (!bestChoicesList.isEmpty()) {
                        for (int i = 0; i < choicesList.size(); i++) {
                            if (choicesList.get(i).getWidth() > bestChoicesList.get(0).getWidth() || choicesList.get(i).getHeight() > bestChoicesList.get(0).getHeight()) {
                                choicesList.remove(i);
                                i--;
                            }
                        }
                    }

                    //Log.d(TAG, "No best choice found, use fallback : " + choicesList);

                    if (choicesList.size() < qualityValues.length) {
                        qualityIndex = Math.min(choicesList.size() - 1, qualityIndex);
                        sizeToReturn = choicesList.get(qualityIndex);
                    } else {
                        final int startIndex = choicesList.size() / qualityValues.length * qualityIndex;
                        final int stopIndex = choicesList.size() / qualityValues.length * qualityIndex + choicesList.size() / qualityValues.length;
                        float bestRatioDelta = 10;
                        for (final Size size : choicesList.subList(startIndex, stopIndex)) {
                            final float currentRatioDelta = Math.abs(((float) size.getHeight() / size.getWidth()) - mPreviewSurfaceAspectRatio);
                            if (currentRatioDelta < bestRatioDelta) {
                                sizeToReturn = size;
                                bestRatioDelta = currentRatioDelta;
                            }
                        }
                    }
                }
            }
        }

        if (sizeToReturn == null) {
            sizeToReturn = choicesList.get(0);
        }

        Log.i(TAG, "Final choice : " + sizeToReturn);

        return sizeToReturn;
    }

    public void closeCamera() {
        //Log.d(TAG, "closeCamera()");
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

        // Reference to the CaptureBuilder instance
        private final CaptureBuilder mCaptureBuilder = CaptureBuilder.getInstance();

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            switch(mState){
                case STATE_PREVIEW :
                    if(mFrameCount > Settings.CAPTURE_UPDATE_FREQUENCY){
                        final Capture capture = mCaptureBuilder.buildCapture(result).getCapture();
                        System.arraycopy(mGravity, 0, capture.gravity, 0, 3);
                        capture.movement = mAccel;
                        //TODO setting touch
                        if(!mIsTouched && !mIsmoving &&
                                capture.validationState == Capture.VALIDATION_WAIT &&
                                capture.cameraState != Capture.STATE_NOT_READY &&
                                capture.lightState != Capture.STATE_NOT_READY &&
                                capture.faceState!= Capture.STATE_NOT_READY){
                            mRendererHandler.sendMessage(mRendererHandler.obtainMessage(Messaging.CAPTURE_NEXT_FRAME, capture));
                        }
                        else {
                            mRendererHandler.sendMessage(mRendererHandler.obtainMessage(Messaging.CHANGE_CAPTURE, capture));
                        }
                        mFrameCount = 0;
                    }
                    mFrameCount++;
                    break;
            }

        }

    };

    protected void startPreview(){
        //Log.d(TAG, "startPreview()");
        if (null == mCameraDevice || null == mPreviewSize || !mSurfaceView.getHolder().getSurface().isValid() || mRendererHandler == null) {
            return;
        }
        try {
            // Events & Sensors
            final Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if(accelerometer != null) {
                mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }

            mSurfaceView.setOnTouchListener(this);

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
            mPreviewBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE); // Faces detection

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
                        showError(R.string.error_camera_generic);
                    }
                }
            }, mRendererHandler);
        }
        catch (CameraAccessException e) {
            showError(R.string.error_camera_generic);
        }
    }

    protected void updatePreview() {
        //Log.d(TAG, "updatePreview()");
        if (null == mCameraDevice) {
            return;
        }
        try {
            final Activity activity = getActivity();
            if (null == activity || activity.isFinishing()) {
                return;
            }

            if(!mIsPaused) {
                mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), mCaptureCallback, mRendererHandler);
            }
            else{
                mPreviewSession.abortCaptures();
            }
        }
        catch (CameraAccessException e) {
            showError(R.string.error_camera_generic);
        }
    }

    public void setPaused(boolean isPaused){
        //Log.d(TAG, "setPaused("+isPaused+")");
        mIsPaused = isPaused;
        updatePreview();
    }

    public boolean isPaused(){
        return mIsPaused;
    }

    public void setRendererHandler(final Handler rendererHandler) {
        //Log.d(TAG, "setRendererHandler("+rendererHandler+")");
        mRendererHandler = rendererHandler;
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        //Log.d(TAG, "setSurfaceView("+surfaceView+")");
        mSurfaceView = surfaceView;
    }

    public SurfaceView getSurfaceView(){
        return mSurfaceView;
    }

    public void setMainHandler(final Handler mainHandler){
        //Log.d(TAG, "setMainHandler("+mainHandler+")");
        mMainHandler = mainHandler;
    }

    private void showError(final int messageResourceId){
        mState = STATE_ERROR;
        if(mMainHandler != null){
            mMainHandler.sendMessage(mMainHandler.obtainMessage(Messaging.SYSTEM_ERROR, messageResourceId));
        }
    }

    public void setPreviewTexture(SurfaceTexture previewSurface) {
        //Log.d(TAG, "setPreviewTexture()");
        this.mPreviewSurface = previewSurface;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //Log.d(TAG, "onTouch("+motionEvent+")");
        mIsTouched = !(motionEvent.getActionMasked() == MotionEvent.ACTION_UP && motionEvent.getPointerCount() == 1);
        return true;
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Log.d(TAG, "onKeyDown("+keyCode+")");
        mIsTouched = true;
        mRendererHandler.sendMessage(mRendererHandler.obtainMessage(Messaging.CHANGE_ZOOM,
                (keyCode == KeyEvent.KEYCODE_VOLUME_UP) ? CameraPlugin.ZOOM_STATE_IN:CameraPlugin.ZOOM_STATE_OUT));
        return true;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //Log.d(TAG, "onKeyUp("+keyCode+")");
        mIsTouched = false;
        mRendererHandler.sendMessage(mRendererHandler.obtainMessage(Messaging.CHANGE_ZOOM, CameraPlugin.ZOOM_STATE_NONE));
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        System.arraycopy(sensorEvent.values, 0, mGravity, 0, 3); ;
        final float x = mGravity[0];
        final float y = mGravity[1];
        final float z = mGravity[2];
        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float)Math.sqrt(x*x + y*y + z*z);
        final float delta = Math.abs(mAccelCurrent - mAccelLast);
        mAccel = mAccel * 0.9f + delta;
        mIsmoving = (mAccel > Settings.MOVEMENT_THRESHOLD);

        if(mAccel > Settings.MOVEMENT_ORIENTATION_CHANGE_THRESHOLD) {
            final int previousOrientation = mOrientation.getOrientation();
            mOrientation.setValue(x, y, z);
            if (mOrientation.getOrientation() != previousOrientation) {
                if (mRendererHandler != null) {
                    mRendererHandler.sendMessage(mRendererHandler.obtainMessage(Messaging.SYSTEM_ORIENTATION_CHANGE, mOrientation));
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //PASS
    }
}
