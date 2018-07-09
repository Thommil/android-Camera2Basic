package com.thommil.animalsgo.gl.libgl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.opengl.GLES20;

/**
 * Abstraction class for FBO use 
 * 
 * @author Thomas MILLET
 *
 */
public class GlFrameBufferObject {

	/**
	 * TAG log
	 */
	@SuppressWarnings("unused")
	private static final String TAG = "A_GO/GlFrameBufferObject";
		
	/**
	 * Handle to use unbind current buffer
	 */
	public static final int UNBIND_HANDLE = GLES20.GL_NONE;
	
	/**
	 * Status -> FrameBuffer is complete
	 */
	public static final int STATUS_COMPLETE = GLES20.GL_FRAMEBUFFER_COMPLETE;
	
	/**
	 * Status -> FrameBuffer attachments are not valid
	 */
	public static final int STATUS_INCOMPLETE_ATTACHMENT = GLES20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT;
	
	/**
	 * Status -> FrameBuffer is missing attachment requirements
	 */
	public static final int STATUS_INCOMPLETE_MISSING_ATTACHMENT = GLES20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT;
	
	/**
	 * Status -> FrameBuffer is missing dimensions
	 */
	public static final int STATUS_INCOMPLETE_DIMENSIONS = GLES20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS;
		
	/**
	 * Status -> FrameBuffer is using a bad combination of formats and targets
	 */
	public static final int STATUS_UNSUPPORTED = GLES20.GL_FRAMEBUFFER_UNSUPPORTED;
	
	/**
	 * The associated handle
	 */
	public final int handle;
	
	/**
	 * Contains the implementation specific settings to read buffer
	 */
	private int[] mReadSettings = null;
	
	/**
	 * Reference to the current bind color attachment
	 */
	private Attachment mColorAttachment = null;
	
	/**
	 * Reference to the current bind depth attachment
	 */
	private Attachment mDepthAttachment = null;
	
	/**
	 * Reference to the current bind stencil attachment
	 */
	private Attachment mStencilAttachment = null;
	
	/**
	 * Default constructor
	 */
	public GlFrameBufferObject(){
		final int[]handles = new int[1];
		GLES20.glGenFramebuffers(1, handles, 0);
		this.handle = handles[0];
	}
	
	/**
	 * Set an attachment to current FrameBufferObject
	 * 
	 * @param attachment The attachment to set
	 * @param type The attachment type of Attachement.TYPE_*
	 */
	public GlFrameBufferObject attach(final Attachment attachment, final int type){
		////Log.d(TAG,"attach("+type+")");
		switch(attachment.getTarget()){
			case GLES20.GL_RENDERBUFFER:
				GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.handle);
				GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, type, GLES20.GL_RENDERBUFFER, attachment.getHandle());
				GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, UNBIND_HANDLE);
                GlOperation.checkGlError(TAG, "glFramebufferRenderbuffer");
				break;
			case GLES20.GL_TEXTURE_2D:
				GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.handle);
				GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, type, attachment.getTarget(), attachment.getHandle(), attachment.getLevel());
				GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, UNBIND_HANDLE);
                GlOperation.checkGlError(TAG, "glFramebufferTexture2D");
				break;
			default:
				throw new RuntimeException("No supported FBO target : "+attachment.getTarget());
		}

        if (getStatus() != GlFrameBufferObject.STATUS_COMPLETE) {
            GlOperation.checkGlError(TAG,"FBO status");
        }
		
		switch(type){
			case Attachment.TYPE_COLOR:
				this.mColorAttachment = attachment;
				break;
			case Attachment.TYPE_DEPTH:
				this.mDepthAttachment = attachment;
				break;
			case Attachment.TYPE_STENCIL:
				this.mStencilAttachment = attachment;
				break;
		}
		return this;
	}
	
	/**
	 * Removes and attachment from the current FBO
	 * 
	 * @param type The attachment type of Attachement.TYPE_* to remove
	 */
	public GlFrameBufferObject detach(final int type){
		////Log.d(TAG,"detach("+type+")");
		switch(type){
			case Attachment.TYPE_COLOR:
				this.mColorAttachment = null;
				break;
			case Attachment.TYPE_DEPTH:
				this.mDepthAttachment = null;
				break;
			case Attachment.TYPE_STENCIL:
				this.mStencilAttachment = null;
				break;
		}
		
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.handle);
		GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, type, GLES20.GL_RENDERBUFFER, UNBIND_HANDLE);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, UNBIND_HANDLE);
        GlOperation.checkGlError(TAG, "glFramebufferRenderbuffer");
		mReadSettings = null;
		return this;
	}
	
	/**
	 * Bind the current FrameBufferObject
	 */
	public GlFrameBufferObject bind(){
		////Log.d(TAG,"bind()");
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.handle);
		return this;
	}
	
	/**
	 * Unbind the current FrameBufferObject
	 */
	public GlFrameBufferObject unbind(){
		////Log.d(TAG,"unbind()");
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, UNBIND_HANDLE);
		return this;
	}
	
	/**
	 * Reads pixels from current FBO or EGL Surface and stores it in a Buffer
	 * 
	 * @param x The x coordinate of the lower left corner of the area to read
	 * @param y The y coordinate of the lower left corner of the area to read
	 * @param width The width of the area to read
	 * @param height The height of the area to read
	 * 
	 * @return A Buffer containing the pixels
	 */
	public ByteBuffer read(final int x, final int y, final int width, final int height){
		////Log.d(TAG,"read()");
		ByteBuffer pixels;

		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.handle);
		
		if(mReadSettings == null){
			mReadSettings = new int[3];
			GLES20.glGetIntegerv(GLES20.GL_IMPLEMENTATION_COLOR_READ_TYPE, mReadSettings, 0);
			GLES20.glGetIntegerv(GLES20.GL_IMPLEMENTATION_COLOR_READ_FORMAT, mReadSettings, 1);
			mReadSettings[2] = 0;
			
			switch(mReadSettings[0]){
				case GLES20.GL_UNSIGNED_BYTE:
					switch(mReadSettings[1]){
						case GLES20.GL_RGBA :
							mReadSettings[2] = 4;
							break;
						case GLES20.GL_RGB :
							mReadSettings[2] = 3;
							break;
						case GLES20.GL_LUMINANCE_ALPHA :
							mReadSettings[2] = 2;
							break;
						case GLES20.GL_LUMINANCE :
						case GLES20.GL_ALPHA :
							mReadSettings[2] = 1;
							break;	
					}
					break;
				case GLES20.GL_UNSIGNED_SHORT:
				case GLES20.GL_UNSIGNED_SHORT_4_4_4_4:
				case GLES20.GL_UNSIGNED_SHORT_5_5_5_1:
				case GLES20.GL_UNSIGNED_SHORT_5_6_5:
					mReadSettings[2] = 2;
					break;
			}
			if(mReadSettings[2] == 0) throw new RuntimeException("Failed to get pixel format for current implementation");
		}

		pixels = ByteBufferPool.getInstance().getDirectByteBuffer(width * height * mReadSettings[2]);

		switch(mReadSettings[1]){
			case GLES20.GL_RGBA :
				pixels.order(ByteOrder.LITTLE_ENDIAN);
				break;
			default:
				pixels.order(ByteOrder.nativeOrder());
		}
		GLES20.glReadPixels(x, y, width, height, mReadSettings[1], mReadSettings[0], pixels);

		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, UNBIND_HANDLE);

		return pixels;
	}
	
	/**
	 * Gets the current FBO status based on STATUS_*
	 * 
	 * @return A status in a int of STATUS_*
	 */
	public int getStatus(){
		////Log.d(TAG,"getStatus()");
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.handle);
		final int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, UNBIND_HANDLE);
		return status;
	}
	
	/**
	 * Free resources associated with current FrameBuffer
	 */
	public GlFrameBufferObject free(){
		////Log.d(TAG,"free()");
		GLES20.glDeleteFramebuffers(1, new int[]{this.handle}, 0);
        GlOperation.checkGlError(TAG, "glDeleteTextures");
		return this;
	}
	
	/**
	 * @return the mColorAttachment
	 */
	public Attachment getColorAttachment() {
		return mColorAttachment;
	}

	/**
	 * @return the mDepthAttachment
	 */
	public Attachment getDepthAttachment() {
		return mDepthAttachment;
	}

	/**
	 * @return the mStencilAttachment
	 */
	public Attachment getStencilAttachment() {
		return mStencilAttachment;
	}

	/**
	 * Interface to be implemented by FBO attachment targets
	 * 
	 * @author Thomas MILLET
	 */
	public static interface Attachment{
		
		/**
		 * Attachment type for COLOR buffer
		 */
		int TYPE_COLOR = GLES20.GL_COLOR_ATTACHMENT0;
		
		/**
		 * Attachment type for DEPTH buffer
		 */
		int TYPE_DEPTH = GLES20.GL_DEPTH_ATTACHMENT;
		
		/**
		 * Attachment type for STENCIL buffer
		 */
		int TYPE_STENCIL = GLES20.GL_STENCIL_ATTACHMENT;
		
		/**
		 * Gets the handle
		 * 
		 * @return The handle of the attachment for binding
		 */
		int getHandle();
		
		/**
		 * Gets the target identifier of the attachment (renderbuffer and textures)
		 * 
		 * @return The target identifier of the attachment (renderbuffer and textures)
		 */
		int getTarget();
		
		/**
		 * Gets the attachment configure level for texture
		 * 
		 * @return The the attachment configure level for texture
		 */
		int getLevel();
	}
}
