package com.thommil.animalsgo.gl;

import com.thommil.animalsgo.gl.libgl.GlIntRect;
import com.thommil.animalsgo.gl.libgl.GlTexture;

public abstract class CameraPlugin extends Plugin{

    private static final String TAG = "A_GO/CameraPlugin";

    protected float[] mCameraTransformMatrix;

    @Override
    public int getType() {
        return TYPE_CAMERA;
    }

    public abstract GlTexture getCameraTexture();

    public void setCameraTransformMatrix(final float[] cameraTransformMatrix){
        mCameraTransformMatrix = cameraTransformMatrix;
    }

}

