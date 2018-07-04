package com.thommil.animalsgo.gl.plugins;

import android.opengl.GLES20;

import com.thommil.animalsgo.R;
import com.thommil.animalsgo.gl.PreviewPlugin;
import com.thommil.animalsgo.gl.libgl.GlIntRect;

public class Basic extends PreviewPlugin {

    private static final String TAG = "A_GO/Plugin/Basic";

    private static final String ID = "basic";

    private int mPositionParamHandle;
    private int mTextureCoordinateParamHandle;
    private int mTextureParamHandle;

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
    }

    @Override
    public void draw(final GlIntRect viewport, final int orientation) {
        mProgram.use().enableAttributes();
        sSquareImageBuffer.bind();
        mCameraTexture.bind();

        GLES20.glVertexAttribPointer(mPositionParamHandle, sSquareImageBuffer.chunks[0].components,
                sSquareImageBuffer.datatype, false, sSquareImageBuffer.stride, 0);
        GLES20.glVertexAttribPointer(mTextureCoordinateParamHandle, sSquareImageBuffer.chunks[1].components,
                sSquareImageBuffer.datatype, false, sSquareImageBuffer.stride, sSquareImageBuffer.chunks[1].offset);
        GLES20.glUniform1i(mTextureParamHandle, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, sSquareImageBuffer.count);

        mCameraTexture.unbind();
        sSquareImageBuffer.unbind();
        mProgram.disableAttributes();
    }
}
