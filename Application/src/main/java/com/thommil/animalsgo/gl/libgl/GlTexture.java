package com.thommil.animalsgo.gl.libgl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;

import android.opengl.GLES20;

/**
 * Abstraction class to define a texture source data 
 * 
 * @author Thomas MILLET
 */
public abstract class GlTexture implements GlFrameBufferObject.Attachment{

	/**
	 * Handle for unbound texture
	 */
	public static final int UNBIND_HANDLE = GLES20.GL_NONE;
	
	/**
	 * Texture is a standard 2D texture
	 */
	public static final int TARGET_TEXTURE_2D = GLES20.GL_TEXTURE_2D;
	
	/**
	 * Texture format ALPHA
	 * 
	 * Each element is a single alpha component.
	 * 
	 * The GL converts it to floating point and assembles it into
	 * an RGBA element by attaching 0 for red, green, and blue.
	 * Each component is then clamped to the range [0,1].
	 */
	public static final int FORMAT_ALPHA = GLES20.GL_ALPHA;
	
	/**
	 * Texture format RGB
	 * 
	 * Each element is an RGB triple.
	 * 
	 * The GL converts it to floating point and assembles it into
	 * an RGBA element by attaching 1 for alpha.
	 * Each component is then clamped to the range [0,1].
	 */
	public static final int FORMAT_RGB = GLES20.GL_RGB;
	
	/**
	 * Texture format RGBA
	 * 
	 * Each element contains all four components. 
	 * The GL converts it to floating point. 
	 * Each component is clamped to the range [0,1].
	 */
	public static final int FORMAT_RGBA = GLES20.GL_RGBA;
	
	/**
	 * Texture format LUMINANCE
	 * 
	 * Each element is a single luminance value.
	 * The GL converts it to floating point, then assembles it into
	 * an RGBA element by replicating the luminance value three times
	 * for red, green, and blue and attaching 1 for alpha. 
	 * Each component is then clamped to the range [0,1].
	 */
	public static final int FORMAT_LUMINANCE = GLES20.GL_LUMINANCE;
	
	/**
	 * Texture format LUMINANCE_ALPHA
	 * 
	 * Each element is a luminance/alpha pair. 
	 * The GL converts it to floating point, then assembles it into 
	 * an RGBA element by replicating the luminance value three times 
	 * for red, green, and blue. 
	 * Each component is then clamped to the range [0,1].
	 */
	public static final int FORMAT_LUMINANCE_ALPHA = GLES20.GL_LUMINANCE_ALPHA;
	
	/**
	 * Indicates no compression type (other types are available in {@link GlTexture}
	 */
	public static final int COMP_FALSE = 0;
	
	/**
	 * Compression format for ETC1
	 */
	public final static int COMP_ETC1 = 0x8d64; 
	
	/**
	 * Compression format for ATITC for RGB textures
	 */
	public final static int COMP_ATC_RGB_AMD = 0x8C92;
	
	/**
	 * Compression format for ATITC for RGBA textures using explicit alpha encoding
	 */
	public final static int COMP_ATC_RGBA_EXPLICIT_ALPHA_AMD = 0x8C93;
	
	/**
	 * Compression format for ATITC for RGBA textures using interpolated alpha encoding
	 */
	public final static int COMP_RGBA_INTERPOLATED_ALPHA_AMD = 0x87EE;
	 
	/**
	 * Compression format for PVRTC RGB 4bits
	 */
	public final static int COMP_RGB_PVRTC_4BPPV1_IMG = 0x8C00;
	
	/**
	 * Compression format for PVRTC RGB 2bits
	 */
	public final static int COMP_RGB_PVRTC_2BPPV1_IMG = 0x8C01;
	
	/**
	 * Compression format for PVRTC RGBA 4bits
	 */
	public final static int COMP_RGBA_PVRTC_4BPPV1_IMG = 0x8C02;
	
	/**
	 * Compression format for PVRTC RGBA 2bits
	 */
	public final static int COMP_RGBA_PVRTC_2BPPV1_IMG = 0x8C03;
	
	/**
	 * Compression format for S3TC DXT1 RGB
	 */
	public final static int COMP_RGB_S3TC_DXT1_EXT = 0x83F1;
	
	/**
	 * Compression format for S3TC DXT1 RGBA
	 */
	public final static int COMP_RGBA_S3TC_DXT1_EXT = 0x83F2;
	
	/**
	 * Compression format for S3TC DXT3
	 */
	public final static int COMP_RGBA_S3TC_DXT3_ANGLE = 0x83F3;
	
	/**
	 * Compression format for S3TC DXT5 RGB
	 */
	public final static int COMP_RGBA_S3TC_DXT5_ANGLE = 0x83F0;
		
	/**
	 * Compression format for 3DC X
	 */
	public final static int COMP_3DC_X_AMD = 0x87F9;
	
	/**
	 * Compression format for 3DC XY
	 */
	public final static int COMP_3DC_XY_AMD = 0x87FA;
	
	
	
	
	
	/**
	 * Texture color type for GL_UNSIGNED_BYTE
	 */
	public static final int TYPE_UNSIGNED_BYTE = GLES20.GL_UNSIGNED_BYTE;
	
	/**
	 * Size of a pixel using type GL_UNSIGNED_BYTE
	 */
	public static final int SIZEOF_UNSIGNED_BYTE = 4;
	
	/**
	 * Texture color type for TYPE_UNSIGNED_SHORT_5_6_5
	 */
	public static final int TYPE_UNSIGNED_SHORT_5_6_5 = GLES20.GL_UNSIGNED_SHORT_5_6_5;
	
	/**
	 * Size of a pixel using type GL_UNSIGNED_SHORT_5_6_5
	 */
	public static final int SIZEOF_UNSIGNED_SHORT_5_6_5 = 2;
	
	/**
	 * Texture color type for TYPE_UNSIGNED_SHORT_4_4_4_4
	 */
	public static final int TYPE_UNSIGNED_SHORT_4_4_4_4 = GLES20.GL_UNSIGNED_SHORT_4_4_4_4;
	
	/**
	 * Size of a pixel using type GL_UNSIGNED_SHORT_4_4_4_4
	 */
	public static final int SIZEOF_UNSIGNED_SHORT_4_4_4_4 = 2;
	
	/**
	 * Texture color type for TYPE_UNSIGNED_SHORT_5_5_5_1
	 */
	public static final int TYPE_UNSIGNED_SHORT_5_5_5_1 = GLES20.GL_UNSIGNED_SHORT_5_5_5_1;
	
	/**
	 * Size of a pixel using type GL_UNSIGNED_SHORT_5_5_5_1
	 */
	public static final int SIZEOF_UNSIGNED_SHORT_5_5_5_1 = 2;
	
	
	
	
	
	/**
	 * Indicate the wrap mode for the "s" axe
	 */
	public static final int WRAP_MODE_S = GLES20.GL_TEXTURE_WRAP_S;
	
	/**
	 * Indicate the wrap mode for the "t" axe
	 */
	public static final int WRAP_MODE_T = GLES20.GL_TEXTURE_WRAP_T;
	
	/**
	 * Wrapping mode : repeat the texture
	 */
	public static final int WRAP_REPEAT = GLES20.GL_REPEAT;
	
	/**
	 * Wrapping mode : clamp fetches to the edge of the texture
	 */
	public static final int WRAP_CLAMP_TO_EDGE = GLES20.GL_CLAMP_TO_EDGE;
	
	/**
	 * Wrapping mode : repeat using mirrored image
	 */
	public static final int WRAP_MIRRORED_REPEAT = GLES20.GL_MIRRORED_REPEAT;
	
	
	
	
	
	/**
	 * Magnification settings for lowest quality
	 */
	public static final int MAG_FILTER_LOW = GLES20.GL_NEAREST;
	
	/**
	 * Magnification settings for highest quality
	 */
	public static final int MAG_FILTER_HIGH = GLES20.GL_LINEAR;
	
	
	
	
	
	/**
	 * Minification settings for lowest quality without mipmaping
	 */
	public static final int MIN_FILTER_LOW = GLES20.GL_NEAREST;
	
	/**
	 * Minification settings for highest quality without mipmaping
	 */
	public static final int MIN_FILTER_HIGH = GLES20.GL_LINEAR;
	
	/**
	 * Minification settings for lowest quality with mipmaping
	 */
	public static final int MIN_FILTER_MIPMAP_LOW = GLES20.GL_NEAREST_MIPMAP_NEAREST;
	
	/**
	 * Minification settings for medium quality with mipmaping
	 */
	public static final int MIN_FILTER_MIPMAP_MEDIUM = GLES20.GL_NEAREST_MIPMAP_LINEAR;
	
	/**
	 * Minification settings for high quality with mipmaping
	 */
	public static final int MIN_FILTER_MIPMAP_BILINEAR = GLES20.GL_LINEAR_MIPMAP_NEAREST;
	
	/**
	 * Minification settings for highest quality with mipmaping
	 */
	public static final int MIN_FILTER_MIPMAP_TRILINEAR = GLES20.GL_LINEAR_MIPMAP_LINEAR;
		
	
	/**
	 * Flags for compression format support
	 */
	public static float[] textureSupportFlags = null;
	
	/**
	 * GL Handle for binding
	 */
	public int handle = UNBIND_HANDLE;

	/**
	 * Default constructor
	 */
	public GlTexture(){
		//android.util.Log.d(TAG,"NEW");
		final int[]handles = new int[1];
		GLES20.glGenTextures(1, handles, 0);
		this.handle = handles[0];
	}

	/**
	 * Create texture on GPU based on current format, type, data ...
	 */
	public GlTexture allocate(){
		GLES20.glTexImage2D(getTarget(), 0, getFormat(), getWidth(), getHeight(), 0, getFormat(), getType(), getBytes());
		GLES20.glTexParameteri(getTarget(), WRAP_MODE_S, getWrapMode(WRAP_MODE_S));
		GLES20.glTexParameteri(getTarget(), WRAP_MODE_T, getWrapMode(WRAP_MODE_T));
		GLES20.glTexParameteri(getTarget(), GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameteri(getTarget(), GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
		return this;
	}

	/**
	 * Bind the current texture to default GPU active texture GL_TEXTURE0
	 */
	public GlTexture bind(){
		this.bind(GLES20.GL_TEXTURE0);
		return this;
	}
	
	/**
	 * Bind the current texture to GPU active texture activeTexture  
	 * 
	 * @param activeTexture The GPU active texture to use
	 */
	public GlTexture bind(final int activeTexture){
		//android.util.Log.d(TAG,"bind("+activeTexture+")");
		GLES20.glActiveTexture(activeTexture);
		GLES20.glBindTexture(getTarget(), this.handle);
		return this;
	}
	
	/**
	 * Unbind the current texture to default GPU active texture GL_TEXTURE0
	 */
	public GlTexture unbind(){
		this.unbind(GLES20.GL_TEXTURE0);
		return this;
	}

	/**
	 * Unbind the current texture
	 *
	 * @param activeTexture The GPU active texture to use
	 */
	public GlTexture unbind(final int activeTexture){
		//android.util.Log.d(TAG,"bind("+activeTexture+")");
		GLES20.glActiveTexture(activeTexture);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, UNBIND_HANDLE);
		return this;
	}

	/**
	 * Get the bytes for this Texture
	 * 
	 * @return The bytes of texture in a ByteBuffer 
	 */
	public ByteBuffer getBytes(){
		return null;
	}

	/**
	 * Get the source image height
	 * 
	 * @return The source image height
	 */
	public int getHeight(){
		return 0;
	}
	
	/**
	 * Get the source image width
	 * 
	 * @return The source image width
	 */
	public int getWidth(){
		return 0;
	}
	
	/**
	 * Get the GL target of texture
	 * 
	 * Default implementation use TARGET_TEXTURE_2D
	 * 
	 * @return TARGET_TEXTURE_2D or TARGET_TEXTURE_CUBE
	 */
	@Override
	public int getTarget(){
		return TARGET_TEXTURE_2D;
	}
	
	/**
	 * Get the texture color format
	 * 
	 * @return FORMAT_ALPHA, FORMAT_RGB, FORMAT_RGBA, FORMAT_LUMINANCE or FORMAT_LUMINANCE_ALPHA
	 */
	public int getFormat(){
		return FORMAT_RGBA;
	}
	
	/**
	 * Get the texture color type
	 * 
	 * @return TYPE_UNSIGNED_BYTE, TYPE_UNSIGNED_SHORT_5_6_5, TYPE_UNSIGNED_SHORT_4_4_4_4 or TYPE_UNSIGNED_SHORT_5_5_5_1
	 */
	public int getType(){
		return TYPE_UNSIGNED_BYTE;
	}
	
	/**
	 * Indicates if the current texture uses compression and
	 * which compression format
	 * 
	 * @return The compression format, COMP_FALSE for no compression
	 */
	public int getCompressionFormat(){
		return COMP_FALSE;
	}
	
	/**
	 * Get the wrapping mode of texture for the given axe 
	 * 
	 * Default implementation returns WRAP_REPEAT
	 * 
	 * @param axeId The axe GL id (WRAP_MODE_S or WRAP_MODE_T)
	 * 
	 * @return WRAP_REPEAT, WRAP_CLAMP_TO_EDGE or WRAP_MIRRORED_REPEAT
	 */
	public int getWrapMode(final int axeId){
		return WRAP_REPEAT;
	}
	
	/**
	 * Get the magnification filter setting
	 * 
	 * Subclass should override this method to set filter
	 * 
	 * Default implementation returns MAG_FILTER_LOW 
	 * 
	 * @return MAG_FILTER_LOW or MAG_FILTER_HIGH
	 */
	public int getMagnificationFilter(){
		return MAG_FILTER_LOW;
	}
	
	/**
	 * Get the magnification filter setting
	 * 
	 * Subclass should override this method to set filter
	 * 
	 * Default implementation returns MIN_FILTER_MIPMAP_MEDIUM 
	 * 
	 * @return MIN_FILTER_LOW, MIN_FILTER_HIGH, MIN_FILTER_MIPMAP_LOW,
	 * 		   MIN_FILTER_MIPMAP_MEDIUM, MIN_FILTER_MIPMAP_BILINEAR
	 * 		   or MIN_FILTER_MIPMAP_TRILINEAR
	 */
	public int getMinificationFilter(){
		return MIN_FILTER_LOW;
	}
	
	/**
	 * Get the current texture buffer size
	 * 
	 * @return The texture buffer size in bytes
	 */
	public int getSize() {
		switch (this.getType()) {
			case TYPE_UNSIGNED_SHORT_4_4_4_4:
				return getWidth() * getHeight() * Short.BYTES;
			case TYPE_UNSIGNED_SHORT_5_5_5_1:
				return getWidth() * getHeight() * Short.BYTES;
			case TYPE_UNSIGNED_SHORT_5_6_5:
				return getWidth() * getHeight() * Short.BYTES;
			default:
				return getWidth() * getHeight() * Integer.BYTES;
		}
	}

	/**
	 * Removes texture from GPU 
	 */
	public void free(){
		//android.util.Log.d(TAG,"free()");
		final int textureHandle[] = new int[]{this.handle};
		GLES20.glDeleteTextures(1, textureHandle, 0);
	}
	
	/**
	 * Indicates if the format is supported by current device
	 * 
	 * ! Warning : must be called in GL Thread only
	 * 
	 * @param format The compression format FORMAT_ATITC, FORMAT_PVRTC ...
	 * 
	 * @return true if format is supported, false otherwise
	 */
	public static boolean isCompressionFormatSupported(final int format){
		//android.util.Log.d(TAG,"isCompressionFormatSupported("+format+")");
		
		//Initialize if not done
		if(textureSupportFlags == null){
			textureSupportFlags = GlOperation.glGetState(GLES20.GL_COMPRESSED_TEXTURE_FORMATS);
			Arrays.sort(textureSupportFlags);
		}
		
		return Arrays.binarySearch(textureSupportFlags, (float)format) >= 0;
	}

	/* (non-Javadoc)
	 * @see fr.kesk.libgl.buffer.FrameBufferObject.Attachment#getHandle()
	 */
	@Override
	public int getHandle() {
		//android.util.Log.d(TAG,"getHandle()");
		return this.handle;
	}

	/* (non-Javadoc)
	 * @see fr.kesk.libgl.buffer.FrameBufferObject.Attachment#getLevel()
	 */
	@Override
	public int getLevel() {
		//android.util.Log.d(TAG,"getLevel()");
		
		//Default level set to 0, can be overridden
		return 0;
	}	
}
