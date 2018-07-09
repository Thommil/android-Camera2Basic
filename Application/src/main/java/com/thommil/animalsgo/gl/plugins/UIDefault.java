package com.thommil.animalsgo.gl.plugins;

import android.opengl.GLES20;

import com.thommil.animalsgo.R;
import com.thommil.animalsgo.gl.UIPlugin;
import com.thommil.animalsgo.gl.libgl.GlBuffer;
import com.thommil.animalsgo.gl.libgl.GlCanvas;
import com.thommil.animalsgo.gl.libgl.GlIntRect;
import com.thommil.animalsgo.gl.libgl.GlOperation;
import com.thommil.animalsgo.gl.libgl.GlTextureAtlas;
import com.thommil.animalsgo.gl.ui.Sprite;
import com.thommil.animalsgo.utils.ByteBufferPool;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class UIDefault extends UIPlugin {

    private static final String TAG = "A_GO/Plugin/UIDefault";

    private static final String ID = "ui/default";
    private static final String PROGRAM_ID = "ui_default";


    protected Sprite mSprite;

    private int mPositionAttributeHandle;
    private int mTextureCoordinatesAttributeHandle;
    private int mColorAttributeHandle;

    private int mTextureUniforHandle;

    private GlTextureAtlas mTextureAtlas;

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
        return this.mContext.getString(R.string.plugins_ui_default_name);
    }

    @Override
    public String getSummary() {
        return mContext.getString(R.string.plugins_ui_default_summary);
    }

    @Override
    public void create() {
        super.create();

        GlOperation.configureBlendTest(GlOperation.BLEND_FACTOR_SRC_ALPA, GlOperation.BLEND_FACTOR_DST_ALPA, GlOperation.BLEND_FACTOR_ONE_MINUS_SRC_ALPA, null);

        mTextureAtlas = new GlTextureAtlas(mContext, R.xml.ui_default_texture_atlas);
        mTextureAtlas.allocate();

        mSprite = mTextureAtlas.createSprite("logo", 1);

        mSprite.setSize(0.2f,0.2f);
        mSprite.setOriginCenter();;

        mProgram.use();
        mPositionAttributeHandle = mProgram.getAttributeHandle(ATTRIBUTE_POSITION);
        mTextureCoordinatesAttributeHandle = mProgram.getAttributeHandle(ATTRIBUTE_TEXTCOORD);
        mColorAttributeHandle = mProgram.getAttributeHandle(ATTRIBUTE_COLOR);
        mTextureUniforHandle = mProgram.getUniformHandle(UNIFORM_TEXTURE);

        buffer = ByteBufferPool.getInstance().getDirectFloatBuffer(Sprite.SPRITE_SIZE);
    }
//final GlBuffer<short[]> indices =  GlBufferGlBuffer.Chunk<short[]>(new short[]{0,1,2,3,3,0});
FloatBuffer buffer;
    @Override
    public void draw(final GlIntRect viewport, final int orientation) {
        GlOperation.setTestState(GlOperation.TEST_BLEND, true);

        mProgram.use().enableAttributes();
        mTextureAtlas.getTexture().bind();
        mSprite.rotate(1);
        buffer.rewind();
        buffer.put(mSprite.getVertices());


        buffer.rewind();
        GLES20.glVertexAttribPointer(mPositionAttributeHandle, 2,
                GLES20.GL_FLOAT, false, Sprite.SPRITE_SIZE, buffer);
        buffer.position(2);
        GLES20.glVertexAttribPointer(mColorAttributeHandle, 1,
                GLES20.GL_FLOAT, false, Sprite.SPRITE_SIZE, buffer);
        buffer.position(3);
        GLES20.glVertexAttribPointer(mTextureCoordinatesAttributeHandle, 2,
                GLES20.GL_FLOAT, false, Sprite.SPRITE_SIZE, buffer);
        GLES20.glUniform1i(mTextureUniforHandle, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        mTextureAtlas.getTexture().unbind();
        mProgram.disableAttributes();
    }

    @Override
    public void free() {
        super.free();
        mTextureAtlas.free();
    }
}
