package com.thommil.animalsgo.gl;

import com.thommil.animalsgo.gl.libgl.GlBuffer;
import com.thommil.animalsgo.gl.libgl.GlDrawableBuffer;
import com.thommil.animalsgo.gl.libgl.GlIntRect;
import com.thommil.animalsgo.gl.libgl.GlOperation;
import com.thommil.animalsgo.gl.libgl.GlTexture;

public abstract class CameraPlugin extends Plugin{

    private static final String TAG = "A_GO/CameraPlugin";

    public static final int TEXTURE_INDEX = 1;

    public static final int ZOOM_STATE_NONE = 0x00;
    public static final int ZOOM_STATE_IN = 0x01;
    public static final int ZOOM_STATE_OUT = 0x02;
    public static final int ZOOM_STATE_RESET = 0x04;

    protected final GlBuffer.Chunk<float[]> mVertChunk =
            new GlBuffer.Chunk<>(new float[]{
                    -1.0f, 1.0f,    // left top
                    -1.0f, -1.0f,   // left bottom
                    1.0f, 1.0f,     // right top
                    1.0f, -1.0f     // right bottom
            },2);

    protected final GlBuffer.Chunk<float[]> mTextChunk =
            new GlBuffer.Chunk<>(new float[]{
                    0.0f,1.0f,
                    0.0f,0.0f,
                    1.0f,1.0f,
                    1.0f,0.0f
            },2);

    protected GlDrawableBuffer<float[]> mCameraPreviewBuffer;

    protected float[] mCameraTransformMatrix;

    protected int mZoomState = ZOOM_STATE_NONE;
    protected float mCurrentZoom = 1.0f;

    @Override
    public int getType() {
        return TYPE_CAMERA;
    }


    @Override
    public void allocate(final float surfaceRatio) {
        super.allocate(surfaceRatio);
        this.mZoomState = ZOOM_STATE_NONE;
        mCurrentZoom = 1.0f;

        //Buffer
        mCameraPreviewBuffer = new GlDrawableBuffer<>(mVertChunk, mTextChunk);
        mCameraPreviewBuffer.commit();
        applyZoom();
    }

    @Override
    public void draw(GlIntRect viewport, int orientation) {
        GlOperation.setTestState(GlOperation.TEST_BLEND, false);

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
        mVertChunk.data[0] = mVertChunk.data[2]
                = mVertChunk.data[3] = mVertChunk.data[7] = -mCurrentZoom;
        mVertChunk.data[1] = mVertChunk.data[4]
                = mVertChunk.data[5] = mVertChunk.data[6] = mCurrentZoom;

        mCameraPreviewBuffer.commit(mVertChunk);
    }

    @Override
    public void free() {
        super.free();
        mCurrentZoom = 1.0f;
        if(mCameraPreviewBuffer != null) {
            mCameraPreviewBuffer.free();
            mCameraPreviewBuffer = null;
        }
    }

    public abstract GlTexture getCameraTexture();

    public void setCameraTransformMatrix(final float[] cameraTransformMatrix){
        mCameraTransformMatrix = cameraTransformMatrix;
    }

    public void setZoomState(final int zoomState){
        mZoomState = zoomState;
    }
}

