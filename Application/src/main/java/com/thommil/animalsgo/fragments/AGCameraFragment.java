package com.thommil.animalsgo.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.support.annotation.NonNull;
import android.support.v4.util.Pools;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.androidexperiments.shadercam.fragments.CameraFragment;

import java.util.Arrays;


/**
 *  Dedicated CameraFragment implementation
 *
 */
public class AGCameraFragment extends CameraFragment{

    private static final String TAG = "A_GO/AGCameraFragment";

    //TODO test settings when recognition is OK
    // Number of frames between 2 updates
    private static final int CAPTURE_UPDATE_FREQUENCY = 10;

    // Mvt detection sensibility (more = less sensible)
    private static final float MOVEMENT_THRESHOLD = 1.0f;

    private OnCaptureCompletedListener mCaptureCompletedListener;

    private float mMaxZoom;
    private float mCurrentZoom;
    private Rect mActiveArraySize;
    private Rect mCurrentZoomRect;

    private Sensor mAccelerometer;

    @Override
    public void onPause() {
        super.onPause();

        final SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        if(mCaptureCallback != null) {
            sensorManager.unregisterListener((CaptureCallback)mCaptureCallback);
        }
    }

    @Override
    protected void startPreview() {
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

            if(mCaptureCallback == null){
                this.setCaptureCallback(new CaptureCallback(mCaptureCompletedListener));
            }

            if(mAccelerometer != null) {
                sensorManager.registerListener((CaptureCallback)mCaptureCallback, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }

        }catch (CameraAccessException cae){
            cae.printStackTrace();
        }

        super.startPreview();
    }

    @Override
    protected void setupCaptureRequest(CaptureRequest.Builder captureRequestBuilder) {
        //Log.d(TAG, "setupCaptureRequest");
        final Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }

        //Settings
        captureRequestBuilder.set(CaptureRequest.CONTROL_CAPTURE_INTENT, CaptureRequest.CONTROL_CAPTURE_INTENT_VIDEO_SNAPSHOT); // High quality video
        captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF); // No Flash (don't bother animals)
        captureRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE); // Find faces
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_OFF); // Outside purpose

        //Zoom
        if(mCurrentZoom != 1.0f) {
            mCurrentZoomRect.left = mActiveArraySize.centerX() - (int) ((mActiveArraySize.right / mCurrentZoom) / 2);
            mCurrentZoomRect.top = mActiveArraySize.centerY() - (int) ((mActiveArraySize.bottom / mCurrentZoom) / 2);
            mCurrentZoomRect.right = mCurrentZoomRect.left + (int) (mActiveArraySize.right / mCurrentZoom);
            mCurrentZoomRect.bottom = mCurrentZoomRect.top + (int) (mActiveArraySize.bottom / mCurrentZoom);
            captureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, mCurrentZoomRect);
        }

        this.mSurfaceView.setOnTouchListener((CaptureCallback)mCaptureCallback);
    }

    public void setOnCaptureCompletedListener(OnCaptureCompletedListener onCaptureCompletedListener) {
        this.mCaptureCompletedListener = onCaptureCompletedListener;
    }

    public void setZoom(float zoomFactor){
        mCurrentZoom = Math.abs(Math.min(zoomFactor, mMaxZoom));
        this.updatePreview();
    }

    /**
     * Decicated CameraCaptureSession.CaptureCallback used for QoS and event dispatch to Renderer
     *
     */
    private static class CaptureCallback extends CameraCaptureSession.CaptureCallback implements View.OnTouchListener, SensorEventListener{

        final private OnCaptureCompletedListener mCaptureCompletedListener;

        private static final int POOL_SIZE = 10;
        private static final Pools.SynchronizedPool<CaptureData> captureDataPool = new Pools.SynchronizedPool<>(POOL_SIZE );

        static{
            for(int i =0; i < POOL_SIZE; i++){
                captureDataPool.release(new CaptureData());
            }
        }

        private boolean isTouched = false;

        private int frameCount = 0;

        private boolean bIsmoving = false;

        final private float[] mGravity = new float[3];
        private float mAccel;
        private float mAccelCurrent;
        private float mAccelLast;


        public CaptureCallback(final OnCaptureCompletedListener captureCompletedListener) {
            this.mCaptureCompletedListener = captureCompletedListener;
            mAccel = 0.00f;
            mAccelCurrent = SensorManager.GRAVITY_EARTH;
            mAccelLast = SensorManager.GRAVITY_EARTH;
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
            bIsmoving = (mAccel > MOVEMENT_THRESHOLD);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
           //PASS
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            isTouched = !(motionEvent.getActionMasked() == MotionEvent.ACTION_UP && motionEvent.getPointerCount() == 1);
            return true;
        }


        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            if(frameCount > AGCameraFragment.CAPTURE_UPDATE_FREQUENCY) {
                //Listener
                CaptureData captureData = captureDataPool.acquire();

                //Camera state
                captureData.cameraState = false;
                final Integer afValue = result.get(CaptureResult.CONTROL_AF_STATE);
                if (afValue != null) {
                    switch (afValue) {
                        case CaptureResult.CONTROL_AF_STATE_INACTIVE:
                        case CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED:
                        case CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED:
                            final Integer aeValue = result.get(CaptureResult.CONTROL_AE_STATE);
                            if (aeValue != null) {
                                switch (aeValue) {
                                    case CaptureResult.CONTROL_AE_STATE_INACTIVE:
                                    case CaptureResult.CONTROL_AE_STATE_LOCKED:
                                    case CaptureResult.CONTROL_AE_STATE_CONVERGED:
                                        captureData.cameraState = true;
                                        captureData.lightState = true;
                                        break;
                                    case CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED:
                                        captureData.cameraState = false;
                                        captureData.lightState = false;
                                        break;
                                    default:
                                        captureData.cameraState = true;
                                        captureData.lightState = true;
                                }
                            } else {
                                captureData.cameraState = true;
                                captureData.lightState = true;
                            }

                            if (captureData.cameraState) {
                                final Integer awbValue = result.get(CaptureResult.CONTROL_AWB_STATE);
                                if (awbValue != null) {
                                    switch (awbValue) {
                                        case CaptureResult.CONTROL_AWB_STATE_INACTIVE:
                                        case CaptureResult.CONTROL_AWB_STATE_LOCKED:
                                        case CaptureResult.CONTROL_AWB_STATE_CONVERGED:
                                            captureData.cameraState = true;
                                            break;
                                        default:
                                            captureData.cameraState = false;
                                    }
                                } else {
                                    captureData.cameraState = true;
                                }
                            }

                            break;
                        default:
                            captureData.cameraState = false;
                    }
                } else {
                    captureData.cameraState = true;
                }

                //Faces
                final Face[] faces = result.get(CaptureResult.STATISTICS_FACES);
                captureData.facesState = (faces == null || faces.length == 0 );

                //Movement
                captureData.movementState = bIsmoving ? false : true;

                //Touch
                captureData.touchState = isTouched ? false : true;

                //Gravity
                System.arraycopy(mGravity, 0, captureData.gravity, 0, 3); ;

                mCaptureCompletedListener.onCaptureDataReceived(captureData);
                captureDataPool.release(captureData);
                frameCount=0;
            }
            else {
                frameCount++;
            }
        }
    }

    /**
     * Listener receiving capture events
     */
    public interface OnCaptureCompletedListener {

        // Called each time a frame has been processed and received current captureData
        void onCaptureDataReceived(final CaptureData captureData);
    }

    /**
     * Encapsulate needed/simplified infos from a CaptureResult
     */
    public static class CaptureData {

        public boolean cameraState = false;
        public boolean facesState = false;
        public boolean movementState = false;
        public boolean lightState = false;
        public boolean touchState = false;
        public float[] gravity = new float[3];

        public String toString(){
            return "[CAM:" +cameraState+", FCS:" +facesState+", MVT:"+movementState+", LGT:"+lightState+", TCH:"+touchState+", GRV :"+ Arrays.toString(gravity)+"]";
        }
    }
}
