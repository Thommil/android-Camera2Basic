package com.thommil.animalsgo.gl.libgl;

import android.util.Log;

import java.nio.FloatBuffer;

public class GlSprite extends GlBuffer<float[]> {

    private static final String TAG = "A_GO/GlSprite";

    public static final int CHUNK_VERTEX_INDEX = 0;
    public static final int CHUNK_TEXTURE_INDEX = 1;

    public static final int CHUNK_LEFT_TOP_X = 0;
    public static final int CHUNK_LEFT_TOP_Y = 1;
    public static final int CHUNK_LEFT_BOTTOM_X = 2;
    public static final int CHUNK_LEFT_BOTTOM_Y = 3;
    public static final int CHUNK_RIGHT_TOP_X = 4;
    public static final int CHUNK_RIGHT_TOP_Y = 5;
    public static final int CHUNK_RIGHT_BOTTOM_X = 6;
    public static final int CHUNK_RIGHT_BOTTOM_Y = 7;


    //Texture
    private final GlTexture mTexture;

    //Position
    private static final int POSITION_X = 0;
    private static final int POSITION_Y = 1;
    private static final int POSITION_WIDTH = 2;
    private static final int POSITION_HEIGHT = 3;
    private static final int POSITION_PIVOT_X = 4;
    private static final int POSITION_PIVOT_Y = 5;
    private static final int POSITION_LEFT = 6;
    private static final int POSITION_TOP = 7;
    private static final int POSITION_RIGHT = 8;
    private static final int POSITION_BOTTOM = 9;

    private float[] mPosition = new float[10];

    private boolean mMustUpdateVert = true;


    //Clip
    private static final int CLIP_LEFT = 0;
    private static final int CLIP_TOP = 1;
    private static final int CLIP_RIGHT = 2;
    private static final int CLIP_BOTTOM = 3;

    private final float[] mClip = new float[4];

    private boolean mMustUpdateText = true;


    public GlSprite(final GlTexture texture) {
        this(texture, 0, 0, texture.getWidth(), texture.getHeight());
    }

    public GlSprite(final GlTexture texture, final int srcX, final int srcY, final int srcWidth, final int srcHeight) {
        super(new GlBuffer.Chunk<>(new float[]{
                        -1.0f, 1.0f,    // left top
                        -1.0f, -1.0f,   // left bottom
                        1.0f, 1.0f,     // right top
                        1.0f, -1.0f     // right bottom
                },2),
                new GlBuffer.Chunk<>(new float[]{
                        0.0f,0.0f,      // left top //Bitmap coords
                        0.0f,1.0f,      // left bottom //Bitmap coords
                        1.0f,0.0f,      // right top //Bitmap coords
                        1.0f,1.0f       // right bottom //Bitmap coords
                },2));

        mTexture = texture;

        clip(srcX, srcY, srcWidth, srcHeight);
    }

    public GlSprite position(final float x, final float y){
        //Log.d(TAG,"position("+x+", "+y+")");
        mPosition[POSITION_X] = x;
        mPosition[POSITION_Y] = y;
        mPosition[POSITION_LEFT] = mPosition[POSITION_X] - mPosition[POSITION_PIVOT_X];
        mPosition[POSITION_TOP] = mPosition[POSITION_Y] + mPosition[POSITION_PIVOT_Y];
        mPosition[POSITION_RIGHT] = mPosition[POSITION_X] + mPosition[POSITION_PIVOT_X];
        mPosition[POSITION_BOTTOM] = mPosition[POSITION_Y] - mPosition[POSITION_PIVOT_Y];

        mMustUpdateVert = true;

        return this;
    }

    public GlSprite size(final float width, final float height){
        //Log.d(TAG,"size("+x+", "+y+")");
        mPosition[POSITION_WIDTH] = width;
        mPosition[POSITION_HEIGHT] = height;
        mPosition[POSITION_PIVOT_X] = width / 2;
        mPosition[POSITION_PIVOT_Y] = height / 2;
        mPosition[POSITION_LEFT] = mPosition[POSITION_X] - mPosition[POSITION_PIVOT_X];
        mPosition[POSITION_TOP] = mPosition[POSITION_Y] + mPosition[POSITION_PIVOT_Y];
        mPosition[POSITION_RIGHT] = mPosition[POSITION_X] + mPosition[POSITION_PIVOT_X];
        mPosition[POSITION_BOTTOM] = mPosition[POSITION_Y] - mPosition[POSITION_PIVOT_Y];

        mMustUpdateVert = true;

        return this;
    }

    public GlSprite clip(final int srcX, final int srcY, final int srcWidth, final int srcHeight){
        //Log.d(TAG,"clip("+srcX+", "+srcY+", "+srcWidth+", "+srcHeight+")");
        mClip[CLIP_LEFT] = (float)srcX / mTexture.getWidth();
        mClip[CLIP_TOP] = (float)srcY / mTexture.getHeight();
        mClip[CLIP_RIGHT] = (float)(srcX + srcWidth) / mTexture.getWidth();
        mClip[CLIP_BOTTOM] = (float)(srcY + srcHeight) / mTexture.getHeight();

        mMustUpdateText = true;

        return this;
    }


    @Override
    public GlBuffer commit(boolean push) {
        //Log.d(TAG,"commit("+push+")");
        if(this.buffer == null){
            this.buffer = ByteBufferPool.getInstance().getDirectFloatBuffer(20);
        }

        if(mMustUpdateVert){
            chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_X] = mPosition[POSITION_LEFT];
            chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_Y] = mPosition[POSITION_TOP];
            chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_X] = mPosition[POSITION_LEFT];
            chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_Y] = mPosition[POSITION_BOTTOM];
            chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_X] = mPosition[POSITION_RIGHT];
            chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_Y] = mPosition[POSITION_TOP];
            chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_X] = mPosition[POSITION_RIGHT];
            chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_Y] = mPosition[POSITION_BOTTOM];

            mMustUpdateVert = false;
        }

        if(mMustUpdateText){

            chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_TOP_X] = mClip[CLIP_LEFT];
            chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_TOP_Y] = mClip[CLIP_TOP];
            chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_BOTTOM_X] = mClip[CLIP_LEFT];
            chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_BOTTOM_Y] = mClip[CLIP_BOTTOM];
            chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_TOP_X] = mClip[CLIP_RIGHT];
            chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_TOP_Y] = mClip[CLIP_TOP];
            chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_BOTTOM_X] = mClip[CLIP_RIGHT];
            chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_BOTTOM_Y] = mClip[CLIP_BOTTOM];

            mMustUpdateText = false;
        }

        final FloatBuffer floatBuffer = (FloatBuffer)this.buffer;
        floatBuffer.position(0);
        floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_X]);
        floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_Y]);
        floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_TOP_X]);
        floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_TOP_Y]);

        floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_X]);
        floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_Y]);
        floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_BOTTOM_X]);
        floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_BOTTOM_Y]);

        floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_X]);
        floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_Y]);
        floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_TOP_X]);
        floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_TOP_Y]);

        floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_X]);
        floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_Y]);
        floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_BOTTOM_X]);
        floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_BOTTOM_Y]);

        //Update server if needed
        if(push){
            push();
        }

        return this;
    }

    @Override
    public GlBuffer commit(Chunk<float[]>[] chunks) {
        throw new RuntimeException("Only full commit is supported by GlSprite");
    }

    @Override
    public GlBuffer commit(Chunk<float[]>[] chunks, boolean push) {
        throw new RuntimeException("Only full commit is supported by GlSprite");
    }

    @Override
    public GlBuffer commit(Chunk<float[]> chunk) {
        throw new RuntimeException("Only full commit is supported by GlSprite");
    }

    @Override
    public GlBuffer commit(Chunk<float[]> chunk, boolean push) {
        throw new RuntimeException("Only full commit is supported by GlSprite");
    }
}
