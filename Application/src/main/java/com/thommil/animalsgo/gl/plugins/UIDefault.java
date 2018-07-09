package com.thommil.animalsgo.gl.plugins;

import android.opengl.GLES11Ext;
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
import java.nio.FloatBuffer;

public class UIDefault extends UIPlugin {

    private static final String TAG = "A_GO/Plugin/UIDefault";

    private static final String ID = "ui/default";
    private static final String PROGRAM_ID = "ui_default";
    private static final String ATLAS_FILE = "ui_default.json";


    private int mPositionAttributeHandle;
    private int mTextureCoordinatesAttributeHandle;
    private int mColorAttributeHandle;

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
    public void create() {
        super.create();

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
        try {
            mTextureAtlas.parseJON(mContext, ResourcesLoader.jsonFromAsset(mContext, com.thommil.animalsgo.Settings.ASSETS_TEXTURES_PATH + ATLAS_FILE));
        }catch(IOException ioe){
            throw new RuntimeException("Failed to load texture atlas : " + ioe);
        }catch(JSONException je){
            throw new RuntimeException("Failed to load texture atlas : " + je);
        }
        GlOperation.setActiveTexture(TEXTURE_INDEX);
        mTextureAtlas.allocate();
        mSprite = mTextureAtlas.createSprite("small");
        mSprite.commit();


        mProgram.use();
        mSprite.chunks[GlSprite.CHUNK_VERTEX_INDEX].handle = mProgram.getAttributeHandle(ATTRIBUTE_POSITION);
        mSprite.chunks[GlSprite.CHUNK_COLOR_INDEX].handle = mProgram.getAttributeHandle(ATTRIBUTE_COLOR);
        mSprite.chunks[GlSprite.CHUNK_TEXTURE_INDEX].handle = mProgram.getAttributeHandle(ATTRIBUTE_TEXTCOORD);
        mTextureUniforHandle = mProgram.getUniformHandle(UNIFORM_TEXTURE);

        GlOperation.configureBlendTest(GlOperation.BLEND_FACTOR_SRC_ALPA, GlOperation.BLEND_FACTOR_ONE_MINUS_SRC_ALPA, GlOperation.BLEND_OPERATION_ADD, null);

    }
//final GlBuffer<short[]> indices =  GlBufferGlBuffer.Chunk<short[]>(new short[]{0,1,2,3,3,0});
FloatBuffer buffer;
    @Override
    public void draw(final GlIntRect viewport, final int orientation) {
        GlOperation.setTestState(GlOperation.TEST_BLEND, true);
        GlOperation.setActiveTexture(TEXTURE_INDEX);

        mProgram.use();
        GLES20.glUniform1i(mTextureUniforHandle, TEXTURE_INDEX);
        GlCanvas.drawArrays(mProgram, mSprite);
    }

    @Override
    public void free() {
        super.free();
        mTextureAtlas.free();
    }
}
