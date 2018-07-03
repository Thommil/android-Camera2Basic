package com.thommil.animalsgo.gl;


import com.thommil.animalsgo.gl.libgl.GlIntRect;

public abstract class PreviewPlugin extends Plugin {

    private static final String TAG = "A_GO/PreviewPlugin";

    protected int mCameraTextureId;

    @Override
    public int getType() {
        return TYPE_PREVIEW;
    }

    public void setCameraTextureId(final int cameraTextureId){
        mCameraTextureId = cameraTextureId;
    }
}
