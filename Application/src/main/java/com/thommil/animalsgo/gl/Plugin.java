package com.thommil.animalsgo.gl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

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

    protected Context mContext;

    protected GlProgram mProgram;

    public void setContext(final Context context){
        this.mContext = context;
    }

    public abstract String getId();

    public abstract String getName();

    public abstract String getSummary();

    public abstract int getType();

    public void create(){
        Log.d(TAG, "create()");
        InputStream vertexInputStream = null, fragmentInputStream = null;
        try {
            vertexInputStream = mContext.getAssets().open(this.getId() + ".vert.glsl");
            fragmentInputStream = mContext.getAssets().open(this.getId() + ".frag.glsl");

            mProgram = new GlProgram(vertexInputStream, fragmentInputStream);
        }catch(IOException ioe){
            throw new RuntimeException("Failed to find shaders source.");
        }finally {
            if(vertexInputStream != null) {
                try {
                    vertexInputStream.close();
                }catch(IOException ioe){
                    Log.e(TAG,"Failed to close vertex source : " + ioe);
                }
            }
            if(fragmentInputStream!= null) {
                try {
                    fragmentInputStream.close();
                }catch(IOException ioe){
                    Log.e(TAG,"Failed to close fragment source : " + ioe);
                }
            }
        }
    }

    public abstract void draw(final GlIntRect viewport, final int orientation);

    public void delete(){
        Log.d(TAG, "delete()");
        if(mProgram != null){
            mProgram.free();;
            mProgram = null;
        }
    }

    public static class Settings {
        // TODO implements settings definition
    }

}
