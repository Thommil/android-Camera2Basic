package com.thommil.animalsgo.gl.plugins;

import android.opengl.GLES20;

import com.thommil.animalsgo.R;
import com.thommil.animalsgo.gl.RendererPlugin;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Basic extends RendererPlugin {

    private static final String TAG = "A_GO/Plugin/Basic";

    private static final String ID = "basic";

    private static final int MASK = TYPE_PREVIEW | TYPE_CAPTURE | TYPE_CARD;

    private int mPositionParamHandle;
    private int mTextureCoordinateParamHandle;
    private int mTextureParamHandle;

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
    public boolean isSupporting(final int flag) {
        return (flag & MASK) > 0;
    }

    @Override
    public void create() {
        super.create();
        GLES20.glUseProgram(mPluginShaderProgram);
        mPositionParamHandle = GLES20.glGetAttribLocation(mPluginShaderProgram, "position");
        mTextureCoordinateParamHandle = GLES20.glGetAttribLocation(mPluginShaderProgram, "texCoord");
        mTextureParamHandle = GLES20.glGetUniformLocation(mPluginShaderProgram, "sTexture");

        final ByteBuffer bb = ByteBuffer.allocateDirect(8 * Float.BYTES);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(new float[]{ -1.0f,-1.0f,-1.0f,1.0f, 1.0f,-1.0f, 1.0f,1.0f});
        mVertexBuffer.position(0);

        final ByteBuffer texturebb = ByteBuffer.allocateDirect(8 * Float.BYTES);
        texturebb.order(ByteOrder.nativeOrder());
        mTextureBuffer = texturebb.asFloatBuffer();
        mTextureBuffer.put(new float[]{0.0f,0.0f,0.0f,1.0f,1.0f,0.0f,1.0f,  1.0f});
        mTextureBuffer.position(0);
    }

    @Override
    public void draw(final int texId, final int width, final int height) {
        GLES20.glViewport(0, 0, width, height);

        GLES20.glUseProgram(mPluginShaderProgram);
        GLES20.glEnableVertexAttribArray(mPositionParamHandle);
        GLES20.glVertexAttribPointer(mPositionParamHandle, 2, GLES20.GL_FLOAT, false, 8, mVertexBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
        GLES20.glUniform1i(mTextureParamHandle, 0);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateParamHandle);
        GLES20.glVertexAttribPointer(mTextureCoordinateParamHandle, 2, GLES20.GL_FLOAT, false, 8, mTextureBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(mPositionParamHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordinateParamHandle);
    }
}
