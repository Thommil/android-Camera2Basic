package com.thommil.animalsgo.gl.plugins;

import android.opengl.GLES20;

import com.thommil.animalsgo.R;
import com.thommil.animalsgo.gl.PreviewPlugin;
import com.thommil.animalsgo.gl.libgl.GlBuffer;
import com.thommil.animalsgo.gl.libgl.GlIntRect;
import com.thommil.animalsgo.utils.ByteBufferPool;

import java.nio.FloatBuffer;

public class Basic extends PreviewPlugin {

    private static final String TAG = "A_GO/Plugin/Basic";

    private static final String ID = "basic";

    private int mPositionParamHandle;
    private int mTextureCoordinateParamHandle;
    private int mTextureParamHandle;

    private static final float[]VERTEX_COORDS = new float[]{ -1.0f,-1.0f,-1.0f,1.0f, 1.0f,-1.0f, 1.0f,1.0f};
    private static final float[]TEXTURE_COORDS = new float[]{0.0f,0.0f,0.0f,1.0f,1.0f,0.0f,1.0f, 1.0f};

    private FloatBuffer mTextureBuffer;
    private FloatBuffer mVertexBuffer;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return this.mContext.getString(R.string.plugins_basic_name);
    }

    @Override
    public String getSummary() {
        return mContext.getString(R.string.plugins_basic_summary);
    }

    @Override
    public void create() {
        super.create();

        mProgram.use();
        mPositionParamHandle = mProgram.getAttributeHandle("position");
        mTextureCoordinateParamHandle = mProgram.getAttributeHandle("texCoord");
        mTextureParamHandle = mProgram.getUniformHandle("sTexture");

        mVertexBuffer = ByteBufferPool.getInstance().getDirectFloatBuffer(VERTEX_COORDS.length);
        mVertexBuffer.put(VERTEX_COORDS);
        mVertexBuffer.position(0);

        mTextureBuffer = ByteBufferPool.getInstance().getDirectFloatBuffer(TEXTURE_COORDS.length);
        mTextureBuffer.put(TEXTURE_COORDS);
        mTextureBuffer.position(0);
    }

    @Override
    public void draw(final GlIntRect viewport, final int orientation) {
        mProgram.use().enableAttributes();

        GLES20.glVertexAttribPointer(mPositionParamHandle, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glVertexAttribPointer(mTextureCoordinateParamHandle, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mCameraTextureId);
        GLES20.glUniform1i(mTextureParamHandle, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        mProgram.disableAttributes();
    }

    @Override
    public void delete() {
        super.delete();
        ByteBufferPool.getInstance().returnDirectBuffer(mTextureBuffer);
        ByteBufferPool.getInstance().returnDirectBuffer(mTextureBuffer);
    }
}
