package com.thommil.animalsgo.gl.plugins;

import android.opengl.GLES20;

import com.thommil.animalsgo.R;
import com.thommil.animalsgo.gl.UIPlugin;
import com.thommil.animalsgo.gl.libgl.GlDrawableBufferBatch;
import com.thommil.animalsgo.gl.libgl.GlIntRect;
import com.thommil.animalsgo.gl.libgl.GlOperation;
import com.thommil.animalsgo.gl.libgl.GlSprite;
import com.thommil.animalsgo.gl.libgl.GlTexture;
import com.thommil.animalsgo.gl.libgl.GlTextureAtlas;
import com.thommil.animalsgo.utils.ResourcesLoader;

import org.json.JSONException;

import java.io.IOException;

public class UIDefault extends UIPlugin {

    private static final String TAG = "A_GO/Plugin/UIDefault";

    private static final String ID = "ui/default";
    private static final String PROGRAM_ID = "ui_default";
    private static final String ATLAS_FILE = "ui_default.json";

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
    public void allocate() {
        super.allocate();

        //Texture
        try {
            mTextureAtlas = new GlTextureAtlas(new GlTexture() {
                @Override
                public int getMagnificationFilter() {
                    return GlTexture.MAG_FILTER_HIGH;
                }

                @Override
                public int getWrapMode(int axeId) {
                    return GlTexture.WRAP_CLAMP_TO_EDGE;
                }
            });

            mTextureAtlas.parseJON(mContext, ResourcesLoader.jsonFromAsset(mContext, com.thommil.animalsgo.Settings.ASSETS_TEXTURES_PATH + ATLAS_FILE));
            mTextureAtlas.allocate();

        }catch(IOException ioe){
            throw new RuntimeException("Failed to load texture atlas : " + ioe);
        }catch(JSONException je){
            throw new RuntimeException("Failed to load texture atlas : " + je);
        }

        //Program
        mProgram.use();
        mBig = mTextureAtlas.createSprite("big");
        mBig.setVertexAttribHandles(mProgram.getAttributeHandle(ATTRIBUTE_POSITION), mProgram.getAttributeHandle(ATTRIBUTE_TEXTCOORD));
        mSmall = mTextureAtlas.createSprite("small");
        mSmall.setVertexAttribHandles(mProgram.getAttributeHandle(ATTRIBUTE_POSITION), mProgram.getAttributeHandle(ATTRIBUTE_TEXTCOORD));
        mTextureUniforHandle = mProgram.getUniformHandle(UNIFORM_TEXTURE);

        //Scene

        //Buffers
        //mBig.allocate(GlBuffer.USAGE_DYNAMIC_DRAW, GlBuffer.TARGET_ARRAY_BUFFER, false);
        //mSmall.allocate(GlBuffer.USAGE_DYNAMIC_DRAW, GlBuffer.TARGET_ARRAY_BUFFER, false);
        //mSprite.size(0.5f,0.5f).commit();
        //mSprite.commit();
        mBig.size(0.2f, 0.2f).position(-0.5f, 0).commit();
        mSmall.size(0.2f, 0.2f).position(0.5f, 0).commit();

        //batch = new GlDrawableBufferBatch<>(mBig, mSmall);
        //batch.setVertexAttribHandles(mProgram.getAttributeHandle(ATTRIBUTE_POSITION), mProgram.getAttributeHandle(ATTRIBUTE_TEXTCOORD));
        //batch.commit();

        //Blend test (should be called each draw if another one is used)
        GlOperation.configureBlendTest(GlOperation.BLEND_FACTOR_SRC_ALPA, GlOperation.BLEND_FACTOR_ONE_MINUS_SRC_ALPA, GlOperation.BLEND_OPERATION_ADD, null);
    }

    private GlSprite mSmall;
    private GlSprite mBig;
    GlDrawableBufferBatch<float[]> batch;

    float size = 0.1f;
    //final GlBuffer<short[]> indices =  GlBufferGlBuffer.Chunk<short[]>(new short[]{0,1,2,3,3,0});

    @Override
    public void draw(final GlIntRect viewport, final float ratio, final int orientation) {
        //Blend test
        GlOperation.setTestState(GlOperation.TEST_BLEND, true);

        //Program
        mProgram.use();
        GLES20.glUniform1i(mTextureUniforHandle, mTextureAtlas.getTexture().index);

        //Texture
        mTextureAtlas.getTexture().bind();

        //Draw
        mBig.draw(mProgram);
        mSmall.draw(mProgram);
        //GlCanvas.draw(mProgram, batch);
    }

    @Override
    public void free() {
        super.free();
        //batch.free();
        mBig.free();
        mSmall.free();

        if(mTextureAtlas != null) {
            mTextureAtlas.free();
            mTextureAtlas = null;
        }
    }
}
