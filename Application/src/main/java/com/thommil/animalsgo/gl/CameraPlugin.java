package com.thommil.animalsgo.gl;


import android.util.Size;

import com.thommil.animalsgo.fragments.CameraFragment;

public abstract class CameraPlugin extends Plugin implements CameraFragment.OnViewportSizeUpdatedListener{

    private static final String TAG = "A_GO/CameraPlugin";

    @Override
    public int getType() {
        return TYPE_CAMERA;
    }

    public abstract int getCameraTextureId();

    public abstract void draw(final float[] cameraTransformMatrix);

}

