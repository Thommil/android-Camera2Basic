package com.thommil.animalsgo.gl;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.thommil.animalsgo.Settings;
import com.thommil.animalsgo.gl.libgl.GlBuffer;
import com.thommil.animalsgo.gl.libgl.GlIntRect;
import com.thommil.animalsgo.gl.libgl.GlProgram;

import java.io.IOException;
import java.io.InputStream;

/**
 * Define a plugin used by CameraRenderer.
 */
public abstract class Plugin {

    private static final String TAG = "A_GO/Plugin";

    // Type for rendering camera on FBO
    public static final int TYPE_CAMERA = 0x01;

    // Type for rendering effects on preview
    public static final int TYPE_PREVIEW = 0x02;

    // Type for handling UI
    public static final int TYPE_UI = 0x04;

    public static final String ATTRIBUTE_POSITION = "positionAttr";
    public static final String ATTRIBUTE_TEXTCOORD = "textCoordAttr";

    public static final String UNIFORM_TEXTURE = "texture1i";
    public static final String UNIFORM_MVP_MATRIX = "mvpMatrix4fv";
    public static final String UNIFORM_VIEW_SIZE = "viewSize2f";


    protected Context mContext;

    protected GlProgram mProgram;

    public void setContext(final Context context){
        this.mContext = context;
    }

    public abstract String getId();

    public abstract String getProgramId();

    public abstract String getName();

    public abstract String getSummary();

    public abstract int getType();

    public void create(){
        //Log.d(TAG, "create()");

        if(mProgram == null) {

            InputStream vertexInputStream = null, fragmentInputStream = null;
            try {
                vertexInputStream = mContext.getAssets().open(com.thommil.animalsgo.Settings.ASSETS_SHADERS_PATH + this.getProgramId() + ".vert.glsl");
                fragmentInputStream = mContext.getAssets().open(com.thommil.animalsgo.Settings.ASSETS_SHADERS_PATH + this.getProgramId() + ".frag.glsl");

                mProgram = new GlProgram(vertexInputStream, fragmentInputStream);
            } catch (IOException ioe) {
                throw new RuntimeException("Failed to find shaders source : " + ioe);
            } finally {
                if (vertexInputStream != null) {
                    try {
                        vertexInputStream.close();
                    } catch (IOException ioe) {
                        Log.e(TAG, "Failed to close vertex source : " + ioe);
                    }
                }
                if (fragmentInputStream != null) {
                    try {
                        fragmentInputStream.close();
                    } catch (IOException ioe) {
                        Log.e(TAG, "Failed to close fragment source : " + ioe);
                    }
                }
            }
        }

    }

    public void free(){
        //Log.d(TAG, "delete()");
        if(mProgram != null){
            mProgram.free();;
            mProgram = null;
        }
    }

    public GlProgram getProgram() {
        return mProgram;
    }

    public void setProgram(GlProgram program) {
        mProgram = program;
    }

    public abstract void draw(final GlIntRect viewport, final int orientation);



    public static class Settings {
        // TODO implements settings definition
    }

}
