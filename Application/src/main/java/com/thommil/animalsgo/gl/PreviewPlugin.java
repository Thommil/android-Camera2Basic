package com.thommil.animalsgo.gl;

import com.thommil.animalsgo.gl.libgl.GlTexture;

public abstract class PreviewPlugin extends Plugin {

    private static final String TAG = "A_GO/PreviewPlugin";

    protected GlTexture mCameraTexture;

    @Override
    public int getType() {
        return TYPE_PREVIEW;
    }

    public void setCameraTexture(final GlTexture cameraTexture){
        mCameraTexture = cameraTexture;
    }
}
