package com.thommil.animalsgo.cv;

import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;

import com.thommil.animalsgo.data.Capture;
import com.thommil.animalsgo.utils.ByteBufferPool;

/**
 * Decicated CameraCaptureSession.CaptureCallback used for QoS and event dispatch to Renderer
 *
 */
public class CaptureBuilder {

    private static final String TAG = "A_GO/CaptureBuilder";

    private final Capture mCaptureBuilder = new Capture();
    private final Capture mCapture = new Capture();
    private boolean mIsdirty = false;

    private final static CaptureBuilder sCapturePreviewBuilder = new CaptureBuilder();

    public static CaptureBuilder getInstance(){
        return sCapturePreviewBuilder;
    }

    public CaptureBuilder buildCapture(final TotalCaptureResult result) {
        if (mCaptureBuilder.mCameraBuffer != null) {
            ByteBufferPool.getInstance().returnDirectBuffer(mCaptureBuilder.mCameraBuffer);
            mCaptureBuilder.mCameraBuffer = null;
        }

        //Camera state
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
                                mCaptureBuilder.cameraState = Capture.STATE_READY;
                                mCaptureBuilder.lightState = Capture.STATE_READY;
                                break;
                            case CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED:
                                mCaptureBuilder.cameraState = Capture.STATE_NOT_READY;
                                mCaptureBuilder.lightState = Capture.STATE_NOT_READY;
                                break;
                            default:
                                mCaptureBuilder.cameraState = Capture.STATE_READY;
                                mCaptureBuilder.lightState = Capture.STATE_NOT_AVAILABLE;
                        }
                    } else {
                        mCaptureBuilder.cameraState = Capture.STATE_READY;
                        mCaptureBuilder.lightState = Capture.STATE_NOT_AVAILABLE;
                    }

                    if (mCaptureBuilder.cameraState == Capture.STATE_READY) {
                        final Integer awbValue = result.get(CaptureResult.CONTROL_AWB_STATE);
                        if (awbValue != null) {
                            switch (awbValue) {
                                case CaptureResult.CONTROL_AWB_STATE_INACTIVE:
                                case CaptureResult.CONTROL_AWB_STATE_LOCKED:
                                case CaptureResult.CONTROL_AWB_STATE_CONVERGED:
                                    mCaptureBuilder.cameraState = Capture.STATE_READY;
                                    break;
                                default:
                                    mCaptureBuilder.cameraState = Capture.STATE_NOT_READY;
                            }
                        } else {
                            mCaptureBuilder.cameraState = Capture.STATE_READY;
                        }
                    }

                    if (mCaptureBuilder.cameraState == Capture.STATE_READY) {
                        final Integer lensValue = result.get(CaptureResult.LENS_STATE);
                        if (lensValue != null) {
                            switch (lensValue) {
                                case CaptureResult.LENS_STATE_STATIONARY:
                                    mCaptureBuilder.cameraState = Capture.STATE_READY;
                                    break;
                                default:
                                    mCaptureBuilder.cameraState = Capture.STATE_NOT_READY;
                            }
                        } else {
                            mCaptureBuilder.cameraState = Capture.STATE_READY;
                        }
                    }

                    break;
                default:
                    mCaptureBuilder.cameraState = Capture.STATE_NOT_READY;
            }
        } else {
            mCaptureBuilder.cameraState = Capture.STATE_NOT_AVAILABLE;
        }

        //Faces
        final Face[] faces = result.get(CaptureResult.STATISTICS_FACES);
        if(faces != null){
            if(faces.length > 1){
                mCaptureBuilder.faceState = Capture.STATE_NOT_READY;
            }
            else{
                mCaptureBuilder.faceState = Capture.STATE_READY;
            }
        }
        else{
            mCaptureBuilder.faceState = Capture.STATE_NOT_AVAILABLE;
        }

        mIsdirty = true;

        return this;
    }

    public synchronized Capture getCapture(){
        if(mIsdirty){
            mCapture.cameraState = mCaptureBuilder.cameraState;
            mCapture.lightState = mCaptureBuilder.lightState;
            mCapture.faceState = mCaptureBuilder.faceState;
            if (mCapture.mCameraBuffer != null) {
                ByteBufferPool.getInstance().returnDirectBuffer(mCapture.mCameraBuffer);
                mCapture.mCameraBuffer = null;
            }
            mIsdirty = false;
        }
        return mCapture;
    }
}
