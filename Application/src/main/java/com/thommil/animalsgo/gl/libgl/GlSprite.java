package com.thommil.animalsgo.gl.libgl;

import android.opengl.GLES20;

import com.thommil.animalsgo.utils.ByteBufferPool;
import com.thommil.animalsgo.utils.MathUtils;

import java.nio.FloatBuffer;

//TODO rotate
public class GlSprite extends GlDrawableBuffer<float[]> {

    private static final String TAG = "A_GO/GlSprite";

    protected static final int CHUNK_VERTEX_INDEX = 0;
    protected static final int CHUNK_TEXTURE_INDEX = 1;

    protected static final int CHUNK_LEFT_TOP = 0;
    protected static final int CHUNK_LEFT_BOTTOM = 1;
    protected static final int CHUNK_RIGHT_TOP = 2;
    protected static final int CHUNK_RIGHT_BOTTOM = 3;

    protected static final int CHUNK_LEFT_TOP_X = 0;
    protected static final int CHUNK_LEFT_TOP_Y = 1;
    protected static final int CHUNK_LEFT_BOTTOM_X = 2;
    protected static final int CHUNK_LEFT_BOTTOM_Y = 3;
    protected static final int CHUNK_RIGHT_TOP_X = 4;
    protected static final int CHUNK_RIGHT_TOP_Y = 5;
    protected static final int CHUNK_RIGHT_BOTTOM_X = 6;
    protected static final int CHUNK_RIGHT_BOTTOM_Y = 7;

    protected boolean mMustUpdate = true;

    //Texture
    protected GlTexture mTexture;

    //Position
    public float x;
    public float y;

    public float width;
    public float height;

    public float pivotX;
    public float pivotY;

    public float rotation = 0f;


    public GlSprite(final GlTexture texture) {
        this(texture, 0, 0, texture.getWidth(), texture.getHeight());
    }

    public GlSprite(final GlTexture texture, final int srcX, final int srcY, final int srcWidth, final int srcHeight) {
        super(new GlBuffer.Chunk<>(new float[]{
                        -1.0f, 1.0f,    // left top
                        -1.0f, -1.0f,   // left bottom
                        1.0f, 1.0f,     // right top
                        1.0f, -1.0f     // right bottom
                }, 2),
                new GlBuffer.Chunk<>(new float[]{
                        0.0f, 0.0f,      // left top //Bitmap coords
                        0.0f, 1.0f,      // left bottom //Bitmap coords
                        1.0f, 0.0f,      // right top //Bitmap coords
                        1.0f, 1.0f       // right bottom //Bitmap coords
                }, 2));

        mTexture = texture;

        clip(srcX, srcY, srcWidth, srcHeight);
    }

    protected GlSprite(Chunk<float[]> ...chunks){
        super(chunks);
    }

    public GlSprite position(final float x, final float y) {
        //Log.d(TAG,"position("+x+", "+y+")");
        this.x = x;
        this.y = y;

        mMustUpdate = true;

        return this;
    }

    public GlSprite rotation(final float deg) {
        //Log.d(TAG,"rotation("+deg+")");
        rotation = deg;
        if(rotation > 360){
            rotation -= 360f;
        }

        mMustUpdate = true;

        return this;
    }

    public GlSprite rotate(final float deg) {
        //Log.d(TAG,"rotate("+deg+")");
        rotation += deg;
        if(rotation > 360){
            rotation -= 360f;
        }

        mMustUpdate = true;

        return this;
    }

    public GlSprite translate(final float dx, final float dy) {
        //Log.d(TAG,"translate("+dx+", "+dy+")");
        this.x += dx;
        this.y += dy;

        mMustUpdate = true;

        return this;
    }

    public GlSprite size(final float width, final float height) {
        //Log.d(TAG,"size("+x+", "+y+")");
        this.width = width;
        this.height = height;
        this.pivotX = this.width / 2;
        this.pivotY = this.height / 2;

        mMustUpdate = true;

        return this;
    }

    public GlSprite scale(final float xFactor, final float yFactor) {
        //Log.d(TAG,"scale("+xFactor+", "+yFactor+")");
        this.width = this.width * xFactor;
        this.height = this.height * yFactor;
        this.pivotX = this.width / 2;
        this.pivotY = this.height / 2;

        mMustUpdate = true;

        return this;
    }

    public GlSprite clip(final int srcX, final int srcY, final int srcWidth, final int srcHeight) {
        //Log.d(TAG,"clip("+srcX+", "+srcY+", "+srcWidth+", "+srcHeight+")");
        chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_TOP_X]
                = chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_BOTTOM_X] = (float) srcX / mTexture.getWidth();

        chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_TOP_Y]
                = chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_TOP_Y] = (float) srcY / mTexture.getHeight();

        chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_TOP_X]
                = chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_BOTTOM_X] = (float) (srcX + srcWidth) / mTexture.getWidth();

        chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_BOTTOM_Y]
                = chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_BOTTOM_Y] = (float) (srcY + srcHeight) / mTexture.getHeight();

        mMustUpdate = true;

        return this;
    }

    public GlSprite flip(final boolean flipX, final boolean flipY) {
        //Log.d(TAG,"flip("+flipX+", "+flipY+")");
        float tmpPos;
        if (flipX) {
            tmpPos = chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_TOP_X];
            chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_TOP_X]
                    = chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_LEFT_BOTTOM_X]
                    = chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_TOP_X];

            chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_TOP_X]
                    = chunks[CHUNK_TEXTURE_INDEX].data[CHUNK_RIGHT_BOTTOM_X]
                    = tmpPos;
        }
        if (flipY) {
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

    protected void commitVertices(){
        final float left = this.x - this.pivotX;
        final float top = this.y + this.pivotY;
        final float right = this.x + this.pivotX;
        final float bottom = this.y - this.pivotY;

        if(rotation != 0){
            switch((int)rotation){
                case 90:
                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_X] = left;
                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_Y] = bottom;

                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_X] = right;
                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_Y] = bottom;

                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_X] = left;
                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_Y] = top;

                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_X] = right;
                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_Y] = top;
                    break;
                case 180:
                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_X] = right;
                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_Y] = bottom;

                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_X] = top;
                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_Y] = right;

                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_X] = left;
                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_Y] = bottom;

                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_X] = left;
                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_Y] = top;
                    break;
                case 270:
                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_X] = right;
                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_Y] = top;

                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_X] = left;
                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_Y] = top;

                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_X] = right;
                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_Y] = bottom;

                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_X] = left;
                    chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_Y] = bottom;
                    break;
                case 360:
                    //PASS
                    break;
                default:
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
        }
        else{
            chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_X]
                    = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_X] = left;

            chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_TOP_Y]
                    = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_Y] = top;

            chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_TOP_X]
                    = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_X] = right;

            chunks[CHUNK_VERTEX_INDEX].data[CHUNK_LEFT_BOTTOM_Y]
                    = chunks[CHUNK_VERTEX_INDEX].data[CHUNK_RIGHT_BOTTOM_Y] = bottom;
        }
    }

    @Override
    public synchronized GlBuffer commit(boolean push) {
        //Log.d(TAG,"commit("+push+")");
        if (this.buffer == null) {
            this.buffer = ByteBufferPool.getInstance().getDirectFloatBuffer(20);
            mManagedBuffer = true;
        }

        if (mMustUpdate) {
            commitVertices();

            final FloatBuffer floatBuffer = (FloatBuffer) this.buffer;
            if(mManagedBuffer){
                floatBuffer.position(0);
            }
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
                    GLES20.glVertexAttribPointer(this.vertexAttribHandles[CHUNK_VERTEX_INDEX], 2, GlBuffer.TYPE_FLOAT, false, 16, 0);
                    GLES20.glVertexAttribPointer(this.vertexAttribHandles[CHUNK_TEXTURE_INDEX], 2, GlBuffer.TYPE_FLOAT, false, 16, 8);
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
                    GLES20.glVertexAttribPointer(this.vertexAttribHandles[CHUNK_VERTEX_INDEX], 2, GlBuffer.TYPE_FLOAT, false, 16, this.buffer);
                    this.buffer.position(2);
                    GLES20.glVertexAttribPointer(this.vertexAttribHandles[CHUNK_TEXTURE_INDEX], 2, GlBuffer.TYPE_FLOAT, false, 16, this.buffer);
                }

                this.buffer.position(0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

                program.disableAttributes();
            }
        }
    }
}
