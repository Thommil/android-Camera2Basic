package com.thommil.animalsgo.gl.libgl;

import android.util.Log;

import java.nio.FloatBuffer;

public class GlSprite extends GlBuffer<float[]> {

    private static final String TAG = "A_GO/GlSprite";

    public static final int CHUNK_VERTEX_INDEX = 0;
    public static final int CHUNK_TEXTURE_INDEX = 1;

    private final GlTexture mTexture;

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
        updateClip(srcX, srcY, srcWidth, srcHeight);
    }

    private void updateClip(final int srcX, final int srcY, final int srcWidth, final int srcHeight){
        //Log.d(TAG,"buildTextCoord("+srcX+", "+srcY+", "+srcWidth+", "+srcHeight+")");
        final float top = (float)srcY / mTexture.getHeight();
        final float left = (float)srcX / mTexture.getWidth();
        final float bottom = (float)(srcY + srcHeight) / mTexture.getHeight();
        final float right = (float)(srcX + srcWidth) / mTexture.getWidth();


        chunks[CHUNK_TEXTURE_INDEX].data[0] = left;
        chunks[CHUNK_TEXTURE_INDEX].data[1] = top;
        chunks[CHUNK_TEXTURE_INDEX].data[2] = left;
        chunks[CHUNK_TEXTURE_INDEX].data[3] = bottom;
        chunks[CHUNK_TEXTURE_INDEX].data[4] = right;
        chunks[CHUNK_TEXTURE_INDEX].data[5] = top;
        chunks[CHUNK_TEXTURE_INDEX].data[6] = right;
        chunks[CHUNK_TEXTURE_INDEX].data[7] = bottom;
    }


    @Override
    public GlBuffer commit(boolean push) {
        //Log.d(TAG,"commit("+push+")");
        if(this.buffer == null){
            this.buffer = ByteBufferPool.getInstance().getDirectFloatBuffer(20);
        }

        final FloatBuffer floatBuffer = (FloatBuffer)this.buffer;
        floatBuffer.position(0);
        floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[0]);
        floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[1]);
        floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[0]);
        floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[1]);

        floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[2]);
        floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[3]);
        floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[2]);
        floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[3]);

        floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[4]);
        floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[5]);
        floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[4]);
        floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[5]);

        floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[6]);
        floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[7]);
        floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[6]);
        floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[7]);

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
