package com.thommil.animalsgo.gl.plugins;

import android.opengl.GLES20;

import com.thommil.animalsgo.R;
import com.thommil.animalsgo.gl.UIPlugin;
import com.thommil.animalsgo.gl.libgl.GlCanvas;
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

    private GlSprite mSprite;

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
        mSprite = mTextureAtlas.createSprite("big");
        mSprite.chunks[GlSprite.CHUNK_VERTEX_INDEX].handle = mProgram.getAttributeHandle(ATTRIBUTE_POSITION);
        mSprite.chunks[GlSprite.CHUNK_TEXTURE_INDEX].handle = mProgram.getAttributeHandle(ATTRIBUTE_TEXTCOORD);
        mTextureUniforHandle = mProgram.getUniformHandle(UNIFORM_TEXTURE);

        //Scene

        //Buffers
        //mSprite.allocate(GlBuffer.USAGE_DYNAMIC_DRAW, GlBuffer.TARGET_ARRAY_BUFFER, false);
        //mSprite.size(0.5f,0.5f).commit();
        //mSprite.commit();

        //Blend test (should be called each draw if another one is used)
        GlOperation.configureBlendTest(GlOperation.BLEND_FACTOR_SRC_ALPA, GlOperation.BLEND_FACTOR_ONE_MINUS_SRC_ALPA, GlOperation.BLEND_OPERATION_ADD, null);
    }
    float size = 0.1f;
    //final GlBuffer<short[]> indices =  GlBufferGlBuffer.Chunk<short[]>(new short[]{0,1,2,3,3,0});

    @Override
    public void draw(final GlIntRect viewport, final float ratio, final int orientation) {
        //Blend test
        GlOperation.setTestState(GlOperation.TEST_BLEND, true);
        mSprite.size(1,ratio).commit();

        //Program
        mProgram.use();
        GLES20.glUniform1i(mTextureUniforHandle, mTextureAtlas.getTexture().index);

        //Texture
        mTextureAtlas.getTexture().bind();

        //Draw
        GlCanvas.drawArrays(mProgram, mSprite);
    }

    @Override
    public void free() {
        super.free();
        mSprite.free();

        if(mTextureAtlas != null) {
            mTextureAtlas.free();
            mTextureAtlas = null;
        }
    }
}
