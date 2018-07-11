package com.thommil.animalsgo.gl.plugins;

import android.opengl.GLES20;

import com.thommil.animalsgo.R;
import com.thommil.animalsgo.gl.PreviewPlugin;
import com.thommil.animalsgo.gl.libgl.GlBuffer;
import com.thommil.animalsgo.gl.libgl.GlDrawableBuffer;
import com.thommil.animalsgo.gl.libgl.GlIntRect;

public class PreviewDefault extends PreviewPlugin {

    private static final String TAG = "A_GO/Plugin/PreviewDefault";

    private static final String ID = "preview/default";
    private static final String PROGRAM_ID = "default";

    protected final GlBuffer.Chunk<float[]> mVertChunk =
            new GlBuffer.Chunk<>(new float[]{
                    -1.0f, 1.0f,    // left top
                    -1.0f, -1.0f,   // left bottom
                    1.0f, 1.0f,     // right top
                    1.0f, -1.0f     // right bottom
            },2);

    protected final GlBuffer.Chunk<float[]> mTextChunk =
            new GlBuffer.Chunk<>(new float[]{
                    0.0f,1.0f,
                    0.0f,0.0f,
                    1.0f,1.0f,
                    1.0f,0.0f
            },2);

    protected GlDrawableBuffer<float[]> mPreviewBuffer;

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
    public void allocate(final float surfaceRatio) {
        super.allocate(surfaceRatio);

        //Buffer
        mPreviewBuffer = new GlDrawableBuffer<>(mVertChunk, mTextChunk);
        mPreviewBuffer.commit();

        //Program
        mProgram.use();
        mPreviewBuffer.setVertexAttribHandles(mProgram.getAttributeHandle(ATTRIBUTE_POSITION), mProgram.getAttributeHandle(ATTRIBUTE_TEXTCOORD));
        mTextureUniforHandle = mProgram.getUniformHandle(UNIFORM_TEXTURE);
    }

    @Override
    public void draw(final GlIntRect viewport, final int orientation) {
        //Program
        mProgram.use();
        GLES20.glUniform1i(mTextureUniforHandle, mSourceTexture.index);

        //Texture
        mSourceTexture.bind();

        //Draw
        mPreviewBuffer.draw(mProgram);
    }

    @Override
    public void free() {
        super.free();
        if(mPreviewBuffer != null) {
            mPreviewBuffer.free();
            mPreviewBuffer = null;
        }
    }
}
