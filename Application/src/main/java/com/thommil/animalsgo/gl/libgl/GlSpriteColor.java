package com.thommil.animalsgo.gl.libgl;

import android.opengl.GLES20;

import com.thommil.animalsgo.utils.ByteBufferPool;

import java.nio.FloatBuffer;

public class GlSpriteColor extends GlSprite {

    private static final String TAG = "A_GO/GlSpriteColor";

    public static final int CHUNK_COLOR_INDEX = 2;

    public GlSpriteColor(final GlTexture texture) {
        this(texture, 0, 0, texture.getWidth(), texture.getHeight());
    }

    public GlSpriteColor(final GlTexture texture, final int srcX, final int srcY, final int srcWidth, final int srcHeight) {
        super(new Chunk<>(new float[]{
                        -1.0f, 1.0f,    // left top
                        -1.0f, -1.0f,   // left bottom
                        1.0f, 1.0f,     // right top
                        1.0f, -1.0f     // right bottom
                }, 2),
                new Chunk<>(new float[]{
                        0.0f, 0.0f,      // left top //Bitmap coords
                        0.0f, 1.0f,      // left bottom //Bitmap coords
                        1.0f, 0.0f,      // right top //Bitmap coords
                        1.0f, 1.0f       // right bottom //Bitmap coords
                }, 2),
                new Chunk<>(new float[]{
                        GlColor.WHITE,      // left top //Bitmap coords
                        GlColor.WHITE,      // left bottom //Bitmap coords
                        GlColor.WHITE,      // right top //Bitmap coords
                        GlColor.WHITE       // right bottom //Bitmap coords
                }, 1));

        mTexture = texture;

        //Hack to hide float behind vec4 bytes
        chunks[2].datatype = TYPE_BYTE;
        chunks[2].normalized = true;
        chunks[2].components = 4;
        chunks[2].offset = 16;

        clip(srcX, srcY, srcWidth, srcHeight);
    }


    @Override
    public GlBuffer commit(boolean push) {
        //Log.d(TAG,"commit("+push+")");
        if (this.buffer == null) {
            this.buffer = ByteBufferPool.getInstance().getDirectFloatBuffer(20);
            mManagedBuffer = true;
        }

        if (mMustUpdate) {
            final FloatBuffer floatBuffer = (FloatBuffer) this.buffer;
            if(mManagedBuffer){
                floatBuffer.position(0);
            }
            floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_X]);
            floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_Y]);
            floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_TOP_X]);
            floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_TOP_Y]);
            floatBuffer.put(chunks[CHUNK_COLOR_INDEX].data[0]);

            floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_X]);
            floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_Y]);
            floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_BOTTOM_X]);
            floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_BOTTOM_Y]);
            floatBuffer.put(chunks[CHUNK_COLOR_INDEX].data[1]);

            floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_X]);
            floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_Y]);
            floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_TOP_X]);
            floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_TOP_Y]);
            floatBuffer.put(chunks[CHUNK_COLOR_INDEX].data[2]);

            floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_X]);
            floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_Y]);
            floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_BOTTOM_X]);
            floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_BOTTOM_Y]);
            floatBuffer.put(chunks[CHUNK_COLOR_INDEX].data[3]);

            //Update server if needed
            if (push) {
                push();
            }
        }

        return this;
    }

    public GlSpriteColor setColor(final float r, final float g, final float b, final float a){
        final float fColor = GlColor.toFloatBits(r, g, b, a);
        chunks[CHUNK_COLOR_INDEX].data[0] = fColor;
        chunks[CHUNK_COLOR_INDEX].data[1] = fColor;
        chunks[CHUNK_COLOR_INDEX].data[2] = fColor;
        chunks[CHUNK_COLOR_INDEX].data[3] = fColor;

        mMustUpdate = true;

        return this;
    }

    @Override
    public void draw(GlProgram program) {
        switch (this.mode){
            case GlBuffer.MODE_VAO: {
                bind();

                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

                unbind();
                break;
            }
            case GlBuffer.MODE_VBO: {
                program.enableAttributes();

                this.bind();

                if(this.vertexAttribHandles != null) {
                    GLES20.glVertexAttribPointer(this.vertexAttribHandles[CHUNK_VERTEX_INDEX], 2, GlBuffer.TYPE_FLOAT, false, 20, 0);
                    GLES20.glVertexAttribPointer(this.vertexAttribHandles[CHUNK_TEXTURE_INDEX], 2, GlBuffer.TYPE_FLOAT, false, 20, 8);
                    GLES20.glVertexAttribPointer(this.vertexAttribHandles[CHUNK_COLOR_INDEX], 4, GlBuffer.TYPE_BYTE, true, 20, 9);
                }

                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

                unbind();
                program.disableAttributes();
                break;
            }
            default: {
                program.enableAttributes();

                if(this.vertexAttribHandles != null) {
                    this.buffer.position(0);
                    GLES20.glVertexAttribPointer(this.vertexAttribHandles[CHUNK_VERTEX_INDEX], 2, GlBuffer.TYPE_FLOAT, false, 20, this.buffer);
                    this.buffer.position(2);
                    GLES20.glVertexAttribPointer(this.vertexAttribHandles[CHUNK_TEXTURE_INDEX], 2, GlBuffer.TYPE_FLOAT, false, 20, this.buffer);
                    this.buffer.position(4);
                    GLES20.glVertexAttribPointer(this.vertexAttribHandles[CHUNK_COLOR_INDEX], 4, GlBuffer.TYPE_BYTE, true, 20, this.buffer);
                }

                this.buffer.position(0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

                program.disableAttributes();
            }
        }
    }
}
