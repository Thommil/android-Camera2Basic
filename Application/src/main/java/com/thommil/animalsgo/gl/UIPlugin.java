package com.thommil.animalsgo.gl;


import android.content.res.AssetManager;

import com.thommil.animalsgo.gl.libgl.GlFloatRect;
import com.thommil.animalsgo.gl.libgl.GlIntRect;

public abstract class UIPlugin extends Plugin {

    private static final String TAG = "A_GO/UIPlugin";

    protected AssetManager mAssetManager;

    protected final GlIntRect mTargetCaptureZone = new GlIntRect();

    @Override
    public int getType() {
        return TYPE_UI;
    }

    public synchronized void setCaptureZone(final GlIntRect captureZone){
        mTargetCaptureZone.top = captureZone.top;
        mTargetCaptureZone.bottom = captureZone.bottom;
        mTargetCaptureZone.left = captureZone.left;
        mTargetCaptureZone.right = captureZone.right;
    }

    public void setAssetManager(final AssetManager assetManager){
        mAssetManager = assetManager;
    }

}
