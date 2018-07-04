package com.thommil.animalsgo.gl.libgl;

import android.opengl.GLES20;
import android.opengl.GLException;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * Tools class used to operate on OpenGL pipeline :
 * <ul>
 * 	<li>Scissor Box testing</li>
 * 	<li>Stencil buffer testing</li>
 * 	<li>Depth buffer testing</li>
 * 	<li>Multisampling</li>
 * 	<li>Blending</li>
 * 	<li>Dithering</li>
 *  <li>Culling</li>
 * </ul>
 * <br/>
 * This class is mainly here to help as a reminder to use vertex and fragment operations and should be ommitted
 * for better portability. Direct calls should be used instead.
 * 
 * @author Thomas MILLET
 *
 */
public final class GlOperation {
	
	/**
	 * TAG log
	 */
	@SuppressWarnings("unused")
	private final static String TAG = "A_GO/GlOperation";
	
	/**
	 * Flag for Color buffer
	 */
	public static final int BUFFER_COLOR = GLES20.GL_COLOR_BUFFER_BIT;
	
	/**
	 * Flag for Depth buffer
	 */
	public static final int BUFFER_DEPTH = GLES20.GL_DEPTH_BUFFER_BIT;
	
	/**
	 * Flag for Stencil buffer
	 */
	public static final int BUFFER_STENCIL = GLES20.GL_STENCIL_BUFFER_BIT;
	
	/**
	 * Flag for depth test
	 */
	public static final int TEST_DEPTH = GLES20.GL_DEPTH_TEST;
	
	/**
	 * Flag for stencil test
	 */
	public static final int TEST_STENCIL = GLES20.GL_STENCIL_TEST;
	
	/**
	 * Flag for blend test
	 */
	public static final int TEST_BLEND = GLES20.GL_BLEND;
	
	/**
	 * Flag for dither test
	 */
	public static final int TEST_DITHER = GLES20.GL_DITHER;
	
	/**
	 * Flag for scissor test
	 */
	public static final int TEST_SCISSOR = GLES20.GL_SCISSOR_TEST;
	
	/**
	 * Flag for culling test
	 */
	public static final int TEST_CULLING = GLES20.GL_CULL_FACE;
	
	/**
	 * Flag for sample coverage test
	 */
	public static final int TEST_SAMPLE_COVERAGE = GLES20.GL_SAMPLE_COVERAGE;
	
	/**
	 * Flag for sample alpha to coverage test
	 */
	public static final int TEST_SAMPLE_ALPHA_TO_COVERAGE = GLES20.GL_SAMPLE_ALPHA_TO_COVERAGE;
	
	/**
	 * Function for test comparison for EQUAL
	 */
	public static final int FUNCTION_EQUAL = GLES20.GL_EQUAL;
	
	/**
	 * Function for test comparison for NOT_EQUAL
	 */
	public static final int FUNCTION_NOT_EQUAL = GLES20.GL_NOTEQUAL;
	
	/**
	 * Function for test comparison for LESS
	 */
	public static final int FUNCTION_LESS = GLES20.GL_LESS;
	
	/**
	 * Function for test comparison for GREATER
	 */
	public static final int FUNCTION_GREATER = GLES20.GL_GREATER;
	
	/**
	 * Function for test comparison for LESS OR EQUAL
	 */
	public static final int FUNCTION_LESS_OR_EQUAL = GLES20.GL_LEQUAL;
	
	/**
	 * Function for test comparison for GREATER OR EQUAL
	 */
	public static final int FUNCTION_GREATER_OF_EQUAL = GLES20.GL_GEQUAL;
	
	/**
	 * Function for test comparison for ALWAYS
	 */
	public static final int FUNCTION_ALWAYS = GLES20.GL_ALWAYS;
	
	/**
	 * Function for test comparison for NEVER
	 */
	public static final int FUNCTION_NEVER = GLES20.GL_NEVER;
	
	/**
	 * Outcome after stencil test -> set to 0
	 */
	public static final int STENCIL_OUT_ZERO = GLES20.GL_ZERO;
	
	/**
	 * Outcome after stencil test -> replace by the reference
	 */
	public static final int STENCIL_OUT_REPLACE = GLES20.GL_REPLACE;
	
	/**
	 * Outcome after stencil test -> increment
	 */
	public static final int STENCIL_OUT_INCR = GLES20.GL_INCR;
	
	/**
	 * Outcome after stencil test -> decrement
	 */
	public static final int STENCIL_OUT_DECR = GLES20.GL_DECR;
	
	/**
	 * Outcome after stencil test -> increment with cycling to max
	 */
	public static final int STENCIL_OUT_INCR_WRAP = GLES20.GL_INCR_WRAP;
	
	/**
	 * Outcome after stencil test -> increment with cycling to min
	 */
	public static final int STENCIL_OUT_DECR_WRAP = GLES20.GL_DECR_WRAP;
	
	/**
	 * Outcome after stencil test -> do nothing
	 */
	public static final int STENCIL_OUT_KEEP = GLES20.GL_KEEP;
	
	/**
	 * Outcome after stencil test -> bitwise inversion
	 */
	public static final int STENCIL_OUT_INVERT = GLES20.GL_INVERT;
	
	/**
	 * Blend factor test -> RGB : 0, 0, 0  Alpha : 0 
	 */
	public static final int BLEND_FACTOR_ZERO = GLES20.GL_ZERO;
	
	/**
	 * Blend factor test -> RGB : 1, 1, 1  Alpha : 1 
	 */
	public static final int BLEND_FACTOR_ONE = GLES20.GL_ONE;
	
	/**
	 * Blend factor test -> RGB : SRCR, SRCG, SRCB  Alpha : SRCA 
	 */
	public static final int BLEND_FACTOR_SRC_COLOR = GLES20.GL_SRC_COLOR;
	
	/**
	 * Blend factor test -> RGB : 1-SRCR, 1-SRCG, 1-SRCB  Alpha : 1-SRCA 
	 */
	public static final int BLEND_FACTOR_ONE_MINUS_SRC_COLOR = GLES20.GL_ONE_MINUS_SRC_COLOR;
	
	/**
	 * Blend factor test -> RGB : SRCA, SRCA, SRCA  Alpha : SRCA 
	 */
	public static final int BLEND_FACTOR_SRC_ALPA = GLES20.GL_SRC_ALPHA;
	
	/**
	 * Blend factor test -> RGB : 1-SRCA, 1-SRCA, 1-SRCA  Alpha : 1-SRCA 
	 */
	public static final int BLEND_FACTOR_ONE_MINUS_SRC_ALPA = GLES20.GL_ONE_MINUS_SRC_ALPHA;
	
	/**
	 * Blend factor test -> RGB : DSTR, DSTG, DSTB  Alpha : DSTA 
	 */
	public static final int BLEND_FACTOR_DST_COLOR = GLES20.GL_DST_COLOR;
	
	/**
	 * Blend factor test -> RGB : 1-DSTR, 1-DSTG, 1-DSTB  Alpha : 1-DSTA 
	 */
	public static final int BLEND_FACTOR_ONE_MINUS_DST_COLOR = GLES20.GL_ONE_MINUS_DST_COLOR;
	
	/**
	 * Blend factor test -> RGB : DSTA, DSTA, DSTA  Alpha : DSTA 
	 */
	public static final int BLEND_FACTOR_DST_ALPA = GLES20.GL_DST_ALPHA;
	
	/**
	 * Blend factor test -> RGB : 1-DSTA, 1-DSTA, 1-DSTA  Alpha : 1-DSTA 
	 */
	public static final int BLEND_FACTOR_ONE_MINUS_DST_ALPA = GLES20.GL_ONE_MINUS_DST_ALPHA;
	
	/**
	 * Blend factor test -> RGB : CSTR, CSTG, CSTB  Alpha : CSTA 
	 */
	public static final int BLEND_FACTOR_CONSTANT_COLOR = GLES20.GL_CONSTANT_COLOR;
	
	/**
	 * Blend factor test -> RGB : 1-CSTR, 1-CSTG, 1-CSTB  Alpha : 1-CSTA 
	 */
	public static final int BLEND_FACTOR_ONE_MINUS_CONSTANT_COLOR = GLES20.GL_ONE_MINUS_CONSTANT_COLOR;
	
	/**
	 * Blend factor test -> RGB : CSTA, CSTA, CSTA  Alpha : CSTA 
	 */
	public static final int BLEND_FACTOR_CONSTANT_ALPA = GLES20.GL_CONSTANT_ALPHA;
	
	/**
	 * Blend factor test -> RGB : 1-CSTA, 1-CSTA, 1-CSTA  Alpha : 1-CSTA 
	 */
	public static final int BLEND_FACTOR_ONE_MINUS_CONSTANT_ALPA = GLES20.GL_ONE_MINUS_CONSTANT_ALPHA;
	
	/**
	 * Blend factor test -> RGB : min(SRCA, 1-DSTA)  Alpha : 1 
	 */
	public static final int BLEND_FACTOR_SRC_ALPHA_SATURATE = GLES20.GL_SRC_ALPHA_SATURATE;
	
	/**
	 * Blend operation between source and destination -> Add values
	 */
	public static final int BLEND_OPERATION_ADD = GLES20.GL_FUNC_ADD;
	
	/**
	 * Blend operation between source and destination -> Substract values
	 */
	public static final int BLEND_OPERATION_SUBSTRACT = GLES20.GL_FUNC_SUBTRACT;
	
	/**
	 * Blend operation between source and destination -> Reverse substract value
	 */
	public static final int BLEND_OPERATION_REVERSE_SUBTRACT = GLES20.GL_FUNC_REVERSE_SUBTRACT;
	
	/**
	 * Culling, specify the front face in clockwise mode (default)
	 */
	public static final int CULLING_FRONT_FACE_CW = GLES20.GL_CW;
	
	/**
	 * Culling, specify the front face in counter clockwise mode
	 */
	public static final int CULLING_FRONT_FACE_CCW = GLES20.GL_CCW;
	
	/**
	 * Culling, specify the face to be culled for FRONT face
	 */
	public static final int CULLING_CULL_FACE_FRONT = GLES20.GL_FRONT;
	
	/**
	 * Culling, specify the face to be culled for BACK face (default)
	 */
	public static final int CULLING_CULL_FACE_BACK = GLES20.GL_BACK;
	
	/**
	 * Culling, specify the face to be culled for FRONT and BACK faces
	 */
	public static final int CULLING_CULL_FACE_FRONT_AND_BACK = GLES20.GL_FRONT_AND_BACK;
	
	/**
	 * Enable or disable several preset OpenGLES tests
	 * 
	 * @param testFlag Should be of TEST_DEPTH, TEST_STENCIL, TEST_BLEND, TEST_DITHER, TEST_SCISSOR, TEST_SAMPLE_COVERAGE, TEST_SAMPLE_ALPHA_TO_COVERAGE, TEST_CULLING
	 * @param enabled true to enable test, false otherwise
	 */
	public static final void setTestState(final int testFlag, final boolean enabled){
		//android.util.//Log.d(TAG,"NEW");
		if(enabled){
			GLES20.glEnable(testFlag);
		}
		else{
			GLES20.glDisable(testFlag);
		}
	}
	
	/**
	 * Using TEST_SCISSOR, define the box in which framebuffer will be updated (update and clear)
	 * 
	 * @param x The x coordinate in viewport
	 * @param y The y coordinate in viewport
	 * @param width The width of the box
	 * @param height The height of the box
	 */
	public static final void configureScissorBox(final int x, final int y, final int width, final int height){
		//android.util.//Log.d(TAG,"configureScissorBox("+x+","+y+","+width+","+height+")");
		GLES20.glScissor(x, y, width, height);
	}
	
	/**
	 * Configure the stencil test (fine version using faces is not implemented here)
	 * 
	 * @param function The comparison function should be of FUNCTION_*
	 * @param reference The reference value for the test
	 * @param mask The stencil mask
	 * @param sFail Result value of STENCIL_OUT_* if the stencil test failed
	 * @param zFail Result value of STENCIL_OUT_* if the stencil pass and depth test failed
	 * @param zPass Result value of STENCIL_OUT_* if both stencil and depth test passed
	 */
	public static final void configureStencilTest(final int function, final int reference, final int mask, final int sFail, final int zFail, final int zPass){
		//android.util.//Log.d(TAG,"configureStencilTest("+function+","+reference+","+mask+","+sFail+","+zFail+","+zPass+")");
		GLES20.glStencilFunc(function, reference, mask);
		GLES20.glStencilOp(sFail, zFail, zPass);
	}
	
	/**
	 * Configure the depth test function for very specific features, default is LESS
	 * 
	 * @param function A function identifier from FUNCTION_*
	 */
	public static final void configureDepthTest(final int function){
		//android.util.//Log.d(TAG,"configureDepthTest("+function+")");
		GLES20.glDepthFunc(function);
	}
	
	/**
	 * Configure the blend test function :
	 * <ul>
	 * 	<li>finalColor = srcColor * srcFactor [blendOperation] dstColor * dstFactor <li>
	 * </ul>
	 * 
	 * @param srcFactor Source pixel blend factor should of BLEND_FACTOR_*
	 * @param dstFactor Destination pixel blend factor should of BLEND_FACTOR_*
	 * @param blendOperation The final operation on results should be of BLEND_OPERATION_*
	 * @param constantColor An optional constant color (float[4]) depending on srcFactor and dstFactor
	 */
	public static final void configureBlendTest(final int srcFactor, final int dstFactor, final int blendOperation, final float[] constantColor){
		//android.util.//Log.d(TAG,"configureBlendTest("+srcFactor+","+dstFactor+","+blendOperation+","+constantColor+")");
		if(constantColor != null) {
			GLES20.glBlendColor(constantColor[0], constantColor[1], constantColor[2], constantColor[3]);
		}
		GLES20.glBlendFunc(srcFactor, dstFactor);
		GLES20.glBlendEquation(blendOperation);
	}
	
	/**
	 * Configures the culling
	 * 
	 * @param frontFace Indicates the front face, should be of CULLING_FRONT_FACE_*
	 * @param cullFace Indicates the culled face, should be of CULLING_CULL_FACE_*
	 */
	public static final void configureCullingTest(final int frontFace, final int cullFace){
		//android.util.//Log.d(TAG,"configureCullingTest("+frontFace+","+cullFace+")");
		GLES20.glFrontFace(frontFace);
		GLES20.glCullFace(cullFace);
	}
	
	/**
	 * Set the default color used when clearing the color buffer
	 * 
	 * @param red red value of clear color
	 * @param green green value of clear color
	 * @param blue blue value of clear color
	 * @param alpha alpha value of clear color
	 */
	public static final void setColorBufferClearValue(final float red, final float green, final float blue, final float alpha){
		//android.util.//Log.d(TAG,"setColorBufferClearValue("+red+","+green+","+blue+","+alpha+")");
		GLES20.glClearColor(red, green, blue, alpha);
	}
	
	/**
	 * Set the default depth when clearing the depth buffer
	 * 
	 * @param depth The default depth when clearing
	 */
	public static final void setDepthBufferClearValue(final float depth){
		//android.util.//Log.d(TAG,"setDepthBufferClearValue("+depth+")");
		GLES20.glClearDepthf(depth);
	}
	
	/**
	 * Set the default stencil mask when clearing the stencil buffer
	 * 
	 * @param stencilMask The stencil mask flags 
	 */
	public static final void setStencilBufferClearValue(final int stencilMask){
		//android.util.//Log.d(TAG,"setStencilBufferClearValue("+stencilMask+")");
		GLES20.glClearStencil(stencilMask);
	}
	
	/**
	 * Clear buffers specified in buffersMask 
	 * 
	 * @param buffersMask Should be of type BUFFER_COLOR, BUFFER_DEPTH, BUFFER_STENCIL
	 */
	public static final void clearBuffers(final int buffersMask){
		//android.util.//Log.d(TAG,"clearBuffers("+buffersMask+")");
		GLES20.glClear(buffersMask);
	}

	/**
	 * Set the color mask used when updating framebuffer
	 * 
	 * @param red Indicates if red color is writable
	 * @param green Indicates if green color is writable
	 * @param blue Indicates if blue color is writable
	 * @param alpha Indicates if alpha is writable
	 */
	public static final void setColorBufferMask(final boolean red, final boolean green, final boolean blue, final boolean alpha){
		//android.util.//Log.d(TAG,"setColorBufferMask("+red+","+green+","+blue+","+alpha+")");
		GLES20.glColorMask(red, green, blue, alpha);
	}
	
	/**
	 * Mainly used to disable depth buffer changes for translucent objects
	 * 
	 * @param depth Indicates if the depth buffer is updated at rendering
	 */
	public static final void setDepthBufferMask(final boolean depth){
		//android.util.//Log.d(TAG,"setDepthBufferMask("+depth+")");
		GLES20.glDepthMask(depth);
	}
	
	/**
	 * Set the stencil mask used when updating stencil buffer
	 * 
	 * @param stencilMask The stencil mask flags
	 */
	public static final void setStencilBufferMask(final int stencilMask){
		//android.util.//Log.d(TAG,"setStencilBufferMask("+stencilMask+")");
		GLES20.glStencilMask(stencilMask);
	}

	/**
	 * Check and log GL errors
	 *
	 * @param op The opefation to check for message
	 */
	public static int checkGlError(String tag, String op) {
		int error = GLES20.GL_NO_ERROR;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(tag, op + ": glError " + GLUtils.getEGLErrorString(error));
		}
		return error;
	}

    /**
     * Unified GL state accessor
     *
     * @param key The key of the state to get
     * @return The state value(s) in a float array
     */
    public static float[] glGetState(final int key){
        //android.util.//Log.d(TAG,"getGlState("+key+")");
        final float[] result;
        switch(key){
            //2
            case GLES20.GL_ALIASED_LINE_WIDTH_RANGE :
            case GLES20.GL_ALIASED_POINT_SIZE_RANGE :
            case GLES20.GL_DEPTH_RANGE :
            case GLES20.GL_MAX_VIEWPORT_DIMS :
                result = new float[2];
                break;
            //4
            case GLES20.GL_BLEND_COLOR :
            case GLES20.GL_COLOR_CLEAR_VALUE :
            case GLES20.GL_COLOR_WRITEMASK :
            case GLES20.GL_SCISSOR_BOX :
            case GLES20.GL_VIEWPORT :
                result = new float[4];
                break;
            //Dynamic
            case GLES20.GL_COMPRESSED_TEXTURE_FORMATS :
                final int[]tmp1 = new int[1];
                GLES20.glGetIntegerv(GLES20.GL_NUM_COMPRESSED_TEXTURE_FORMATS, tmp1, 0);
                result = new float[tmp1[0]];
                break;
            case GLES20.GL_SHADER_BINARY_FORMATS :
                final int[]tmp2 = new int[1];
                GLES20.glGetIntegerv(GLES20.GL_NUM_SHADER_BINARY_FORMATS, tmp2, 0);
                result = new float[tmp2[0]];
                break;
            //1
            default :
                result = new float[1];
        }
        GLES20.glGetFloatv(key, result, 0);
        return result;
    }

    public static void setViewport(final int left, final int bottom, final int width, final int height){
    	GLES20.glViewport(left, bottom, width, height);
	}
}

