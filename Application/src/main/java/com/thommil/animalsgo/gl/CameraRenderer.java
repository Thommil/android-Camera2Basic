package com.thommil.animalsgo.gl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;

import com.thommil.animalsgo.data.Messaging;
import com.thommil.animalsgo.fragments.CameraFragment;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;


public class CameraRenderer extends HandlerThread implements SurfaceTexture.OnFrameAvailableListener, CameraFragment.OnViewportSizeUpdatedListener, Handler.Callback {

    private static final String TAG = "A_GO/CameraRenderer";
    private static final String THREAD_NAME = "CameraRendererThread";

    private final String DEFAULT_FRAGMENT_SHADER = "camera.frag.glsl";
    private final String DEFAULT_VERTEX_SHADER = "camera.vert.glsl";

    // Current context for use with utility methods
    protected Context mContext;

    // Underlying surface dimensions
    private int mSurfaceWidth, mSurfaceHeight;

    // Underlying surface ratio
    private float mSurfaceAspectRatio;

    // main texture for display, based on TextureView that is created in activity or fragment
    // and passed in after onSurfaceTextureAvailable is called, guaranteeing its existence.
    private Surface mSurface;

    // EGLCore used for creating WindowSurface for preview
    private EglCore mEglCore;

    // Primary WindowSurface for rendering to screen
    private WindowSurface mWindowSurface;

    // Texture created for GLES rendering of camera data
    private SurfaceTexture mPreviewTexture;

    // shaders identifier
    private int mCameraShaderProgram;

    // Basic mesh rendering code
    private static final float SQUARE_SIZE = 1.0f;
    private static final float SQUARE_COORDS[] = {
            -SQUARE_SIZE, SQUARE_SIZE, // 0.0f,     // top left
            SQUARE_SIZE, SQUARE_SIZE, // 0.0f,   // top right
            -SQUARE_SIZE, -SQUARE_SIZE, // 0.0f,   // bottom left
            SQUARE_SIZE, -SQUARE_SIZE, // 0.0f,   // bottom right
    };
    private static final short DRAW_ORDER[] = {0, 1, 2, 1, 3, 2};

    // Texture coord buffer
    private FloatBuffer mTextureBuffer;

    // Texture coords values (can be modified)
    private float mTextureCoords[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    // Shader handle for texture coordinates
    private int textureCoordinateHandle;

    // Shader handle for texture ID
    private int mTextureParamHandle;

    // Shader handle for texture tranform matrix
    private int mTextureTranformHandle;

    // Texture verrices buffer
    private FloatBuffer mVertexBuffer;

    // Texture vertices order buffer
    private ShortBuffer mDrawListBuffer;

    // GSLG handle for texture vertices
    private int mPositionHandle;

    // Cam texture ID
    private int mCamTextureId;

    // matrix for transforming our camera texture, available immediately after PreviewTexture.updateTexImage()
    private final float[] mCameraTransformMatrix = new float[16];

    // Handler for communcation with the UI thread. Implementation below at
    private Handler mHandler;

    // Main handler
    private Handler mMainHandler;

    /**
     * Interface listener for some callbacks to the UI thread when rendering is setup and finished.
     */
    private OnRendererReadyListener mOnRendererReadyListener;

    /**
     * Reference to our users CameraFragment to ease setting viewport size. Thought about decoupling but wasn't
     * worth the listener/callback hastle
     */
    protected CameraFragment mCameraFragment;


    public CameraRenderer(Context context, Surface surface, int width, int height) {
        super(THREAD_NAME);

        this.mContext = context;
        this.mSurface = surface;

        this.mSurfaceWidth = width;
        this.mSurfaceHeight = height;
        this.mSurfaceAspectRatio = (float)width / height;
    }

    public void initGL() {
        Log.d(TAG, "initGL()");
        mEglCore = new EglCore(null, EglCore.FLAG_TRY_GLES3);

        //create preview surface
        mWindowSurface = new WindowSurface(mEglCore, mSurface, true);
        mWindowSurface.makeCurrent();

        initGLComponents();
    }

    protected void initGLComponents() {
        Log.d(TAG, "initGLComponents()");
        onPreSetupGLComponents();

        setupVertexBuffer();
        setupCameraTextureCoords();
        setupCameraTexture();
        setupShaders();

        onSetupComplete();
    }


    public void deinitGL() {
        Log.d(TAG, "deinitGL()");
        deinitGLComponents();

        mWindowSurface.release();

        mEglCore.release();

    }

    protected void deinitGLComponents() {
        Log.d(TAG, "deinitGLComponents()");
        GLES20.glDeleteTextures(1, new int[]{mCamTextureId}, 0);
        GLES20.glDeleteProgram(mCameraShaderProgram);

        mPreviewTexture.release();
        mPreviewTexture.setOnFrameAvailableListener(null);
    }

    @Override
    public void onViewportSizeUpdated(Size surfaceSize, Size previewSize) {
        Log.d(TAG, "onViewportSizeUpdated(" +surfaceSize+", "+previewSize+")");
        final float surfaceRatio = (float)surfaceSize.getWidth()/(float)surfaceSize.getHeight();
        final float previewRatio = (float)previewSize.getHeight()/(float)previewSize.getWidth();

        Log.d(TAG, "Ratios - S : " +surfaceRatio+", P : "+previewRatio);

        //We must crop preview vertically
        if(previewRatio > surfaceRatio){
            final float delta = (previewRatio - surfaceRatio) / 2f;
            mTextureCoords = new float[]{
                    delta, 1.0f,
                    1.0f-delta, 1.0f,
                    delta, 0.0f,
                    1.0f-delta, 0.0f
            };
        }
        //We must crop preview horizontally
        else{
            final float delta = (surfaceRatio - previewRatio ) / 2f;
            mTextureCoords = new float[]{
                    0.0f, 1.0f-delta,
                    1.0f, 1.0f-delta,
                    0.0f, delta,
                    1.0f, delta
            };
        }

        Log.d(TAG, "TextureCoords : " + Arrays.toString(mTextureCoords));

        setupCameraTextureCoords();
    }

    protected void onPreSetupGLComponents() {
        Log.d(TAG, "onPreSetupGLComponents()");
        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    protected void setupVertexBuffer() {
        Log.d(TAG, "setupVertexBuffer()");
        // Draw list buffer
        ByteBuffer dlb = ByteBuffer.allocateDirect(DRAW_ORDER.length * Short.BYTES);
        dlb.order(ByteOrder.nativeOrder());
        mDrawListBuffer = dlb.asShortBuffer();
        mDrawListBuffer .put(DRAW_ORDER);
        mDrawListBuffer .position(0);

        // Initialize the texture holder
        ByteBuffer bb = ByteBuffer.allocateDirect(SQUARE_COORDS.length * Float.BYTES);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(SQUARE_COORDS);
        mVertexBuffer.position(0);
    }

    protected void setupCameraTextureCoords(){
        Log.d(TAG, "setupCameraTextureCoord()");
        ByteBuffer texturebb = ByteBuffer.allocateDirect(mTextureCoords.length * Float.BYTES);
        texturebb.order(ByteOrder.nativeOrder());

        mTextureBuffer = texturebb.asFloatBuffer();
        mTextureBuffer.put(mTextureCoords);
        mTextureBuffer.position(0);
    }

    protected void setupCameraTexture() {
        Log.d(TAG, "setupCameraTexture()");

        int[] texturesId = new int[1];
        GLES20.glGenTextures(1, texturesId , 0);
        checkGlError("Texture generate");
        mCamTextureId = texturesId[0];

        //set texture[0] to camera texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mCamTextureId);
        checkGlError("Texture bind");

        mPreviewTexture = new SurfaceTexture(mCamTextureId);
        mPreviewTexture.setOnFrameAvailableListener(this);
    }

    protected void setupShaders() {
        Log.d(TAG, "setupShaders()");
        try {
            int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
            final String vertexShaderCode = ShaderUtils.getStringFromFileInAssets(mContext, DEFAULT_VERTEX_SHADER);
            GLES20.glShaderSource(vertexShaderHandle, vertexShaderCode);
            GLES20.glCompileShader(vertexShaderHandle);
            checkGlError("Vertex shader compile");

            Log.d(TAG, "vertexShader info log:\n " + GLES20.glGetShaderInfoLog(vertexShaderHandle));

            int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
            final String fragmentShaderCode = ShaderUtils.getStringFromFileInAssets(mContext, DEFAULT_FRAGMENT_SHADER);
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShaderCode);
            GLES20.glCompileShader(fragmentShaderHandle);
            checkGlError("Pixel shader compile");

            Log.d(TAG, "fragmentShader info log:\n " + GLES20.glGetShaderInfoLog(fragmentShaderHandle));

            mCameraShaderProgram = GLES20.glCreateProgram();
            GLES20.glAttachShader(mCameraShaderProgram, vertexShaderHandle);
            GLES20.glAttachShader(mCameraShaderProgram, fragmentShaderHandle);
            GLES20.glLinkProgram(mCameraShaderProgram);
            checkGlError("Shader program compile");

            int[] status = new int[1];
            GLES20.glGetProgramiv(mCameraShaderProgram, GLES20.GL_LINK_STATUS, status, 0);
            if (status[0] != GLES20.GL_TRUE) {
                String error = GLES20.glGetProgramInfoLog(mCameraShaderProgram);
                Log.e("SurfaceTest", "Error while linking program:\n" + error);
            }

            GLES20.glUseProgram(mCameraShaderProgram);
            mTextureParamHandle = GLES20.glGetUniformLocation(mCameraShaderProgram, "camTexture");
            mTextureTranformHandle = GLES20.glGetUniformLocation(mCameraShaderProgram, "camTextureTransform");
            textureCoordinateHandle = GLES20.glGetAttribLocation(mCameraShaderProgram, "camTexCoordinate");
            mPositionHandle = GLES20.glGetAttribLocation(mCameraShaderProgram, "position");
        }catch(IOException ioe){
            throw new RuntimeException("Failed to find shaders source.");
        }
    }

    protected void onSetupComplete() {
        Log.d(TAG, "onSetupComplete()");
        mOnRendererReadyListener.onRendererReady();
    }

    @Override
    public synchronized void start() {
        Log.d(TAG, "start()");
        if(mCameraFragment == null) {
            throw new RuntimeException("CameraFragment is null! Please call setCameraFragment prior to initialization.");
        }

        if(mOnRendererReadyListener == null) {
            throw new RuntimeException("OnRenderReadyListener is not set! Set listener prior to calling start()");
        }

        if(mMainHandler == null){
            throw new RuntimeException("Main UI handler reference must be set before start()");
        }

        super.start();
    }


    @Override
    public void run() {
        Log.d(TAG, "run()");

        Looper.prepare();

        //create handler for communication from UI
        mHandler = new Handler(Looper.myLooper(), this);
        mMainHandler.sendMessage(mMainHandler.obtainMessage(Messaging.SYSTEM_CONNECT_RENDERER, mHandler));

        //Associated GL Thread to capture completion
        mCameraFragment.setRendererHandler(mHandler);

        //initialize all GL on this context
        initGL();

        //Loop
        Looper.loop();

        //we're done here
        deinitGL();

        mOnRendererReadyListener.onRendererFinished();
    }


    @Override
    public synchronized void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mPreviewTexture.updateTexImage();
        mPreviewTexture.getTransformMatrix(mCameraTransformMatrix);

        draw();
        mWindowSurface.makeCurrent();

        if (!mWindowSurface.swapBuffers()) {
            shutdown();
        }
    }

    public void draw() {
        //set shader
        GLES20.glUseProgram(mCameraShaderProgram);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 8, mVertexBuffer);

        //camera texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mCamTextureId);
        GLES20.glUniform1i(mTextureParamHandle, 0);

        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 8, mTextureBuffer);

        GLES20.glUniformMatrix4fv(mTextureTranformHandle, 1, false, mCameraTransformMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer );

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(textureCoordinateHandle);
    }

    @Override
    public boolean handleMessage(Message message) {
        Log.d(TAG, "handleMessage(" + message);

        switch(message.what){
            case Messaging.SYSTEM_SHUTDOWN:
                shutdown();
                break;
        }
        return true;
    }


    protected void shutdown() {
        Log.d(TAG, "shutdown");
        mHandler.getLooper().quit();
    }

    public void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("SurfaceTest", op + ": glError " + GLUtils.getEGLErrorString(error));
        }
    }

    public SurfaceTexture getPreviewTexture() {
        Log.d(TAG, "getPreviewTexture()");
        return mPreviewTexture;
    }

    public void setOnRendererReadyListener(OnRendererReadyListener listener) {
        Log.d(TAG, "setOnRendererReadyListener()");
        mOnRendererReadyListener = listener;

    }


    public void setCameraFragment(CameraFragment cameraFragment) {
        Log.d(TAG, "setCameraFragment()");
        mCameraFragment = cameraFragment;
    }

    public void setMainHandler(final Handler mainHandler) {
        this.mMainHandler = mainHandler;
    }

    private void showError(final String message){
        if(mMainHandler != null){
            mMainHandler.sendMessage(mMainHandler.obtainMessage(Messaging.SYSTEM_ERROR, message));
        }
    }

    /**
     * Interface for callbacks when render thread completes its setup
     */
    public interface OnRendererReadyListener {
        /**
         * Called when {@link #onSetupComplete()} is finished with its routine
         */
        void onRendererReady();

        /**
         * Called once the looper is killed and our {@link #run()} method completes
         */
        void onRendererFinished();
    }
}
