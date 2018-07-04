package com.thommil.animalsgo.gl.libgl;

import android.opengl.GLES20;

/**
 * Abstraction class of a renderbuffer for FBO use
 * 
 * @author Thomas MILLET
 *
 */
public class GlRenderBufferObject implements GlFrameBufferObject.Attachment{

	/**
	 * TAG log
	 */
	@SuppressWarnings("unused")
	private static final String TAG = "A_GO/GlRenderBufferObject";
	
	/**
	 * Handle to use to unbind current buffer
	 */
	public static final int UNBIND_HANDLE = GLES20.GL_NONE;

	/**
	 * RenderBuffer format for color buffer based on RGB565
	 */
	public static final int FORMAT_COLOR_RGB565 = GLES20.GL_RGB565;
	
	/**
	 * RenderBuffer format for color buffer based on RGBA4
	 */
	public static final int FORMAT_COLOR_RGBA4 = GLES20.GL_RGBA4;
	
	/**
	 * RenderBuffer format for color buffer based on RGB5_A1
	 */
	public static final int FORMAT_COLOR_RGB5_A1 = GLES20.GL_RGB5_A1;
	
	/**
	 * RenderBuffer format for 16bits depth buffer
	 */
	public static final int FORMAT_DEPTH_COMPONENT16 = GLES20.GL_DEPTH_COMPONENT16;
	
	/**
	 * RenderBuffer format for 8bits stencil buffer
	 */
	public static final int FORMAT_STENCIL_INDEX8 = GLES20.GL_STENCIL_INDEX8;
	
	/**
	 * The associated handle
	 */
	public final int handle;
	
	/**
	 * The format of the renderbuffer (FORMAT_*)
	 */
	public final int format;
	
	/**
	 * The width of the renderbuffer
	 */
	public final int width;
	
	/**
	 * The height of the renderbuffer
	 */
	public final int height;
	
	/**
	 * Default constructor
	 */
	public GlRenderBufferObject(final int format, final int width, final int height){
		final int[]handles = new int[1];
		GLES20.glGenRenderbuffers(1, handles, 0);
		this.handle = handles[0];
		this.format = format;
		this.width = width;
		this.height = height;
		
		//Create buffer
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, this.handle);
		GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, this.format, this.width, this.height);
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, UNBIND_HANDLE);
	}
	
	/**
	 * Bind the current RenderBufferObject
	 */
	public GlRenderBufferObject bind(){
		//android.util.//Log.d(TAG,"bind()");
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, this.handle);
		return this;
	}
	
	/**
	 * Unbind the current RenderBufferObject
	 */
	public GlRenderBufferObject unbind(){
		//android.util.//Log.d(TAG,"unbind()");
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, UNBIND_HANDLE);
		return this;
	}
	/**
	 * Free resources associated with current RenderBuffer
	 */
	public GlRenderBufferObject free(){
		//android.util.//Log.d(TAG,"free()");
		GLES20.glDeleteRenderbuffers(1, new int[]{this.handle}, 0);
		return this;
	}
	
	/* (non-Javadoc)
	 * @see fr.kesk.libgl.buffer.FrameBufferObject.Attachment#getHandle()
	 */
	@Override
	public int getHandle() {
		//android.util.//Log.d(TAG,"getHandle()");
		return this.handle;
	}

	/* (non-Javadoc)
	 * @see fr.kesk.libgl.buffer.FrameBufferObject.Attachment#getTarget()
	 */
	@Override
	public int getTarget() {
		//android.util.//Log.d(TAG,"getTarget()");
		return GLES20.GL_RENDERBUFFER;
	}

	/* (non-Javadoc)
	 * @see fr.kesk.libgl.buffer.FrameBufferObject.Attachment#getLevel()
	 */
	@Override
	public int getLevel() {
		//android.util.//Log.d(TAG,"getLevel()");
		return 0;
	}	
}
