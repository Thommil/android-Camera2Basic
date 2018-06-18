package com.thommil.animalsgo.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.support.annotation.NonNull;
import android.support.v4.util.Pools;
import android.util.Log;
import android.util.Range;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.androidexperiments.shadercam.fragments.CameraFragment;
import com.thommil.animalsgo.gl.AGCameraRenderer;

import java.util.Arrays;

/**
 *  Dedicated CameraFragment implementation :
 *      - CaptureRequest.Builder specific conf
 *      - QoS
 *
 *      TODO Quality in prefs
 */
public class AGCameraFragment extends CameraFragment implements View.OnTouchListener{

    private static final String TAG = "A_GO/AGCameraFragment";

    public enum Quality {QUALITY_MEDIUM, QUALITY_HIGH};

    private Quality mQuality = Quality.QUALITY_HIGH;

    private CaptureCallback mCaptureCallback;

    private OnCaptureCompletedListener mCaptureCompletedListener;

    private float mMaxZoom;
    private float mCurrentZoom;
    private Rect mActiveArraySize;
    private Rect mCurrentZoomRect;

    @Override
    public void onResume() {
        super.onResume();
        this.mSurfaceView.setOnTouchListener(this);
    }

    @Override
    protected void startPreview() {
        try {
            final CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraDevice.getId());

            mMaxZoom =  characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
            mCurrentZoom = 1.0f;
            mActiveArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            mCurrentZoomRect = new Rect(mActiveArraySize);

            if(mCaptureCallback == null){
                this.setCaptureCallback(new CaptureCallback(this, mCaptureCompletedListener));
            }

        }catch (CameraAccessException cae){
            cae.printStackTrace();
        }

        super.startPreview();
    }

    @Override
    protected void setupCaptureRequest(CaptureRequest.Builder captureRequestBuilder) {
        Log.d(TAG, "setupCaptureRequest");
        final Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }

        //Common settings
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
        captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
        captureRequestBuilder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON);
        captureRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF);

        //Quality based settings
        switch (mQuality){
            case QUALITY_HIGH:
                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                captureRequestBuilder.set(CaptureRequest.CONTROL_CAPTURE_INTENT, CaptureRequest.CONTROL_CAPTURE_INTENT_VIDEO_SNAPSHOT);
                captureRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range(30, 60));
                captureRequestBuilder.set(CaptureRequest.EDGE_MODE, CaptureRequest.EDGE_MODE_HIGH_QUALITY);
                captureRequestBuilder.set(CaptureRequest.HOT_PIXEL_MODE, CaptureRequest.HOT_PIXEL_MODE_HIGH_QUALITY);
                captureRequestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);
                captureRequestBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_HIGH_QUALITY);
                captureRequestBuilder.set(CaptureRequest.SHADING_MODE, CaptureRequest.SHADING_MODE_HIGH_QUALITY);
                captureRequestBuilder.set(CaptureRequest.TONEMAP_MODE, CaptureRequest.TONEMAP_MODE_HIGH_QUALITY);
                break;
            default:
                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                captureRequestBuilder.set(CaptureRequest.CONTROL_CAPTURE_INTENT, CaptureRequest.CONTROL_CAPTURE_INTENT_VIDEO_RECORD);
                captureRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_FAST);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range(0, 30));
                captureRequestBuilder.set(CaptureRequest.EDGE_MODE, CaptureRequest.EDGE_MODE_FAST);
                captureRequestBuilder.set(CaptureRequest.HOT_PIXEL_MODE, CaptureRequest.HOT_PIXEL_MODE_FAST);
                captureRequestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF);
                captureRequestBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_FAST);
                captureRequestBuilder.set(CaptureRequest.SHADING_MODE, CaptureRequest.SHADING_MODE_FAST);
                captureRequestBuilder.set(CaptureRequest.TONEMAP_MODE, CaptureRequest.TONEMAP_MODE_FAST);
                break;
        }

        //Zoom
        if(mCurrentZoom != 1.0f) {
            mCurrentZoomRect.left = mActiveArraySize.centerX() - (int) ((mActiveArraySize.right / mCurrentZoom) / 2);
            mCurrentZoomRect.top = mActiveArraySize.centerY() - (int) ((mActiveArraySize.bottom / mCurrentZoom) / 2);
            mCurrentZoomRect.right = mCurrentZoomRect.left + (int) (mActiveArraySize.right / mCurrentZoom);
            mCurrentZoomRect.bottom = mCurrentZoomRect.top + (int) (mActiveArraySize.bottom / mCurrentZoom);
            captureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, mCurrentZoomRect);
        }
    }

    public Quality getQuality() {
        return mQuality;
    }

    public void setQuality(Quality quality) {
        Log.d(TAG, "setQuality - " + quality);
        this.mQuality = quality;
        this.updatePreview();
    }

    public void setOnCaptureCompletedListener(OnCaptureCompletedListener onCaptureCompletedListener) {
        this.mCaptureCompletedListener = onCaptureCompletedListener;
    }



    public void setZoom(float zoomFactor){
        mCurrentZoom = Math.abs(Math.min(zoomFactor , mMaxZoom));
        this.updatePreview();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //this.updatePreview();
        return false;
    }

    /**
     * Decicated CameraCaptureSession.CaptureCallback used for QoS and event dispatch to Renderer
     *
     */
    private static class CaptureCallback extends CameraCaptureSession.CaptureCallback{

        private AGCameraFragment mCameraFragment;
        private OnCaptureCompletedListener mCaptureCompletedListener;

        private static final int POOL_SIZE = 10;
        private static final Pools.SimplePool<CaptureData> captureDataPool = new Pools.SimplePool<CaptureData>(POOL_SIZE );

        protected long lastTime = 0;
        protected int frames = 0;

        // Threshold to lower quality 25fps
        private int THRESHOLD = 25 * 5;
        private boolean bEnabledQoS = true;

        public CaptureCallback(AGCameraFragment cameraFragment, OnCaptureCompletedListener captureCompletedListener) {
            this(cameraFragment, captureCompletedListener, true);
        }

        public CaptureCallback(AGCameraFragment cameraFragment, OnCaptureCompletedListener captureCompletedListener, boolean enabledQoS) {
            this.mCameraFragment = cameraFragment;
            this.mCaptureCompletedListener = captureCompletedListener;
            this.bEnabledQoS = enabledQoS;
            for(int i =0; i < POOL_SIZE; i++){
                captureDataPool.release(new CaptureData());
            }
        }

        protected void QoS(){
            if(lastTime == 0) {
                lastTime = System.currentTimeMillis();
            }
            frames ++;
            long currentTime = System.currentTimeMillis();

            if(currentTime - lastTime > 5000){
                lastTime = currentTime;
                //Lower quality
                Log.d(TAG, "Average Camera FPS - " + ((float)frames/5f));
                if(frames < THRESHOLD){
                    switch (mCameraFragment.mQuality){
                        case QUALITY_HIGH:
                            mCameraFragment.setQuality(Quality.QUALITY_MEDIUM);
                            break;
                    }
                }
                frames = 0;
            }
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            //Listener
            CaptureData captureData = captureDataPool.acquire();
            mCaptureCompletedListener.onCaptureDataReceived(captureData);
            captureDataPool.release(captureData);

            //QoS
            if(bEnabledQoS) {
                QoS();
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

    }
}
