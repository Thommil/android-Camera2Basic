package com.thommil.animalsgo.gl.libgl;

import java.nio.Buffer;
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
	private final static String TAG = GlFrameBufferObject.class.getName();
		
	/**
	 * Handle to use unbind current buffer
	 */
	public static final int UNBIND_HANDLE = GLES20.GL_ZERO;
	
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
	private static int[] readSettings = null;
	
	/**
	 * Reference to the current bind color attachment
	 */
	private Attachment colorAttachment = null;
	
	/**
	 * Reference to the current bind depth attachment
	 */
	private Attachment depthAttachment = null;
	
	/**
	 * Reference to the current bind stencil attachment
	 */
	private Attachment stencilAttachment = null;
	
	/**
	 * Default constructor
	 */
	public GlFrameBufferObject(){
		//android.util.Log.d(TAG,"NEW");
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
	public void attach(final Attachment attachment, final int type){
		//android.util.Log.d(TAG,"attach("+type+")");
		switch(attachment.getAttachmentTarget()){
			case GLES20.GL_RENDERBUFFER:
				GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.handle);
				GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, type, GLES20.GL_RENDERBUFFER, attachment.getAttachmentHandle());
				GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, UNBIND_HANDLE);
				break;
			case GLES20.GL_TEXTURE_2D:
				GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.handle);
				GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, type, attachment.getAttachmentTarget(), attachment.getAttachmentHandle(), attachment.getAttachmentLevel());
				GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, UNBIND_HANDLE);
				break;
			default:
				throw new RuntimeException("No supported FBO target : "+attachment.getAttachmentTarget());
		}
		
		switch(type){
			case Attachment.TYPE_COLOR:
				this.colorAttachment = attachment;
				break;
			case Attachment.TYPE_DEPTH:
				this.depthAttachment = attachment;
				break;
			case Attachment.TYPE_STENCIL:
				this.stencilAttachment = attachment;
				break;
		}
	}
	
	/**
	 * Removes and attachment from the current FBO
	 * 
	 * @param type The attachment type of Attachement.TYPE_* to remove
	 */
	public void detach(final int type){
		//android.util.Log.d(TAG,"detach("+type+")");
		switch(type){
			case Attachment.TYPE_COLOR:
				this.colorAttachment = null;
				break;
			case Attachment.TYPE_DEPTH:
				this.depthAttachment = null;
				break;
			case Attachment.TYPE_STENCIL:
				this.stencilAttachment = null;
				break;
		}
		
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.handle);
		GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, type, GLES20.GL_RENDERBUFFER, UNBIND_HANDLE);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, UNBIND_HANDLE);
	}
	
	/**
	 * Bind the current FrameBufferObject
	 */
	public void bind(){
		//android.util.Log.d(TAG,"bind()");
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.handle);
	}
	
	/**
	 * Unbind the current FrameBufferObject
	 */
	public void unbind(){
		//android.util.Log.d(TAG,"unbind()");
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, UNBIND_HANDLE);
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
	public Buffer read(final int x, final int y, final int width, final int height){
		//android.util.Log.d(TAG,"read()");
		Buffer pixels;

		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.handle);
		
		if(GlFrameBufferObject.readSettings == null){
			GlFrameBufferObject.readSettings = new int[3];
			GLES20.glGetIntegerv(GLES20.GL_IMPLEMENTATION_COLOR_READ_TYPE, GlFrameBufferObject.readSettings, 0);
			GLES20.glGetIntegerv(GLES20.GL_IMPLEMENTATION_COLOR_READ_FORMAT, GlFrameBufferObject.readSettings, 1);
			GlFrameBufferObject.readSettings[2] = 0;
			
			switch(GlFrameBufferObject.readSettings[0]){
				case GLES20.GL_UNSIGNED_BYTE:
					switch(GlFrameBufferObject.readSettings[1]){
						case GLES20.GL_RGBA : 
							GlFrameBufferObject.readSettings[2] = 4;
							break;
						case GLES20.GL_RGB : 
							GlFrameBufferObject.readSettings[2] = 3;
							break;
						case GLES20.GL_LUMINANCE_ALPHA : 
							GlFrameBufferObject.readSettings[2] = 2;
							break;
						case GLES20.GL_LUMINANCE :
						case GLES20.GL_ALPHA :
							GlFrameBufferObject.readSettings[2] = 1;
							break;	
					}
					break;
				case GLES20.GL_UNSIGNED_SHORT:
				case GLES20.GL_UNSIGNED_SHORT_4_4_4_4:
				case GLES20.GL_UNSIGNED_SHORT_5_5_5_1:
				case GLES20.GL_UNSIGNED_SHORT_5_6_5:
					GlFrameBufferObject.readSettings[2] = 2;
					break;
			}
			if(GlFrameBufferObject.readSettings[2] == 0) throw new RuntimeException("Failed to get pixel format for current implementation");
		}
		
		pixels = ByteBuffer.allocateDirect(width * height * GlFrameBufferObject.readSettings[2]).order(ByteOrder.nativeOrder()).position(0);
		GLES20.glReadPixels(x, y, width, height, GlFrameBufferObject.readSettings[1], GlFrameBufferObject.readSettings[0], pixels);
		
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, UNBIND_HANDLE);
		
		return pixels.position(0);
	}
	
	/**
	 * Gets the current FBO status based on STATUS_*
	 * 
	 * @return A status in a int of STATUS_*
	 */
	public int getStatus(){
		//android.util.Log.d(TAG,"getStatus()");
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.handle);
		final int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, UNBIND_HANDLE);
		return status;
	}
	
	/**
	 * Free resources associated with current FrameBuffer
	 */
	public void free(){
		//android.util.Log.d(TAG,"free()");
		GLES20.glDeleteFramebuffers(1, new int[]{this.handle}, 0);
	}
	
	/**
	 * @return the colorAttachment
	 */
	public Attachment getColorAttachment() {
		return colorAttachment;
	}

	/**
	 * @return the depthAttachment
	 */
	public Attachment getDepthAttachment() {
		return depthAttachment;
	}

	/**
	 * @return the stencilAttachment
	 */
	public Attachment getStencilAttachment() {
		return stencilAttachment;
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
		public static final int TYPE_COLOR = GLES20.GL_COLOR_ATTACHMENT0;
		
		/**
		 * Attachment type for DEPTH buffer
		 */
		public static final int TYPE_DEPTH = GLES20.GL_DEPTH_ATTACHMENT;
		
		/**
		 * Attachment type for STENCIL buffer
		 */
		public static final int TYPE_STENCIL = GLES20.GL_STENCIL_ATTACHMENT;
		
		/**
		 * Gets the handle
		 * 
		 * @return The handle of the attachment for binding
		 */
		public int getAttachmentHandle();
		
		/**
		 * Gets the target identifier of the attachment (renderbuffer and textures)
		 * 
		 * @return The target identifier of the attachment (renderbuffer and textures)
		 */
		public int getAttachmentTarget();
		
		/**
		 * Gets the attachment mipmap level for texture
		 * 
		 * @return The the attachment mipmap level for texture
		 */
		public int getAttachmentLevel();
	}
}
