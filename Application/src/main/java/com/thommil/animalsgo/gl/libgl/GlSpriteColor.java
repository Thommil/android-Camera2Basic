package com.thommil.animalsgo.gl.libgl;

import android.opengl.GLES20;

import com.thommil.animalsgo.utils.ByteBufferPool;
import com.thommil.animalsgo.utils.MathUtils;

import java.nio.FloatBuffer;

public class GlSpriteColor extends GlSprite {

    private static final String TAG = "A_GO/GlSpriteColor";

    public static final int CHUNK_COLOR_INDEX = 2;

    public float color = GlColor.WHITE;

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

    public GlSpriteColor setColor(final float r, final float g, final float b, final float a){
        this.color = GlColor.toFloatBits(r, g, b, a);
        chunks[CHUNK_COLOR_INDEX].data[CHUNK_LEFT_TOP] = this.color;
        chunks[CHUNK_COLOR_INDEX].data[CHUNK_LEFT_BOTTOM] = this.color;
        chunks[CHUNK_COLOR_INDEX].data[CHUNK_RIGHT_TOP] = this.color;
        chunks[CHUNK_COLOR_INDEX].data[CHUNK_RIGHT_BOTTOM] = this.color;

        mMustUpdate = true;

        return this;
    }

    @Override
    public GlBuffer commit(boolean push) {
        //Log.d(TAG,"commit("+push+")");
        if (this.buffer == null) {
            this.buffer = ByteBufferPool.getInstance().getDirectFloatBuffer(20);
            mManagedBuffer = true;
        }

        if (mMustUpdate) {
            if(rotation != 0){
                final float localX = -this.pivotX;
                final float localY = this.pivotY;
                final float localX2 = localX + this.width;
                final float localY2 = localY - this.height;

                final float cos = MathUtils.cosDeg(rotation);
                final float sin = MathUtils.sinDeg(rotation);
                final float localXCos = localX * cos;
                final float localXSin = localX * sin;
                final float localYCos = localY * cos;
                final float localYSin = localY * sin;
                final float localX2Cos = localX2 * cos;
                final float localX2Sin = localX2 * sin;
                final float localY2Cos = localY2 * cos;
                final float localY2Sin = localY2 * sin;

                chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_X] = localXCos - localYSin;
                chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_Y] = localXSin + localYCos;

                chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_X] = localXCos - localY2Sin;
                chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_Y] = localXSin + localY2Cos;

                chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_X] = localX2Cos - localYSin;
                chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_Y] = localX2Sin + localYCos;

                chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_X] = localX2Cos - localY2Sin;
                chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_Y] = localX2Sin + localY2Cos;


            }
            else{
                chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_X]
                        = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_X] = this.x - this.pivotX; //left

                chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_Y]
                        = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_Y] = this.y + this.pivotY; //top

                chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_X]
                        = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_X] = this.x + this.pivotX; //right

                chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_Y]
                        = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_Y] = this.y - this.pivotY; //bottom
            }

            final FloatBuffer floatBuffer = (FloatBuffer) this.buffer;
            if(mManagedBuffer){
                floatBuffer.position(0);
            }
            floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_X]);
            floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_Y]);
            floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_TOP_X]);
            floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_TOP_Y]);
            floatBuffer.put(chunks[CHUNK_COLOR_INDEX].data[CHUNK_LEFT_TOP]);

            floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_X]);
            floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_Y]);
            floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_BOTTOM_X]);
            floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_BOTTOM_Y]);
            floatBuffer.put(chunks[CHUNK_COLOR_INDEX].data[CHUNK_LEFT_BOTTOM]);

            floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_X]);
            floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_Y]);
            floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_TOP_X]);
            floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_TOP_Y]);
            floatBuffer.put(chunks[CHUNK_COLOR_INDEX].data[CHUNK_RIGHT_TOP]);

            floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_X]);
            floatBuffer.put(chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_Y]);
            floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_BOTTOM_X]);
            floatBuffer.put(chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_BOTTOM_Y]);
            floatBuffer.put(chunks[CHUNK_COLOR_INDEX].data[CHUNK_RIGHT_BOTTOM]);

            //Update server if needed
            if (push) {
                push();
            }

            mMustUpdate = false;
        }

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
