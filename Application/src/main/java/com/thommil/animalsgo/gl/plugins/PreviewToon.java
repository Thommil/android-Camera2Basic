package com.thommil.animalsgo.gl.plugins;

import android.opengl.GLES20;

import com.thommil.animalsgo.R;
import com.thommil.animalsgo.gl.PreviewPlugin;
import com.thommil.animalsgo.gl.libgl.GlBuffer;
import com.thommil.animalsgo.gl.libgl.GlIntRect;


public class PreviewToon extends PreviewPlugin {

    private static final String TAG = "A_GO/Plugin/PreviewToon";

    private static final String ID = "preview/toon";
    private static final String PROGRAM_ID = "toon";

    protected final GlBuffer.Chunk<float[]> mSquareImageVertChunk =
            new GlBuffer.Chunk<>(new float[]{
                    -1.0f,-1.0f,
                    -1.0f,1.0f,
                    1.0f,-1.0f,
                    1.0f,1.0f
            },2);

    protected final GlBuffer.Chunk<float[]> mSquareImageFragChunk =
            new GlBuffer.Chunk<>(new float[]{
                    0.0f,0.0f,
                    0.0f,1.0f,
                    1.0f,0.0f,
                    1.0f, 1.0f
            },2);

    protected GlBuffer<float[]> mSquareImageBuffer;

    private int mPositionAttributeHandle;
    private int mTextureCoordinatesAttributeHandle;

    private int mTextureUniforHandle;
    private int mViewSizeUniformHandle;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getProgramId() {
        return PROGRAM_ID;
    }

    @Override
    public String getName() {
        return this.mContext.getString(R.string.plugins_preview_toon_name);
    }

    @Override
    public String getSummary() {
        return mContext.getString(R.string.plugins_preview_toon_summary);
    }

    @Override
    public void create() {
        super.create();

        mProgram.use();
        mPositionAttributeHandle = mProgram.getAttributeHandle(ATTRIBUTE_POSITION);
        mTextureCoordinatesAttributeHandle = mProgram.getAttributeHandle(ATTRIBUTE_TEXTCOORD);
        mTextureUniforHandle = mProgram.getUniformHandle(UNIFORM_TEXTURE);
        mViewSizeUniformHandle = mProgram.getUniformHandle(UNIFORM_VIEW_SIZE);

        mSquareImageBuffer = new GlBuffer<>(new GlBuffer.Chunk[]{mSquareImageVertChunk, mSquareImageFragChunk});
    }

    @Override
    public void draw(final GlIntRect viewport, final int orientation) {
        mProgram.use().enableAttributes();
        mCameraTexture.bind();

        mSquareImageBuffer.buffer.rewind();
        GLES20.glVertexAttribPointer(mPositionAttributeHandle, mSquareImageBuffer.chunks[0].components,
                mSquareImageBuffer.datatype, false, mSquareImageBuffer.stride, mSquareImageBuffer.buffer);
        mSquareImageBuffer.buffer.position(mSquareImageBuffer.chunks[1].position);
        GLES20.glVertexAttribPointer(mTextureCoordinatesAttributeHandle, mSquareImageBuffer.chunks[1].components,
                mSquareImageBuffer.datatype, false, mSquareImageBuffer.stride, mSquareImageBuffer.buffer);
        GLES20.glUniform1i(mTextureUniforHandle, 0);
        GLES20.glUniform2f(mViewSizeUniformHandle, viewport.width(), viewport.height());
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mSquareImageBuffer.count);

        mCameraTexture.unbind();
        mProgram.disableAttributes();
    }

    @Override
    public void free() {
        super.free();
        mSquareImageBuffer.free();
    }
}
