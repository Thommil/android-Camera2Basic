package com.thommil.animalsgo.gl.plugins;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Size;

import com.thommil.animalsgo.R;
import com.thommil.animalsgo.gl.CameraPlugin;
import com.thommil.animalsgo.gl.libgl.GlIntRect;
import com.thommil.animalsgo.gl.libgl.GlOperation;
import com.thommil.animalsgo.utils.ByteBufferPool;

import java.nio.FloatBuffer;

public class CameraBasic extends CameraPlugin {

    private static final String TAG = "A_GO/plugin/CameraBasic";

    private static final String ID = "camera_basic";

    private static final float[]VERTEX_COORDS = new float[]{ -1.0f,-1.0f,-1.0f,1.0f, 1.0f,-1.0f, 1.0f,1.0f};
    private static final float[]TEXTURE_COORDS = new float[]{0.0f,0.0f,0.0f,1.0f,1.0f,0.0f,1.0f,  1.0f};


    private FloatBuffer mTextureBuffer;
    private FloatBuffer mVertexBuffer;

    private int textureCoordinateHandle;
    private int mTextureParamHandle;
    private int mTextureTranformHandle;
    private int mPositionHandle;
    private int mCamTextureId;

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
    public int getCameraTextureId() {
        return mCamTextureId;
    }

    @Override
    public void create() {
        super.create();

        mProgram.use();
        mPositionHandle = mProgram.getAttributeHandle("position");
        textureCoordinateHandle = mProgram.getAttributeHandle("camTexCoordinate");
        mTextureParamHandle = mProgram.getUniformHandle("camTexture");
        mTextureTranformHandle = mProgram.getUniformHandle("camTextureTransform");

        mVertexBuffer = ByteBufferPool.getInstance().getDirectFloatBuffer(VERTEX_COORDS.length);
        mVertexBuffer.put(VERTEX_COORDS);
        mVertexBuffer.position(0);

        mTextureBuffer = ByteBufferPool.getInstance().getDirectFloatBuffer(TEXTURE_COORDS.length);
        mTextureBuffer.put(TEXTURE_COORDS);
        mTextureBuffer.position(0);

        final int[] texturesId = new int[1];
        GLES20.glGenTextures(1, texturesId , 0);
        GlOperation.checkGlError("Texture generate");
        mCamTextureId = texturesId[0];

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mCamTextureId);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GlOperation.checkGlError("Texture bind");
    }

    @Override
    public void draw(final GlIntRect viewport, final int orientation) {
        //Camera shader -> FBO
        mProgram.use().enableAttributes();

        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 8, mVertexBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mCamTextureId);
        GLES20.glUniform1i(mTextureParamHandle, 0);

        GLES20.glVertexAttribPointer(textureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 8, mTextureBuffer);

        //TODO Transform matrix when android < 6 (using accelerometer)
        //TODO Transform matrix on zoom
        GLES20.glUniformMatrix4fv(mTextureTranformHandle, 1, false, mCameraTransformMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        mProgram.disableAttributes();
    }

    @Override
    public void delete() {
        super.delete();
        GLES20.glDeleteTextures(1, new int[]{mCamTextureId}, 0);
        ByteBufferPool.getInstance().returnDirectBuffer(mTextureBuffer);
        ByteBufferPool.getInstance().returnDirectBuffer(mTextureBuffer);
    }
}
