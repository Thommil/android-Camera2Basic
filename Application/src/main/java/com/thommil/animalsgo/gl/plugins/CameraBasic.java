package com.thommil.animalsgo.gl.plugins;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

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
        };

        mCameraTexture.bind().allocate();
    }

    @Override
    public void draw(final GlIntRect viewport, final int orientation) {
        //Camera shader -> FBO
        mProgram.use().enableAttributes();
        sSquareImageBuffer.bind();
        mCameraTexture.bind();

        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, sSquareImageBuffer.datasize * 4, 0);
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 2, GLES20.GL_FLOAT, false, sSquareImageBuffer.datasize * 4, sSquareImageBuffer.datasize * 2);
        GLES20.glUniform1i(mTextureParamHandle, 0);
        //TODO Transform matrix when android < 6 (using accelerometer)
        //TODO Transform matrix on zoom
        GLES20.glUniformMatrix4fv(mTextureTranformHandle, 1, false, mCameraTransformMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        mCameraTexture.unbind();
        sSquareImageBuffer.unbind();
        mProgram.disableAttributes();
    }

    @Override
    public void delete() {
        super.delete();
        mCameraTexture.free();
    }
}
