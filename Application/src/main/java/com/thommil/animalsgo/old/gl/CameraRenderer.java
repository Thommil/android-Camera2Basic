package com.thommil.animalsgo.old.gl;

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

import com.thommil.animalsgo.old.fragments.CameraFragment;
import com.thommil.animalsgo.opencv.SnapshotValidator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/** *
 * Base camera rendering class. Responsible for rendering to proper window contexts, as well as
 * recording video with built-in media recorder.
 *
 * Subclass this and add any kind of fun stuff u want, new shaders, textures, uniforms - go to town!
 *
 * TODO Android < 6 -> Fix Ratio (using gravity ?)
 *
 */

public class CameraRenderer extends HandlerThread implements SurfaceTexture.OnFrameAvailableListener, CameraFragment.OnViewportSizeUpdatedListener, Handler.Callback, CameraFragment.OnCaptureCompletedListener, View.OnTouchListener {

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

    protected FloatBuffer textureBuffer;

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
     */
    protected Handler mHandler;

    /**
     * Interface listener for some callbacks to the UI thread when rendering is setup and finished.
     */
    private OnRendererReadyListener mOnRendererReadyListener;

    /**
     * Reference to our users CameraFragment to ease setting viewport size. Thought about decoupling but wasn't
     * worth the listener/callback hastle
     */
    protected CameraFragment mCameraFragment;

    private String mFragmentShaderPath;
    private String mVertexShaderPath;

    private static final int SNAPSHOT_SCORE_THRESHOLD = 70;

    private final Handler mainHandler;

    private final SnapshotValidator snapshotValidator;

    //private final SnapshotValidator.Snapshot snapshotInstance;

    private final CameraFragment.CaptureData mCurrentCaptureData;

    public final static int STATE_PREVIEW = 0x00;
    public final static int STATE_START_ANALYZE = 0x01;
    public final static int STATE_ANALYZING = 0X02;
    public final static int STATE_CONFIRM_SNAPSHOT = 0X04;

    public final static int STATE_SHUTDOWN = 0X08;

    private int mState;

    public CameraRenderer(Context context, Surface surface, int width, int height) {
        super(THREAD_NAME);
        init(context, surface, width, height, DEFAULT_FRAGMENT_SHADER, DEFAULT_VERTEX_SHADER);
        //Log.d(TAG, "AGCameraRenderer");
        mState = STATE_PREVIEW;
        mainHandler = new Handler(Looper.getMainLooper());
        snapshotValidator = new SnapshotValidator();
        //snapshotInstance = new SnapshotValidator.Snapshot();
        mCurrentCaptureData = new CameraFragment.CaptureData();
        snapshotValidator.start();
    }

    private void init(Context context, Surface surface, int width, int height, String fragPath, String vertPath)
    {
        //Log.d(TAG, "init - "+width+", "+height+", "+fragPath+", "+vertPath);
        this.mContext = context;
        this.mSurface = surface;

        this.mSurfaceWidth = width;
        this.mSurfaceHeight = height;
        this.mSurfaceAspectRatio = (float)width / height;

        this.mFragmentShaderPath = fragPath;
        this.mVertexShaderPath = vertPath;

    }

    protected void initialize() {
        //Log.d(TAG, "initialize");
        setupCameraFragment();

        if(fragmentShaderCode == null || vertexShaderCode == null) {
            loadFromShadersFromAssets(mFragmentShaderPath, mVertexShaderPath);
        }
    }

    protected void setupCameraFragment() {
        //Log.d(TAG, "setupCameraFragment");
        if(mCameraFragment == null) {
            throw new RuntimeException("CameraFragment is null! Please call setCameraFragment prior to initialization.");
        }
        }

    protected void loadFromShadersFromAssets(String pathToFragment, String pathToVertex)
    {
        //Log.d(TAG, "loadFromShadersFromAssets - "+pathToFragment+", "+pathToVertex);
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
        //Log.d(TAG, "initGL");
        mEglCore = new EglCore(null, /*EglCore.FLAG_RECORDABLE |*/ EglCore.FLAG_TRY_GLES3);

        //create preview surface
        mWindowSurface = new WindowSurface(mEglCore, mSurface, true);
        mWindowSurface.makeCurrent();

        initGLComponents();
    }

    protected void initGLComponents() {
        //Log.d(TAG, "initGLComponents");
        onPreSetupGLComponents();

        setupVertexBuffer();
        setupCameraTextureCoords();
        setupCameraTexture();
        setupShaders();

        onSetupComplete();
    }


    // ------------------------------------------------------------
    // deinit
    // ------------------------------------------------------------

    public void deinitGL() {
        //Log.d(TAG, "deinitGL");
        deinitGLComponents();

        mWindowSurface.release();

        mEglCore.release();

    }

    protected void deinitGLComponents() {
        //Log.d(TAG, "deinitGLComponents");
        GLES20.glDeleteTextures(1, new int[]{mCamTextureId}, 0);
        GLES20.glDeleteProgram(mCameraShaderProgram);

        mPreviewTexture.release();
        mPreviewTexture.setOnFrameAvailableListener(null);
    }

    // ------------------------------------------------------------
    // setup
    // ------------------------------------------------------------


    @Override
    public void onViewportSizeUpdated(Size surfaceSize, Size previewSize) {
        //Log.d(TAG, "onViewportSizeUpdated - " +surfaceSize+","+previewSize);
        float surfaceRatio = (float)surfaceSize.getWidth()/(float)surfaceSize.getHeight();
        float previewRatio = (float)previewSize.getHeight()/(float)previewSize.getWidth();

        //Log.d(TAG, "Ratios - S : " +surfaceRatio+", P : "+previewRatio);

        //We must crop preview vertically
        if(previewRatio > surfaceRatio){
            float delta = (previewRatio - surfaceRatio) / 2f;
            textureCoords = new float[]{
                    delta, 1.0f,
                    1.0f-delta, 1.0f,
                    delta, 0.0f,
                    1.0f-delta, 0.0f
            };
        }
        //We must crop preview horizontally
        else{
            float delta = (surfaceRatio - previewRatio ) / 2f;
            textureCoords = new float[]{
                    0.0f, 1.0f-delta,
                    1.0f, 1.0f-delta,
                    0.0f, delta,
                    1.0f, delta
            };
        }

        //Log.d(TAG, "TextureCoords : " + Arrays.toString(textureCoords));

        setupCameraTextureCoords();

        //snapshotInstance.width = surfaceSize.getWidth();
        //snapshotInstance.height = surfaceSize.getHeight();
        //snapshotInstance.data = ByteBuffer.allocateDirect(snapshotInstance.width * snapshotInstance.height * 4);
    }

    /**
     * override this method if there's anything else u want to accomplish before
     * the main camera setup gets underway
     */
    protected void onPreSetupGLComponents() {
        //Log.d(TAG, "onPreSetupGLComponents");
        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    protected void setupVertexBuffer() {
        //Log.d(TAG, "setupVertexBuffer");
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

    protected void setupCameraTextureCoords(){
        //Log.d(TAG, "setupCameraTextureCoord");
        ByteBuffer texturebb = ByteBuffer.allocateDirect(textureCoords.length * 4);
        texturebb.order(ByteOrder.nativeOrder());

        textureBuffer = texturebb.asFloatBuffer();
        textureBuffer.put(textureCoords);
        textureBuffer.position(0);
    }

    /**
     * Remember that Android's camera api returns camera texture not as {@link GLES20#GL_TEXTURE_2D}
     * but rather as {@link GLES11Ext#GL_TEXTURE_EXTERNAL_OES}, which we bind here
     */
    protected void setupCameraTexture() {
        //Log.d(TAG, "setupCameraTexture");

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
        //Log.d(TAG, "setupShaders");
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShaderHandle, vertexShaderCode);
        GLES20.glCompileShader(vertexShaderHandle);
        checkGlError("Vertex shader compile");

        //Log.d(TAG, "vertexShader info log:\n " + GLES20.glGetShaderInfoLog(vertexShaderHandle));

        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShaderHandle, fragmentShaderCode);
        GLES20.glCompileShader(fragmentShaderHandle);
        checkGlError("Pixel shader compile");

        //Log.d(TAG, "fragmentShader info log:\n " + GLES20.glGetShaderInfoLog(fragmentShaderHandle));

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
        //Log.d(TAG, "onSetupComplete");
        mOnRendererReadyListener.onRendererReady();
    }

    @Override
    public synchronized void start() {
        //Log.d(TAG, "start");
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
        //Log.d(TAG, "run");

        Looper.prepare();

        //create handler for communication from UI
        mHandler = new Handler(Looper.myLooper(), this);

        //Associated GL Thread to capture completion
        mCameraFragment.setBackgroundHandler(mHandler);

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

        if(currentTime - lastLog > 1000){
            Log.i(TAG, "FPS : "+fps);
            fps=0;
            lastLog = currentTime;
        }

        lastTime = currentTime;
    }

    /**
     * main draw routine
     */
    public void drawPreview()
    {
        //set shader
        GLES20.glUseProgram(mCameraShaderProgram);
        int textureParamHandle = GLES20.glGetUniformLocation(mCameraShaderProgram, "camTexture");
        int textureTranformHandle = GLES20.glGetUniformLocation(mCameraShaderProgram, "camTextureTransform");
        textureCoordinateHandle = GLES20.glGetAttribLocation(mCameraShaderProgram, "camTexCoordinate");
        positionHandle = GLES20.glGetAttribLocation(mCameraShaderProgram, "position");


        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer);

        //camera texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mCamTextureId);
        GLES20.glUniform1i(textureParamHandle, 0);

        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 8, textureBuffer);

        GLES20.glUniformMatrix4fv(textureTranformHandle, 1, false, mCameraTransformMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(textureCoordinateHandle);
    }

    @Override
    public boolean handleMessage(Message message) {
        //Log.d(TAG, "handleMessage - " + message);

        /*switch(message.what){
            case SnapshotValidator.ANALYZE :
                switch(mState){
                    case STATE_ANALYZING :
                        if (message.arg1 > SNAPSHOT_SCORE_THRESHOLD) {
                            mState = STATE_CONFIRM_SNAPSHOT;
                        }
                        else{
                            mState = STATE_PREVIEW;
                        }
                        break;
                }
                break;
        }*/
        return true;
    }

    @Override
    public void onCaptureDataReceived(final CameraFragment.CaptureData captureData) {
        //Log.d(TAG, "onCaptureDataReceived - "+captureData);
        //TODO add HUD state and drawing
        switch(mState){
            //Only in PREVIEW
            case STATE_PREVIEW :
                mCurrentCaptureData.lightState = captureData.lightState;
                mCurrentCaptureData.movementState = captureData.movementState;
                mCurrentCaptureData.touchState = captureData.touchState;
                mCurrentCaptureData.cameraState = captureData.cameraState;
                System.arraycopy(captureData.gravity, 0, mCurrentCaptureData.gravity, 0, 3);
                if(captureData.lightState & captureData.movementState & captureData.touchState & captureData.cameraState){
                    mState = STATE_START_ANALYZE;
                }
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // TODO Remove mock for UI events
        mState = STATE_PREVIEW;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                mCameraFragment.setPaused(false);
            }
        });
        return true;
    }

    public void draw() {
        logFPS();
        switch(mState){
            case STATE_PREVIEW :
                drawPreview();
                drawHUD();
                break;
            case STATE_START_ANALYZE :
                drawPreview();
                final Handler handler = snapshotValidator.getHandler();
                //snapshotInstance.callBackHandler = mHandler;
                //snapshotInstance.data.rewind();
                //TODO only capture square inside HUD
                //GLES20.glReadPixels(0, 0, snapshotInstance.width, snapshotInstance.height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, snapshotInstance.data);
                //GlUtil.checkGlError("glReadPixels");
                //snapshotInstance.data.rewind();
                //System.arraycopy(mCurrentCaptureData.gravity, 0, snapshotInstance.gravity, 0, 3);
                //handler.sendMessage(handler.obtainMessage(SnapshotValidator.ANALYZE, snapshotInstance));
                drawHUD();
                mState = STATE_ANALYZING;
                break;
            case STATE_CONFIRM_SNAPSHOT :
                drawConfirmSnapshot();
                break;
        }
    }

    private void drawHUD(){

        //TODO HUD
    }

    private void drawConfirmSnapshot(){
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                mCameraFragment.setPaused(true);
                mCameraFragment.getSurfaceView().setOnTouchListener(CameraRenderer.this);
            }
        });
    }

    public void shutdown() {
        //Log.d(TAG, "shutdown");
        //snapshotValidator.shutdown();
        mState = STATE_SHUTDOWN;
        mHandler.getLooper().quit();
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
        //Log.d(TAG, "getPreviewTexture");
        return mPreviewTexture;
    }

    public void setOnRendererReadyListener(OnRendererReadyListener listener) {
        //Log.d(TAG, "setOnRendererReadyListener");
        mOnRendererReadyListener = listener;

    }


    public void setCameraFragment(CameraFragment cameraFragment) {
        //Log.d(TAG, "setCameraFragment");
        mCameraFragment = cameraFragment;
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
