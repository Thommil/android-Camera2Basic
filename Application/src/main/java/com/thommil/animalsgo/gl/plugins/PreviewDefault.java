package com.thommil.animalsgo.gl.plugins;

import android.opengl.GLES20;

import com.thommil.animalsgo.R;
import com.thommil.animalsgo.gl.PreviewPlugin;
import com.thommil.animalsgo.gl.libgl.GlBuffer;
import com.thommil.animalsgo.gl.libgl.GlCanvas;
import com.thommil.animalsgo.gl.libgl.GlIntRect;

public class PreviewDefault extends PreviewPlugin {

    private static final String TAG = "A_GO/Plugin/PreviewDefault";

    private static final String ID = "preview/default";
    private static final String PROGRAM_ID = "default";

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

    private int mTextureUniforHandle;

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
        return this.mContext.getString(R.string.plugins_preview_default_name);
    }

    @Override
    public String getSummary() {
        return mContext.getString(R.string.plugins_preview_default_summary);
    }

    @Override
    public void create() {
        super.create();

        mProgram.use();
        mSquareImageVertChunk.handle = mProgram.getAttributeHandle(ATTRIBUTE_POSITION);
        mSquareImageFragChunk.handle = mProgram.getAttributeHandle(ATTRIBUTE_TEXTCOORD);
        mTextureUniforHandle = mProgram.getUniformHandle(UNIFORM_TEXTURE);

        mSquareImageBuffer = new GlBuffer<>(new GlBuffer.Chunk[]{mSquareImageVertChunk, mSquareImageFragChunk});
        mSquareImageBuffer.commit();
    }

    @Override
    public void draw(final GlIntRect viewport, final int orientation) {
        mProgram.use();
        mCameraTexture.bind();

        GLES20.glUniform1i(mTextureUniforHandle, 0);
        GlCanvas.drawArrays(mProgram, mSquareImageBuffer);

        mCameraTexture.unbind();
    }

    @Override
    public void free() {
        super.free();
        mSquareImageBuffer.free();
    }
}
