package com.thommil.animalsgo.gl.plugins;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.thommil.animalsgo.R;
import com.thommil.animalsgo.gl.CameraPlugin;
import com.thommil.animalsgo.gl.libgl.GlIntRect;
import com.thommil.animalsgo.gl.libgl.GlTexture;

public class CameraDefault extends CameraPlugin {

    private static final String TAG = "A_GO/plugin/CameraDefault";

    private static final String ID = "camera/default";
    private static final String PROGRAM_ID = "camera_default";

    private int mPositionAttributeHandle;
    private int mTextureCoordinatesAttributeHandle;

    private int mTextureUniforHandle;
    private int mMvpMatrixNuniformHandle;

    private GlTexture mCameraTexture;

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
        return this.mContext.getString(R.string.plugins_camera_default_name);
    }

    @Override
    public String getSummary() {
        return mContext.getString(R.string.plugins_camera_default_summary);
    }

    @Override
    public GlTexture getCameraTexture() {
        return mCameraTexture;
    }

    @Override
    public void create() {
        super.create();

        mProgram.use();
        mPositionAttributeHandle = mProgram.getAttributeHandle(ATTRIBUTE_POSITION);
        mTextureCoordinatesAttributeHandle = mProgram.getAttributeHandle(ATTRIBUTE_TEXTCOORD);
        mTextureUniforHandle = mProgram.getUniformHandle(UNIFORM_TEXTURE);
        mMvpMatrixNuniformHandle = mProgram.getUniformHandle(UNIFORM_MVP_MATRIX);

        mCameraTexture = new GlTexture() {
            @Override
            public int getTarget() {
                return GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
            }

            @Override
            public int getMagnificationFilter() {
                return GlTexture.MAG_FILTER_HIGH;
            }

            @Override
            public int getWrapMode(int axeId) {
                return GlTexture.WRAP_CLAMP_TO_EDGE;
            }

            @Override
            public int getFormat() {
                return FORMAT_RGB;
            }

            @Override
            public int getType() {
                return TYPE_UNSIGNED_SHORT_5_6_5;
            }
        };

        mCameraTexture.bind().configure();
    }

    @Override
    public void draw(final GlIntRect viewport, final int orientation) {
        super.draw(viewport, orientation);

        //Camera mProgram -> FBO
        mProgram.use().enableAttributes();
        mCameraTexture.bind();

        mCameraPreviewBuffer.buffer.rewind();
        GLES20.glVertexAttribPointer(mPositionAttributeHandle, mCameraPreviewBuffer.chunks[0].components,
                mCameraPreviewBuffer.datatype, false, mCameraPreviewBuffer.stride, mCameraPreviewBuffer.buffer);
        mCameraPreviewBuffer.buffer.position(mCameraPreviewBuffer.chunks[1].position);
        GLES20.glVertexAttribPointer(mTextureCoordinatesAttributeHandle, mCameraPreviewBuffer.chunks[1].components,
                mCameraPreviewBuffer.datatype, false, mCameraPreviewBuffer.stride, mCameraPreviewBuffer.buffer);

        GLES20.glUniform1i(mTextureUniforHandle, 0);
        GLES20.glUniformMatrix4fv(mMvpMatrixNuniformHandle, 1, false, mCameraTransformMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mCameraPreviewBuffer.count);

        mCameraTexture.unbind();
        mProgram.disableAttributes();
    }

    @Override
    public void free() {
        super.free();
        mCameraTexture.free();
    }
}
