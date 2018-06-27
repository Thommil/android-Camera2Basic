package com.thommil.animalsgo.gl;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.IOException;

/**
 * Define a plugin used by CameraRenderer.
 */
public abstract class RendererPlugin{

    private static final String TAG = "A_GO/RendererPlugin";

    protected Context mContext;

    protected int mPluginShaderProgram;

    public void setContext(final Context context){
        this.mContext = context;
    }

    public abstract String getId();

    public abstract String getName();

    public abstract String getSummary();

    public abstract boolean isPreviewPlugin();

    public abstract boolean isCardPlugin();

    public void create(){
        Log.d(TAG, "create()");
        try {
            final int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
            final String vertexShaderCode = ShaderUtils.getStringFromFileInAssets(mContext, this.getId() + ".vert.glsl");
            GLES20.glShaderSource(vertexShaderHandle, vertexShaderCode);
            GLES20.glCompileShader(vertexShaderHandle);
            CameraRenderer.checkGlError("Vertex shader compile");

            Log.d(TAG, "vertexShader info log:\n " + GLES20.glGetShaderInfoLog(vertexShaderHandle));

            final int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
            final String fragmentShaderCode = ShaderUtils.getStringFromFileInAssets(mContext, this.getId() + ".frag.glsl");
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShaderCode);
            GLES20.glCompileShader(fragmentShaderHandle);
            CameraRenderer.checkGlError("Pixel shader compile");

            Log.d(TAG, "fragmentShader info log:\n " + GLES20.glGetShaderInfoLog(fragmentShaderHandle));

            mPluginShaderProgram = GLES20.glCreateProgram();
            GLES20.glAttachShader(mPluginShaderProgram, vertexShaderHandle);
            GLES20.glAttachShader(mPluginShaderProgram, fragmentShaderHandle);
            GLES20.glLinkProgram(mPluginShaderProgram);
            CameraRenderer.checkGlError("Shader program compile");

            final int[] status = new int[1];
            GLES20.glGetProgramiv(mPluginShaderProgram, GLES20.GL_LINK_STATUS, status, 0);
            if (status[0] != GLES20.GL_TRUE) {
                String error = GLES20.glGetProgramInfoLog(mPluginShaderProgram);
                CameraRenderer.checkGlError("Error while linking program:\n" + error);
            }
        }catch(IOException ioe){
            throw new RuntimeException("Failed to find shaders source.");
        }
    }

    public abstract void draw(final int texId, final int width, final int height);


    public void delete(){
        Log.d(TAG, "create()");
        GLES20.glDeleteProgram(mPluginShaderProgram);
    }

    public static class Settings {
        // TODO implements settings definition
    }
}
