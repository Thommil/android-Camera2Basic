package com.thommil.animalsgo.gl.plugins;

import android.opengl.GLES11;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.thommil.animalsgo.R;
import com.thommil.animalsgo.gl.CameraPlugin;
import com.thommil.animalsgo.gl.libgl.GlIntRect;
import com.thommil.animalsgo.gl.libgl.GlTexture;

public class CameraBasic extends CameraPlugin {

    private static final String TAG = "A_GO/plugin/CameraBasic";

    private static final String ID = "camera_basic";

    private int textureCoordinateHandle;
    private int mTextureParamHandle;
    private int mTextureTranformHandle;
    private int mPositionHandle;
    private GlTexture mCameraTexture;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return this.mContext.getString(R.string.plugins_camera_basic_name);
    }

    @Override
    public String getSummary() {
        return mContext.getString(R.string.plugins_camera_basic_summary);
    }

    @Override
    public GlTexture getCameraTexture() {
        return mCameraTexture;
    }

    @Override
    public void create() {
        super.create();

        mProgram.use();
        mPositionHandle = mProgram.getAttributeHandle("position");
        textureCoordinateHandle = mProgram.getAttributeHandle("camTexCoordinate");
        mTextureParamHandle = mProgram.getUniformHandle("camTexture");
        mTextureTranformHandle = mProgram.getUniformHandle("camTextureTransform");

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
        };

        mCameraTexture.bind().configure();
    }

    @Override
    public void draw(final GlIntRect viewport, final int orientation) {
        super.draw(viewport, orientation);

        //Camera shader -> FBO
        mProgram.use().enableAttributes();
        mCameraTexture.bind();

        mCameraPreviewBuffer.buffer.rewind();
        GLES20.glVertexAttribPointer(mPositionHandle, mCameraPreviewBuffer.chunks[0].components,
                sSquareImageBuffer.datatype, false, sSquareImageBuffer.stride, mCameraPreviewBuffer.buffer);
        mCameraPreviewBuffer.buffer.position(mCameraPreviewBuffer.chunks[1].position);
        GLES20.glVertexAttribPointer(textureCoordinateHandle, mCameraPreviewBuffer.chunks[1].components,
                sSquareImageBuffer.datatype, false, sSquareImageBuffer.stride, mCameraPreviewBuffer.buffer);
        GLES20.glUniform1i(mTextureParamHandle, 0);
        GLES20.glUniformMatrix4fv(mTextureTranformHandle, 1, false, mCameraTransformMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, sSquareImageBuffer.count);

        mCameraTexture.unbind();
        mProgram.disableAttributes();
    }

    @Override
    public void free() {
        super.free();
        mCameraTexture.free();
    }
}
