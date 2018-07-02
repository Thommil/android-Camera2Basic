package com.thommil.animalsgo.gl;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.thommil.animalsgo.capture.Capture;
import com.thommil.animalsgo.data.Messaging;
import com.thommil.animalsgo.data.Orientation;
import com.thommil.animalsgo.data.Settings;
import com.thommil.animalsgo.fragments.CameraFragment;
import com.thommil.animalsgo.capture.CaptureBuilder;
import com.thommil.animalsgo.gl.libgl.EglCore;
import com.thommil.animalsgo.gl.libgl.GlOperation;
import com.thommil.animalsgo.gl.libgl.GlProgram;
import com.thommil.animalsgo.gl.libgl.WindowSurface;
import com.thommil.animalsgo.utils.ByteBufferPool;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;


// TODO Import KeskGL (chunks for draw, RBO for photo capture...)
// TODO GLRect Class to hide gl textcoords
public class CameraRenderer extends HandlerThread implements SurfaceTexture.OnFrameAvailableListener, CameraFragment.OnViewportSizeUpdatedListener, Handler.Callback {

    private static final String TAG = "A_GO/CameraRenderer";
    private static final String THREAD_NAME = "CameraRendererThread";

    private final String CAMERA_FRAGMENT_SHADER = "camera.frag.glsl";
    private final String CAMERA_VERTEX_SHADER = "camera.vert.glsl";

    // Machine states
    private final static int STATE_ERROR = 0x00;
    private final static int STATE_PREVIEW = 0x01;
    private final static int STATE_CAPTURE_NEXT_FRAME = 0x02;
    private final static int STATE_VALIDATION_IN_PROGRESS = 0x04;
    private final static int STATE_VALIDATION_DONE = 0x08;

    // Current Thread state
    private int mState = STATE_PREVIEW;

    // Current context for use with utility methods
    protected Context mContext;

    // Underlying surface dimensions
    private int mSurfaceWidth, mSurfaceHeight;

    // main texture for display, based on TextureView that is created in activity or fragment
    // and passed in after onSurfaceTextureAvailable is called, guaranteeing its existence.
    private Surface mSurface;

    // EGLCore used for creating WindowSurface for preview
    private EglCore mEglCore;

    // Primary WindowSurface for rendering to screen
    private WindowSurface mWindowSurface;

    // Texture created for GLES rendering of camera data
    private SurfaceTexture mPreviewTexture;

    // Cmaera program
    private GlProgram mCameraProgram;

    private final float mVertexCoords[] = {
            -1.0f,  -1.0f,
            -1.0f,   1.0f,
            1.0f,  -1.0f,
            1.0f,   1.0f
    };

    // Texture coord buffer
    private FloatBuffer mTextureBuffer;

    // Texture coords values (can be modified)
    private final float mTextureCoords[] = {
            0.0f,  0.0f,
            0.0f,  1.0f,
            1.0f,  0.0f,
            1.0f,  1.0f,
    };

    // Shader handle for texture coordinates
    private int textureCoordinateHandle;

    // Shader handle for texture ID
    private int mTextureParamHandle;

    // Shader handle for texture tranform matrix
    private int mTextureTranformHandle;

    // Texture verrices buffer
    private FloatBuffer mVertexBuffer;

    // GSLG handle for texture vertices
    private int mPositionHandle;

    // Cam texture ID
    private int mCamTextureId;

    // FBO ID
    private int mFBOId;

    // FBO texture ID
    private int mFBOTextureId;

    // matrix for transforming our camera texture, available immediately after PreviewTexture.updateTexImage()
    private final float[] mCameraTransformMatrix = new float[16];

    // Handler for communcation with the UI thread. Implementation below at
    private Handler mHandler;

    // Main handler
    private Handler mMainHandler;

    // Interface listener for some callbacks to the UI thread when rendering is setup and finished.
    private OnRendererReadyListener mOnRendererReadyListener;

    // Reference to our users CameraFragment to ease setting viewport size. Thought about decoupling but wasn't
    protected CameraFragment mCameraFragment;

    // Plugins manager
    private PluginManager mPluginManager;

    // Current plugin
    private Plugin mPlugin;

    // Current preview size
    private Size mPreviewSize;

    // Current orientation
    private int mOrientation;

    // Current capture zone
    private final Rect mCaptureZone = new Rect();

    // Buffer for storing capture data
    private ByteBuffer mCaptureBuffer;

    // Capture currently built
    private Capture mCurrentCapture;

    public CameraRenderer(Context context, Surface surface, int width, int height) {
        super(THREAD_NAME);

        mContext = context;
        mSurface = surface;

        mSurfaceWidth = width;
        mSurfaceHeight = height;

        mPluginManager = PluginManager.getInstance(context);

        mOrientation = Orientation.ORIENTATION_0;
        mState = STATE_PREVIEW;
    }

    public void initGL() {
        Log.d(TAG, "initGL()");
        mEglCore = new EglCore();

        //create preview surface
        mWindowSurface = new WindowSurface(mEglCore, mSurface, true);
        mWindowSurface.makeCurrent();

        initGLComponents();
    }

    protected void initGLComponents() {
        Log.d(TAG, "initGLComponents()");

        setupVertexBuffer();
        setupCameraTextureCoords();
        setupCameraTexture();
        setupShaders();
        setupFBO();

        GLES20.glClearColor(0,0,0, 1);

        mPluginManager.initialize(Plugin.TYPE_PREVIEW | Plugin.TYPE_CAPTURE | Plugin.TYPE_UI);
        mPlugin = mPluginManager.getPlugin(Settings.getInstance().getString(Settings.PLUGINS_DEFAULT));

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
        deleteFBO();
        GLES20.glDeleteTextures(1, new int[]{mCamTextureId}, 0);
        mCameraProgram.free();
        ByteBufferPool.getInstance().returnDirectBuffer(mVertexBuffer);
        ByteBufferPool.getInstance().returnDirectBuffer(mTextureBuffer);
        ByteBufferPool.getInstance().returnDirectBuffer(mCaptureBuffer);
        mPluginManager.destroy();
        mPreviewTexture.release();
        mPreviewTexture.setOnFrameAvailableListener(null);
    }

    @Override
    public void onViewportSizeUpdated(Size surfaceSize, Size previewSize) {
        Log.d(TAG, "onViewportSizeUpdated(" +surfaceSize+", "+previewSize+")");
        mPreviewSize = new Size(previewSize.getHeight(), previewSize.getWidth());

        final float surfaceRatio = (float)surfaceSize.getWidth()/(float)surfaceSize.getHeight();
        final float drawRatio = (float)mPreviewSize.getWidth()/(float)mPreviewSize.getHeight();

        Log.d(TAG, "Ratios - Surface : " +surfaceRatio+", Draw : "+drawRatio);

        if(surfaceRatio > drawRatio){
            mVertexCoords[1] = mVertexCoords[5] = -(1.0f + (surfaceRatio - drawRatio));
            mVertexCoords[3] = mVertexCoords[7] = (1.0f + (surfaceRatio - drawRatio));

        }
        else if(surfaceRatio < drawRatio){
            mVertexCoords[1] = mVertexCoords[5] = -(1.0f - (drawRatio - surfaceRatio));
            mVertexCoords[3] = mVertexCoords[7] = (1.0f - (drawRatio - surfaceRatio));
        }

        Log.d(TAG, "Adapted vertices : " + Arrays.toString(mVertexCoords));

        updateCaptureZone();
        setupVertexBuffer();
    }

    private void updateCaptureZone(){
        if(mCaptureBuffer == null) {
            if (mSurfaceHeight > mSurfaceWidth) {
                mCaptureBuffer = ByteBufferPool.getInstance().getDirectByteBuffer((int) (mSurfaceWidth / Settings.CAPTURE_RATIO) * mSurfaceWidth * Integer.BYTES);

            } else {
                mCaptureBuffer = ByteBufferPool.getInstance().getDirectByteBuffer((int) (mSurfaceHeight / Settings.CAPTURE_RATIO) * mSurfaceHeight * Integer.BYTES);
            }
            mCaptureBuffer.order(ByteOrder.LITTLE_ENDIAN);
            mCaptureBuffer.rewind();
        }

        final int captureHeight;
        switch (mOrientation) {
            case Orientation.ORIENTATION_90:
            case Orientation.ORIENTATION_270:
                captureHeight = (int) (mSurfaceWidth / Settings.CAPTURE_RATIO);
                break;
            default:
                captureHeight = (int) (mSurfaceWidth * Settings.CAPTURE_RATIO);
        }
        mCaptureZone.left = 0;
        mCaptureZone.right = mSurfaceWidth;
        mCaptureZone.bottom = (mSurfaceHeight - captureHeight) / 2;
        mCaptureZone.top = mCaptureZone.bottom + captureHeight;

        Log.d(TAG, "Capture size : " + mCaptureZone);
    }


    protected void setupVertexBuffer() {
        Log.d(TAG, "setupVertexBuffer()");
        // Initialize the texture holder
        if(mVertexBuffer == null) {
            mVertexBuffer = ByteBufferPool.getInstance().getDirectFloatBuffer(mVertexCoords.length);
        }
        else{
            mVertexBuffer.position(0);
        }
        mVertexBuffer.put(mVertexCoords);
        mVertexBuffer.position(0);
    }

    protected void setupCameraTextureCoords(){
        Log.d(TAG, "setupCameraTextureCoord()");
        if(mTextureBuffer == null) {
            mTextureBuffer = ByteBufferPool.getInstance().getDirectFloatBuffer(mTextureCoords.length);
        }
        else{
            mTextureBuffer.position(0);
        }
        mTextureBuffer.put(mTextureCoords);
        mTextureBuffer.position(0);
    }

    protected void setupCameraTexture() {
        Log.d(TAG, "setupCameraTexture()");

        final int[] texturesId = new int[1];
        GLES20.glGenTextures(1, texturesId , 0);
        GlOperation.checkGlError("Texture generate");
        mCamTextureId = texturesId[0];

        //set texture[0] to camera texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mCamTextureId);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GlOperation.checkGlError("Texture bind");

        mPreviewTexture = new SurfaceTexture(mCamTextureId);
        mPreviewTexture.setOnFrameAvailableListener(this);
    }

    protected void setupShaders() {
        Log.d(TAG, "setupShaders()");
        InputStream vertexInputStream = null, fragmentInputStream = null;
        try {
            vertexInputStream = mContext.getAssets().open(CAMERA_VERTEX_SHADER);
            fragmentInputStream = mContext.getAssets().open(CAMERA_FRAGMENT_SHADER);

            mCameraProgram = new GlProgram(vertexInputStream, fragmentInputStream);

            mCameraProgram.use();
            mTextureParamHandle = mCameraProgram.getUniformHandle("camTexture");
            mTextureTranformHandle = mCameraProgram.getUniformHandle("camTextureTransform");
            textureCoordinateHandle = mCameraProgram.getAttributeHandle("camTexCoordinate");
            mPositionHandle = mCameraProgram.getAttributeHandle("position");

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

    private void deleteFBO() {
        Log.d(TAG, "deleteFBO()");
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE);
        GLES20.glDeleteFramebuffers(1, new int[]{mFBOId}, 0);
        GLES20.glDeleteTextures(1, new int[]{mFBOTextureId}, 0);
    }

    private void setupFBO()
    {
        Log.d(TAG, "setupFBO()");

        deleteFBO();

        final int[] ids = new int[1];

        GLES20.glGenTextures(1, ids, 0);
        mFBOTextureId = ids[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFBOTextureId);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mSurfaceWidth, mSurfaceHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        //int hFBO;
        GLES20.glGenFramebuffers(1, ids, 0);
        mFBOId = ids[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFBOId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mFBOTextureId, 0);
        GlOperation.checkGlError("initFBO error");

        final int FBOstatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (FBOstatus != GLES20.GL_FRAMEBUFFER_COMPLETE)
            GlOperation.checkGlError("initFBO failed, status : " + FBOstatus);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE);
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


    private void draw(){
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFBOId);

        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //Camera shader -> FBO
        mCameraProgram.use();
        mCameraProgram.enableAttributes();

        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 8, mVertexBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mCamTextureId);
        GLES20.glUniform1i(mTextureParamHandle, 0);

        GLES20.glVertexAttribPointer(textureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 8, mTextureBuffer);

        //TODO Transform matrix when android < 6 (using accelerometer)
        //TODO Transform matrix on zoom
        GLES20.glUniformMatrix4fv(mTextureTranformHandle, 1, false, mCameraTransformMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        mCameraProgram.disableAttributes();

        if(mState == STATE_CAPTURE_NEXT_FRAME){
            mCaptureBuffer.rewind();
            GLES20.glReadPixels(mCaptureZone.left, mCaptureZone.bottom, mCaptureZone.width(), Math.abs(mCaptureZone.height()), GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mCaptureBuffer);
            mCaptureBuffer.rewind();
            mCurrentCapture.mOriginalBuffer = mCaptureBuffer.asReadOnlyBuffer();
            mMainHandler.sendMessage(mMainHandler.obtainMessage(Messaging.VALIDATION_REQUEST, mCurrentCapture));
            mState = STATE_VALIDATION_IN_PROGRESS;
            /*Bitmap bmp = Bitmap.createBitmap(mCaptureZone.width(), -mCaptureZone.height(), Bitmap.Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(mCaptureBuffer);
            Mat mat = new Mat(-mCaptureZone.height(), mCaptureZone.width(), CvType.CV_8UC4, mCaptureBuffer);
            //Mat mat = new Mat(-mCaptureZone.height(), mCaptureZone.width(), CvType.CV_8UC4);
            //Utils.bitmapToMat(bmp, mat);
            Mat dst = new Mat(-mCaptureZone.height(), mCaptureZone.width(), CvType.CV_8UC4);
            Imgproc.Canny(mat, dst, 300, 600, 5, true);
            //Photo.stylization(mat, dst);

            //Utils.matToBitmap(dst, bmp);
            //mCaptureBuffer.
            //Bitmap bmp = Bitmap.createBitmap(mCaptureZone.width(), -mCaptureZone.height(), Bitmap.Config.ARGB_8888);
            //bmp.copyPixelsFromBuffer(mCaptureBuffer);
            mState = STATE_VALIDATION_IN_PROGRESS;
            //bmp.recycle();*/
            /*
            int width = getWidth();
            int height = getHeight();
            ByteBuffer buf = ByteBuffer.allocateDirect(width * height * 4);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            GLES20.glReadPixels(0, 0, width, height,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
            GlUtil.checkGlError("glReadPixels");
            buf.rewind();

            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(buf);
                 */
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE);

        //Plugin draw
        mPlugin.draw(mFBOTextureId, mSurfaceWidth, mSurfaceHeight, mOrientation);

        switch(mState){
            case STATE_VALIDATION_IN_PROGRESS:
                // TODO write RBO
                // TODO possible scan effects
                break;
            case STATE_VALIDATION_DONE:
                // TODO read RBO
                // TODO STATE -> Choose/confirm
                Log.d(TAG, "Capture done : " + mCurrentCapture);
                mState = STATE_PREVIEW;
                CaptureBuilder.getInstance().releaseCapture(mCurrentCapture);
                mCurrentCapture = null;
                break;
        }

        //UI draw
        drawUI();
    }

    private void drawUI(){

    }

    @Override
    public boolean handleMessage(Message message) {
        Log.d(TAG, "handleMessage(" + message);
        switch(message.what){
            case Messaging.SYSTEM_SHUTDOWN:
                shutdown();
                break;
            case Messaging.SYSTEM_ORIENTATION_CHANGE:
                if(mState == STATE_PREVIEW) {
                    mOrientation = ((Orientation) message.obj).getOrientation();
                    updateCaptureZone();
                }
                break;
            case Messaging.CHANGE_PLUGIN:
                if(mState == STATE_PREVIEW){
                    mPlugin = mPluginManager.getPlugin((String)message.obj);
                }
                break;
            case Messaging.CAPTURE_NEXT_FRAME:
                if(mState == STATE_PREVIEW){
                    mState = STATE_CAPTURE_NEXT_FRAME;
                    mCurrentCapture = (Capture)message.obj;
                    mCurrentCapture.validationState = Capture.VALIDATION_IN_PROGRESS;
                    mCurrentCapture.width = Math.abs(mCaptureZone.width());
                    mCurrentCapture.height = Math.abs(mCaptureZone.height());
                }
                break;
            case Messaging.VALIDATION_DONE:
                mCurrentCapture = (Capture)message.obj;
                switch(mCurrentCapture.validationState){
                    case Capture.VALIDATION_SUCCEED :
                        mState = STATE_VALIDATION_DONE;
                        break;
                    default:
                        mState = STATE_PREVIEW;
                        CaptureBuilder.getInstance().releaseCapture(mCurrentCapture);
                        mCurrentCapture = null;
                }
                break;
        }
        return true;
    }


    protected void shutdown() {
        Log.d(TAG, "shutdown");
        mHandler.getLooper().quit();
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

    private void showError(final int messageResourceId){
        mState = STATE_ERROR;
        if(mMainHandler != null){
            mMainHandler.sendMessage(mMainHandler.obtainMessage(Messaging.SYSTEM_ERROR, messageResourceId));
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
