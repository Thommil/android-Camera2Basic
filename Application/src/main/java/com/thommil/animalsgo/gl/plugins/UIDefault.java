package com.thommil.animalsgo.gl.plugins;

import android.opengl.GLES20;

import com.thommil.animalsgo.R;
import com.thommil.animalsgo.gl.UIPlugin;
import com.thommil.animalsgo.gl.libgl.GlBuffer;
import com.thommil.animalsgo.gl.libgl.GlColor;
import com.thommil.animalsgo.gl.libgl.GlDrawableBufferBatch;
import com.thommil.animalsgo.gl.libgl.GlIntRect;
import com.thommil.animalsgo.gl.libgl.GlOperation;
import com.thommil.animalsgo.gl.libgl.GlSprite;
import com.thommil.animalsgo.gl.libgl.GlSpriteColor;
import com.thommil.animalsgo.gl.libgl.GlTexture;
import com.thommil.animalsgo.gl.libgl.GlTextureAtlas;
import com.thommil.animalsgo.utils.ResourcesLoader;

import org.json.JSONException;

import java.io.IOException;

public class UIDefault extends UIPlugin {

    private static final String TAG = "A_GO/Plugin/UIDefault";

    private static final String ID = "ui/default";
    private static final String PROGRAM_ID = "ui_default";

    private static final String ATLAS_FILE = "textures/ui_default.json";

    private int mTextureUniforHandle;

    private GlTextureAtlas mTextureAtlas;

    private GlSpriteColor mSmall;
    private GlSpriteColor mBig;
    private GlSpriteColor mLogo;
    GlDrawableBufferBatch mBatch;

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
    public void allocate(final float surfaceRatio) {
        super.allocate(surfaceRatio);

        //Scene
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

            mTextureAtlas.parseJON(mContext, ResourcesLoader.jsonFromAsset(mContext, ATLAS_FILE));
            mTextureAtlas.allocate();

            GlTextureAtlas.SubTexture subTexture = mTextureAtlas.getSubTexture("big");
            mLogo = new GlSpriteColor(mTextureAtlas.getTexture(), subTexture.x, subTexture.y, subTexture.width, subTexture.height);
            subTexture = mTextureAtlas.getSubTexture("small");
            mSmall = new GlSpriteColor(mTextureAtlas.getTexture(), subTexture.x, subTexture.y, subTexture.width, subTexture.height);
            mBig = new GlSpriteColor(mTextureAtlas.getTexture(), subTexture.x, subTexture.y, subTexture.width, subTexture.height);

        }catch(IOException ioe){
            throw new RuntimeException("Failed to load texture atlas : " + ioe);
        }catch(JSONException je){
            throw new RuntimeException("Failed to load texture atlas : " + je);
        }

        //Program
        mProgram.use();
        mTextureUniforHandle = mProgram.getUniformHandle(UNIFORM_TEXTURE);

        //Buffer & Batch
        mBatch = new GlDrawableBufferBatch(mLogo, mSmall, mBig);
        mBatch.setVertexAttribHandles(mProgram.getAttributeHandle(ATTRIBUTE_POSITION), mProgram.getAttributeHandle(ATTRIBUTE_TEXTCOORD),mProgram.getAttributeHandle(ATTRIBUTE_COLOR));
        mBatch.allocate(GlBuffer.USAGE_DYNAMIC_DRAW, GlBuffer.TARGET_ARRAY_BUFFER, false);
        mLogo.size(0.5f,0.5f).position(0.0f, 0.0f);
        mSmall.size(0.19f, 0.30f).position(-0.5f, 0);
        mBig.size(0.39f, 0.6f).position(0.5f, 0);
        mBatch.commit();
        //Blend test (should be called each draw if another one is used)
        GlOperation.configureBlendTest(GlOperation.BLEND_FACTOR_SRC_ALPA, GlOperation.BLEND_FACTOR_ONE_MINUS_SRC_ALPA, GlOperation.BLEND_OPERATION_ADD, null);
    }


    float color = 0f;

    @Override
    public void draw(final GlIntRect viewport, final int orientation) {
        //Blend test
        GlOperation.setTestState(GlOperation.TEST_BLEND, true);
        mLogo.rotate(0.1f);
        //color += 0.001;
        mBig.translate(0.000f, 0.001f);
        mSmall.translate(0.000f, -0.001f);
        mBatch.commit();

        //Program
        mProgram.use();
        GLES20.glUniform1i(mTextureUniforHandle, mTextureAtlas.getTexture().index);

        //Texture
        mTextureAtlas.getTexture().bind();

        //Draw
        mBatch.draw(mProgram);
    }

    @Override
    public void free() {
        super.free();
        mBatch.free();
        mLogo.free();
        mBig.free();
        mSmall.free();

        if(mTextureAtlas != null) {
            mTextureAtlas.free();
            mTextureAtlas = null;
        }
    }
}
