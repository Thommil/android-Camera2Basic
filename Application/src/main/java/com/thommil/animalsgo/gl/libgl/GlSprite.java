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

    private boolean mMustUpdate = true;

    //Texture
    private final GlTexture mTexture;

    //Position
    private static final int POSITION_X = 0;
    private static final int POSITION_Y = 1;
    private static final int POSITION_WIDTH = 2;
    private static final int POSITION_HEIGHT = 3;
    private static final int POSITION_PIVOT_X = 4;
    private static final int POSITION_PIVOT_Y = 5;

    private final float[] mPosition = new float[6];


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

        chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_X]
                = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_X] = mPosition[POSITION_X] - mPosition[POSITION_PIVOT_X];

        chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_Y]
                = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_Y] = mPosition[POSITION_Y] + mPosition[POSITION_PIVOT_Y];

        chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_X]
                = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_X] = mPosition[POSITION_X] + mPosition[POSITION_PIVOT_X];

        chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_Y]
                = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_Y] = mPosition[POSITION_Y] - mPosition[POSITION_PIVOT_Y];

        mMustUpdate = true;

        return this;
    }

    public GlSprite translate(final float dx, final float dy){
        //Log.d(TAG,"position("+x+", "+y+")");
        mPosition[POSITION_X] += dx;
        mPosition[POSITION_Y] += dy;

        chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_X]
                = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_X] = mPosition[POSITION_X] - mPosition[POSITION_PIVOT_X];

        chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_Y]
                = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_Y] = mPosition[POSITION_Y] + mPosition[POSITION_PIVOT_Y];

        chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_X]
                = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_X] = mPosition[POSITION_X] + mPosition[POSITION_PIVOT_X];

        chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_Y]
                = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_Y] = mPosition[POSITION_Y] - mPosition[POSITION_PIVOT_Y];

        mMustUpdate = true;

        return this;
    }

    public GlSprite size(final float width, final float height){
        //Log.d(TAG,"size("+x+", "+y+")");
        mPosition[POSITION_WIDTH] = width;
        mPosition[POSITION_HEIGHT] = height;
        mPosition[POSITION_PIVOT_X] = mPosition[POSITION_WIDTH] / 2;
        mPosition[POSITION_PIVOT_Y] = mPosition[POSITION_HEIGHT] / 2;

        chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_X]
                = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_X] = mPosition[POSITION_X] - mPosition[POSITION_PIVOT_X];

        chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_Y]
                = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_Y] = mPosition[POSITION_Y] + mPosition[POSITION_PIVOT_Y];

        chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_X]
                = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_X] = mPosition[POSITION_X] + mPosition[POSITION_PIVOT_X];

        chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_Y]
                = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_Y] = mPosition[POSITION_Y] - mPosition[POSITION_PIVOT_Y];

        mMustUpdate = true;

        return this;
    }

    public GlSprite scale(final float xFactor, final float yFactor){
        //Log.d(TAG,"size("+x+", "+y+")");
        mPosition[POSITION_WIDTH] = mPosition[POSITION_WIDTH] * xFactor;
        mPosition[POSITION_HEIGHT] = mPosition[POSITION_HEIGHT] * yFactor;
        mPosition[POSITION_PIVOT_X] = mPosition[POSITION_WIDTH] / 2;
        mPosition[POSITION_PIVOT_Y] = mPosition[POSITION_HEIGHT] / 2;

        chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_X]
                = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_X] = mPosition[POSITION_X] - mPosition[POSITION_PIVOT_X];

        chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_Y]
                = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_Y] = mPosition[POSITION_Y] + mPosition[POSITION_PIVOT_Y];

        chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_X]
                = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_X] = mPosition[POSITION_X] + mPosition[POSITION_PIVOT_X];

        chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_Y]
                = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_Y] = mPosition[POSITION_Y] - mPosition[POSITION_PIVOT_Y];

        mMustUpdate = true;

        return this;
    }

    public GlSprite clip(final int srcX, final int srcY, final int srcWidth, final int srcHeight){
        //Log.d(TAG,"clip("+srcX+", "+srcY+", "+srcWidth+", "+srcHeight+")");
        chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_TOP_X]
                = chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_BOTTOM_X] = (float)srcX / mTexture.getWidth();

        chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_TOP_Y]
                = chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_TOP_Y] = (float)srcY / mTexture.getHeight();

        chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_TOP_X]
                = chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_BOTTOM_X] = (float)(srcX + srcWidth) / mTexture.getWidth();

        chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_BOTTOM_Y]
                = chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_BOTTOM_Y] = (float)(srcY + srcHeight) / mTexture.getHeight();

        mMustUpdate = true;

        return this;
    }

    public GlSprite flip(final boolean flipX, final boolean flipY){
        //Log.d(TAG,"size("+x+", "+y+")");
        float tmpPos;
        if(flipX){
            tmpPos = chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_TOP_X];
            chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_TOP_X]
                    = chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_BOTTOM_X]
                    = chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_TOP_X];

            chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_TOP_X]
                    = chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_BOTTOM_X]
                    = tmpPos;
        }
        if(flipY){
            tmpPos = chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_TOP_Y];
            chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_TOP_Y]
                    = chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_TOP_Y]
                    = chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_BOTTOM_Y];

            chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_BOTTOM_Y]
                    = chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_BOTTOM_Y]
                    = tmpPos;

        }

        mMustUpdate = true;

        return this;
    }


    @Override
    public GlBuffer commit(boolean push) {
        //Log.d(TAG,"commit("+push+")");
        if(this.buffer == null){
            this.buffer = ByteBufferPool.getInstance().getDirectFloatBuffer(20);
        }

        if(mMustUpdate) {
            final FloatBuffer floatBuffer = (FloatBuffer) this.buffer;
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
            if (push) {
                push();
            }
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
