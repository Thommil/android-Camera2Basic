package com.thommil.animalsgo.gl.plugins;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.thommil.animalsgo.R;
import com.thommil.animalsgo.gl.CameraPlugin;
import com.thommil.animalsgo.gl.libgl.GlCanvas;
import com.thommil.animalsgo.gl.libgl.GlIntRect;
import com.thommil.animalsgo.gl.libgl.GlOperation;
import com.thommil.animalsgo.gl.libgl.GlTexture;

public class CameraDefault extends CameraPlugin {

    private static final String TAG = "A_GO/plugin/CameraDefault";

    private static final String ID = "camera/default";
    private static final String PROGRAM_ID = "camera_default";

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

        //Texture
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

        //Program
        mProgram.use();
        mVertChunk.handle = mProgram.getAttributeHandle(ATTRIBUTE_POSITION);
        mTextChunk.handle = mProgram.getAttributeHandle(ATTRIBUTE_TEXTCOORD);
        mTextureUniforHandle = mProgram.getUniformHandle(UNIFORM_TEXTURE);
        mMvpMatrixNuniformHandle = mProgram.getUniformHandle(UNIFORM_MVP_MATRIX);
    }

    @Override
    public void draw(final GlIntRect viewport, final int orientation) {
        super.draw(viewport, orientation);

        //Program
        mProgram.use();
        GLES20.glUniform1i(mTextureUniforHandle, mCameraTexture.index);
        GLES20.glUniformMatrix4fv(mMvpMatrixNuniformHandle, 1, false, mCameraTransformMatrix, 0);

        //Texture
        mCameraTexture.bind();

        //Draw
        GlCanvas.drawArrays(mProgram, mCameraPreviewBuffer);
    }

    @Override
    public void free() {
        super.free();
        mCameraTexture.free();
    }
}
