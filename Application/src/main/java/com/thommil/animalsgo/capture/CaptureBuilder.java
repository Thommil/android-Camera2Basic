package com.thommil.animalsgo.capture;

import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.support.v4.util.Pools;

/**
 * Decicated CameraCaptureSession.CaptureCallback used for QoS and event dispatch to Renderer
 *
 */
public class CaptureBuilder {

    private static final int POOL_SIZE = 10;
    private static final Pools.SimplePool<Capture> sCapturePreviewPool = new Pools.SimplePool<>(POOL_SIZE );

    static{
        for(int i =0; i < POOL_SIZE; i++){
            sCapturePreviewPool.release(new Capture());
        }
    }

    private final static CaptureBuilder sCapturePreviewBuilder = new CaptureBuilder();

    public static CaptureBuilder getInstance(){
        return sCapturePreviewBuilder;
    }

    public Capture buildCapture(final TotalCaptureResult result) {
        //Listener
        final Capture capture = sCapturePreviewPool.acquire();

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
                                capture.cameraState = Capture.STATE_READY;
                                capture.lightState = Capture.STATE_READY;
                                break;
                            case CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED:
                                capture.cameraState = Capture.STATE_NOT_READY;
                                capture.lightState = Capture.STATE_NOT_READY;
                                break;
                            default:
                                capture.cameraState = Capture.STATE_READY;
                                capture.lightState = Capture.STATE_NOT_AVAILABLE;
                        }
                    } else {
                        capture.cameraState = Capture.STATE_READY;
                        capture.lightState = Capture.STATE_NOT_AVAILABLE;
                    }

                    if (capture.cameraState == Capture.STATE_READY) {
                        final Integer awbValue = result.get(CaptureResult.CONTROL_AWB_STATE);
                        if (awbValue != null) {
                            switch (awbValue) {
                                case CaptureResult.CONTROL_AWB_STATE_INACTIVE:
                                case CaptureResult.CONTROL_AWB_STATE_LOCKED:
                                case CaptureResult.CONTROL_AWB_STATE_CONVERGED:
                                    capture.cameraState = Capture.STATE_READY;
                                    break;
                                default:
                                    capture.cameraState = Capture.STATE_NOT_READY;
                            }
                        } else {
                            capture.cameraState = Capture.STATE_READY;
                        }
                    }

                    if (capture.cameraState == Capture.STATE_READY) {
                        final Integer lensValue = result.get(CaptureResult.LENS_STATE);
                        if (lensValue != null) {
                            switch (lensValue) {
                                case CaptureResult.LENS_STATE_STATIONARY:
                                    capture.cameraState = Capture.STATE_READY;
                                    break;
                                default:
                                    capture.cameraState = Capture.STATE_NOT_READY;
                            }
                        } else {
                            capture.cameraState = Capture.STATE_READY;
                        }
                    }

                    break;
                default:
                    capture.cameraState = Capture.STATE_NOT_READY;
            }
        } else {
            capture.cameraState = Capture.STATE_NOT_AVAILABLE;
        }

        return capture;
    }

    public void releaseCapture(final Capture capture){
        capture.validationState = Capture.VALIDATION_IN_PROGRESS;
        sCapturePreviewPool.release(capture);
    }
}
