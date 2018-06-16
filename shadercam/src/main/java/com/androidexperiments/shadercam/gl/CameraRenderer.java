package com.androidexperiments.shadercam.gl;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.media.MediaRecorder;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.Toast;

import com.androidexperiments.shadercam.fragments.CameraFragment;
import com.androidexperiments.shadercam.utils.ShaderUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/** *
 * Base camera rendering class. Responsible for rendering to proper window contexts, as well as
 * recording video with built-in media recorder.
 *
 * Subclass this and add any kind of fun stuff u want, new shaders, textures, uniforms - go to town!
 *
 */

public class CameraRenderer extends Thread implements SurfaceTexture.OnFrameAvailableListener
{
    private static final String TAG = "A_GO/CameraRenderer";
    private static final String THREAD_NAME = "CameraRendererThread";

    /**
     * if you create new files, just override these defaults in your subclass and
     * don't edit the {@link #vertexShaderCode} and {@link #fragmentShaderCode} variables
     */
    protected String DEFAULT_FRAGMENT_SHADER = "camera.frag.glsl";

    protected String DEFAULT_VERTEX_SHADER = "camera.vert.glsl";

    /**
     * Current context for use with utility methods
     */
    protected Context mContext;

    protected int mSurfaceWidth, mSurfaceHeight;

    protected float mSurfaceAspectRatio;

    /**
     * main texture for display, based on TextureView that is created in activity or fragment
     * and passed in after onSurfaceTextureAvailable is called, guaranteeing its existence.
     */
    protected Surface mSurface;

    /**
     * EGLCore used for creating {@link WindowSurface}s for preview and recording
     */
    protected EglCore mEglCore;

    /**
     * Primary {@link WindowSurface} for rendering to screen
     */
    protected WindowSurface mWindowSurface;

    /**
     * Texture created for GLES rendering of camera data
     */
    protected SurfaceTexture mPreviewTexture;

    /**
     * if you override these in ctor of subclass, loader will ignore the files listed above
     */
    protected String vertexShaderCode;

    protected String fragmentShaderCode;

    /**
     * Basic mesh rendering code
     */
    protected static float squareSize = 1.0f;

    protected static float squareCoords[] = {
            -squareSize, squareSize, // 0.0f,     // top left
            squareSize, squareSize, // 0.0f,   // top right
            -squareSize, -squareSize, // 0.0f,   // bottom left
            squareSize, -squareSize, // 0.0f,   // bottom right
    };

    protected static short drawOrder[] = {0, 1, 2, 1, 3, 2};

    private FloatBuffer textureBuffer;

    protected float textureCoords[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    protected int mCameraShaderProgram;

    protected FloatBuffer vertexBuffer;

    protected ShortBuffer drawListBuffer;

    protected int textureCoordinateHandle;

    protected int positionHandle;

    protected long lastTime = 0;
    protected long lastLog = 0;
    protected int fps = 0;

    /**
     * Cam texture ID
     */
    private int mCamTextureId;

    /**
     * matrix for transforming our camera texture, available immediately after {@link #mPreviewTexture}s
     * {@code updateTexImage()} is called in our main {@link #draw()} loop.
     */
    protected float[] mCameraTransformMatrix = new float[16];

    /**
     * Handler for communcation with the UI thread. Implementation below at
     * {@link com.androidexperiments.shadercam.gl.CameraRenderer.RenderHandler RenderHandler}
     */
    private RenderHandler mHandler;

    /**
     * Interface listener for some callbacks to the UI thread when rendering is setup and finished.
     */
    private OnRendererReadyListener mOnRendererReadyListener;

    /**
     * Reference to our users CameraFragment to ease setting viewport size. Thought about decoupling but wasn't
     * worth the listener/callback hastle
     */
    private CameraFragment mCameraFragment;

    private String mFragmentShaderPath;
    private String mVertexShaderPath;

    /**
     * Simple ctor to use default shaders
     */
    public CameraRenderer(Context context, Surface surface, int width, int height)
    {
        Log.d(TAG, "CameraRenderer - "+width+", "+height);
        init(context, surface, width, height, DEFAULT_FRAGMENT_SHADER, DEFAULT_VERTEX_SHADER);
    }

    /**
     * Main constructor for passing in shaders to override the default shader.
     * Context, surface, width, and height are passed in automatically by CameraTextureListener
     * @param fragPath the file name of your fragment shader, ex: "lip_service.frag" if it is top-level /assets/ folder. Add subdirectories if needed
     * @param vertPath the file name of your vertex shader, ex: "lip_service.vert" if it is top-level /assets/ folder. Add subdirectories if needed
     */
    public CameraRenderer(Context context, Surface surface, int width, int height, String fragPath, String vertPath)
    {
        Log.d(TAG, "CameraRenderer - "+width+", "+height+", "+fragPath+", "+vertPath);
        init(context, surface, width, height, fragPath, vertPath);
    }

    private void init(Context context, Surface surface, int width, int height, String fragPath, String vertPath)
    {
        Log.d(TAG, "init - "+width+", "+height+", "+fragPath+", "+vertPath);
        this.setName(THREAD_NAME);

        this.mContext = context;
        this.mSurface = surface;

        this.mSurfaceWidth = width;
        this.mSurfaceHeight = height;
        this.mSurfaceAspectRatio = (float)width / height;

        this.mFragmentShaderPath = fragPath;
        this.mVertexShaderPath = vertPath;

    }

    protected void initialize() {
        Log.d(TAG, "initialize");
        setupCameraFragment();

        if(fragmentShaderCode == null || vertexShaderCode == null) {
            loadFromShadersFromAssets(mFragmentShaderPath, mVertexShaderPath);
        }
    }

    protected void setupCameraFragment() {
        Log.d(TAG, "setupCameraFragment");
        if(mCameraFragment == null) {
            throw new RuntimeException("CameraFragment is null! Please call setCameraFragment prior to initialization.");
        }
        }

    protected void loadFromShadersFromAssets(String pathToFragment, String pathToVertex)
    {
        Log.d(TAG, "loadFromShadersFromAssets - "+pathToFragment+", "+pathToVertex);
        try {
            fragmentShaderCode = ShaderUtils.getStringFromFileInAssets(mContext, pathToFragment);
            vertexShaderCode = ShaderUtils.getStringFromFileInAssets(mContext, pathToVertex);
        }
        catch (IOException e) {
            Log.e(TAG, "loadFromShadersFromAssets() failed. Check paths to assets.\n" + e.getMessage());
        }
    }


    /**
     * Initialize all necessary components for GLES rendering, creating window surfaces for drawing
     * the preview as well as the surface that will be used by MediaRecorder for recording
     */
    public void initGL() {
        Log.d(TAG, "initGL");
        mEglCore = new EglCore(null, /*EglCore.FLAG_RECORDABLE |*/ EglCore.FLAG_TRY_GLES3);

        //create preview surface
        mWindowSurface = new WindowSurface(mEglCore, mSurface, true);
        mWindowSurface.makeCurrent();

        initGLComponents();
    }

    protected void initGLComponents() {
        Log.d(TAG, "initGLComponents");
        onPreSetupGLComponents();

        setupVertexBuffer();
        setupCameraTexture();
        setupShaders();

        onSetupComplete();
    }


    // ------------------------------------------------------------
    // deinit
    // ------------------------------------------------------------

    public void deinitGL() {
        Log.d(TAG, "deinitGL");
        deinitGLComponents();

        mWindowSurface.release();

        mEglCore.release();

    }

    protected void deinitGLComponents() {
        Log.d(TAG, "deinitGLComponents");
        GLES20.glDeleteTextures(1, new int[]{mCamTextureId}, 0);
        GLES20.glDeleteProgram(mCameraShaderProgram);

        mPreviewTexture.release();
        mPreviewTexture.setOnFrameAvailableListener(null);
    }

    // ------------------------------------------------------------
    // setup
    // ------------------------------------------------------------

    /**
     * override this method if there's anything else u want to accomplish before
     * the main camera setup gets underway
     */
    protected void onPreSetupGLComponents() {
        Log.d(TAG, "onPreSetupGLComponents");
        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    protected void setupVertexBuffer() {
        Log.d(TAG, "setupVertexBuffer");
        // Draw list buffer
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // Initialize the texture holder
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);
    }

    /**
     * Remember that Android's camera api returns camera texture not as {@link GLES20#GL_TEXTURE_2D}
     * but rather as {@link GLES11Ext#GL_TEXTURE_EXTERNAL_OES}, which we bind here
     */
    protected void setupCameraTexture() {
        Log.d(TAG, "setupCameraTexture");

        ByteBuffer texturebb = ByteBuffer.allocateDirect(textureCoords.length * 4);
        texturebb.order(ByteOrder.nativeOrder());

        textureBuffer = texturebb.asFloatBuffer();
        textureBuffer.put(textureCoords);
        textureBuffer.position(0);

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

    /**
     * Handling this manually here but check out another impl at {@link GlUtil#createProgram(String, String)}
     */
    protected void setupShaders() {
        Log.d(TAG, "setupShaders");
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShaderHandle, vertexShaderCode);
        GLES20.glCompileShader(vertexShaderHandle);
        checkGlError("Vertex shader compile");

        Log.d(TAG, "vertexShader info log:\n " + GLES20.glGetShaderInfoLog(vertexShaderHandle));

        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
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
    }

    /**
     * called when all setup is complete on basic GL stuffs
     * override for adding textures and other shaders and make sure to call
     * super so that we can let them know we're done
     */
    protected void onSetupComplete() {
        Log.d(TAG, "onSetupComplete");
        mOnRendererReadyListener.onRendererReady();
    }

    @Override
    public synchronized void start() {
        Log.d(TAG, "start");
        initialize();

        if(mOnRendererReadyListener == null)
            throw new RuntimeException("OnRenderReadyListener is not set! Set listener prior to calling start()");

        super.start();
    }


    /**
     * primary loop - this does all the good things
     */
    @Override
    public void run()
    {
        Log.d(TAG, "run");

        Looper.prepare();

        //create handler for communication from UI
        mHandler = new RenderHandler(this);

        //initialize all GL on this context
        initGL();

        lastTime = System.currentTimeMillis();
        lastLog = lastTime;

        //LOOOOOOOOOOOOOOOOP
        Looper.loop();

        //we're done here
        deinitGL();

        mOnRendererReadyListener.onRendererFinished();
    }

    /**
     * stop our thread, and make sure we kill a recording if its still happening
     *
     * this should only be called from our handler to ensure thread-safe
     */
    public void shutdown() {
        Log.d(TAG, "shutdown");
        //kill ouy thread
        Looper.myLooper().quit();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture)
    {
        boolean swapResult;

        synchronized (this)
        {
            mPreviewTexture.updateTexImage();
            mPreviewTexture.getTransformMatrix(mCameraTransformMatrix);

            draw();
            mWindowSurface.makeCurrent();
            swapResult = mWindowSurface.swapBuffers();

            if (!swapResult) {
                // This can happen if the Activity stops without waiting for us to halt.
                Log.e(TAG, "swapBuffers failed, killing renderer thread");
                shutdown();
            }
        }
    }

    protected void logFPS(){
        fps++;
        long currentTime = System.currentTimeMillis();
        long duration = currentTime -lastTime;

        if(currentTime - lastLog > 1000){
            Log.d(TAG, "FPS : "+fps);
            fps=0;
            lastLog = currentTime;
        }

        lastTime = currentTime;
    }

    /**
     * main draw routine
     */
    public void draw()
    {
        //logFPS();
        //set shader
        GLES20.glUseProgram(mCameraShaderProgram);

        int textureParamHandle = GLES20.glGetUniformLocation(mCameraShaderProgram, "camTexture");
        int textureTranformHandle = GLES20.glGetUniformLocation(mCameraShaderProgram, "camTextureTransform");
        textureCoordinateHandle = GLES20.glGetAttribLocation(mCameraShaderProgram, "camTexCoordinate");
        positionHandle = GLES20.glGetAttribLocation(mCameraShaderProgram, "position");


        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 4 * 2, vertexBuffer);

        //camera texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mCamTextureId);
        GLES20.glUniform1i(textureParamHandle, 0);

        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 4 * 2, textureBuffer);

        GLES20.glUniformMatrix4fv(textureTranformHandle, 1, false, mCameraTransformMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(textureCoordinateHandle);
    }


    /**
     * utility for checking GL errors
     * @param op
     */
    public void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("SurfaceTest", op + ": glError " + GLUtils.getEGLErrorString(error));
        }
    }

    //getters and setters
    public SurfaceTexture getPreviewTexture() {
        Log.d(TAG, "getPreviewTexture");
        return mPreviewTexture;
    }

    public RenderHandler getRenderHandler() {
        Log.d(TAG, "getRenderHandler");
        return mHandler;
    }

    public void setOnRendererReadyListener(OnRendererReadyListener listener) {
        Log.d(TAG, "setOnRendererReadyListener");
        mOnRendererReadyListener = listener;

    }


    public void setCameraFragment(CameraFragment cameraFragment) {
        Log.d(TAG, "setCameraFragment");
        mCameraFragment = cameraFragment;
    }


    /**
     * {@link Handler} responsible for communication between this render thread and the UI thread.
     *
     * For now, the only thing we really need to worry about is shutting down the thread upon completion
     * of recording, since we cannot access the {@link android.media.MediaRecorder} surface once
     * {@link MediaRecorder#stop()} is called.
     */
    public static class RenderHandler extends Handler
    {
        private static final String TAG = RenderHandler.class.getSimpleName();

        private static final int MSG_SHUTDOWN = 0;

        /**
         * Our camera renderer ref, weak since we're dealing with static class so it doesn't leak
         */
        private WeakReference<CameraRenderer> mWeakRenderer;

        /**
         * Call from render thread.
         */
        public RenderHandler(CameraRenderer rt) {
            Log.d(TAG, "RenderHandler");
            mWeakRenderer = new WeakReference<>(rt);
        }

        /**
         * Sends the "shutdown" message, which tells the render thread to halt.
         * Call from UI thread.
         */
        public void sendShutdown() {
            Log.d(TAG, "sendShutdown");
            sendMessage(obtainMessage(RenderHandler.MSG_SHUTDOWN));
        }

        @Override
        public void handleMessage(Message msg)
        {
            Log.d(TAG, "handleMessage - "+msg);
            CameraRenderer renderer = mWeakRenderer.get();
            if (renderer == null) {
                Log.w(TAG, "RenderHandler.handleMessage: weak ref is null");
                return;
            }

            int what = msg.what;
            switch (what) {
                case MSG_SHUTDOWN:
                    renderer.shutdown();
                    break;
                default:
                    throw new RuntimeException("unknown message " + what);
            }
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
