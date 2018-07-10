package com.thommil.animalsgo.gl;

import com.thommil.animalsgo.gl.libgl.GlTexture;

public abstract class PreviewPlugin extends Plugin {

    private static final String TAG = "A_GO/PreviewPlugin";

    public static final int TEXTURE_INDEX = 2;

    protected GlTexture mSourceTexture;

    @Override
    public int getType() {
        return TYPE_PREVIEW;
    }

    public void setSourceTexture(final GlTexture sourceTexture){
        mSourceTexture = sourceTexture;
    }


}
