package com.thommil.animalsgo.gl;

import android.opengl.Matrix;

import com.thommil.animalsgo.Settings;
import com.thommil.animalsgo.gl.libgl.GlBuffer;
import com.thommil.animalsgo.gl.libgl.GlIntRect;
import com.thommil.animalsgo.gl.libgl.GlTexture;

public abstract class CameraPlugin extends Plugin{

    private static final String TAG = "A_GO/CameraPlugin";

    public static final int ZOOM_STATE_NONE = 0x00;
    public static final int ZOOM_STATE_IN = 0x01;
    public static final int ZOOM_STATE_OUT = 0x02;
    public static final int ZOOM_STATE_RESET = 0x04;

    protected final GlBuffer.Chunk<float[]> mCameraPreviewVertChunk =
            new GlBuffer.Chunk<>(new float[]{
                    -1.0f,-1.0f,
                    -1.0f,1.0f,
                    1.0f,-1.0f,
                    1.0f,1.0f
            },2);

    protected GlBuffer<float[]> mCameraPreviewBuffer;

    protected float[] mCameraTransformMatrix;

    protected int mZoomState = ZOOM_STATE_NONE;
    protected float mCurrentZoom = 1.0f;
    private boolean mIsZoomDirty = false;

    @Override
    public int getType() {
        return TYPE_CAMERA;
    }


    @Override
    public void create() {
        super.create();
        this.mZoomState = ZOOM_STATE_NONE;
        mCurrentZoom = 1.0f;
        mCameraPreviewBuffer = new GlBuffer<>(new GlBuffer.Chunk[]{mCameraPreviewVertChunk, SQUARE_IMAGE_TEXT_CHUNK});
        applyZoom();
    }

    @Override
    public void draw(GlIntRect viewport, int orientation) {
        //TODO Transform matrix when android < 6 (using accelerometer)

        if(mZoomState > ZOOM_STATE_NONE) {
            switch (mZoomState) {
                case ZOOM_STATE_RESET:
                    mCurrentZoom = 1.0f;
                    break;
                case ZOOM_STATE_IN:
                    mCurrentZoom += (mCurrentZoom / com.thommil.animalsgo.Settings.ZOOM_VELOCITY);
                    mCurrentZoom = Math.min(com.thommil.animalsgo.Settings.ZOOM_MAX, mCurrentZoom);
                    break;
                case ZOOM_STATE_OUT:
                    mCurrentZoom -= (mCurrentZoom /com.thommil.animalsgo.Settings.ZOOM_VELOCITY);
                    mCurrentZoom = Math.max(1f, mCurrentZoom);
                    break;
            }
            applyZoom();
        }
    }

    private void applyZoom(){
        mCameraPreviewVertChunk.data[0] = mCameraPreviewVertChunk.data[1]
                = mCameraPreviewVertChunk.data[2] = mCameraPreviewVertChunk.data[5] = -mCurrentZoom;
        mCameraPreviewVertChunk.data[3] = mCameraPreviewVertChunk.data[4]
                = mCameraPreviewVertChunk.data[6] = mCameraPreviewVertChunk.data[7] = mCurrentZoom;

        mCameraPreviewBuffer.update(0);
    }

    @Override
    public void free() {
        super.free();
        mCameraPreviewBuffer.free();
    }

    public abstract GlTexture getCameraTexture();

    public void setCameraTransformMatrix(final float[] cameraTransformMatrix){
        mCameraTransformMatrix = cameraTransformMatrix;
    }

    public void setZoomState(final int zoomState){
        mZoomState = zoomState;
    }
}

