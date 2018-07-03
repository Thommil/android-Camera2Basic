package com.thommil.animalsgo.gl;

import com.thommil.animalsgo.gl.libgl.GlIntRect;

public abstract class CameraPlugin extends Plugin{

    private static final String TAG = "A_GO/CameraPlugin";

    protected float[] mCameraTransformMatrix;

    @Override
    public int getType() {
        return TYPE_CAMERA;
    }

    public abstract int getCameraTextureId();

    public void setCameraTransformMatrix(final float[] cameraTransformMatrix){
        mCameraTransformMatrix = cameraTransformMatrix;
    }

}

