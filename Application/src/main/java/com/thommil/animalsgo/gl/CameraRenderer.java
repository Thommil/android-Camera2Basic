package com.thommil.animalsgo.gl;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
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
import com.thommil.animalsgo.Settings;
import com.thommil.animalsgo.fragments.CameraFragment;
import com.thommil.animalsgo.capture.CaptureBuilder;
import com.thommil.animalsgo.gl.libgl.EglCore;
import com.thommil.animalsgo.gl.libgl.GlFrameBufferObject;
import com.thommil.animalsgo.gl.libgl.GlIntRect;
import com.thommil.animalsgo.gl.libgl.GlOperation;
import com.thommil.animalsgo.gl.libgl.GlTexture;
import com.thommil.animalsgo.gl.libgl.WindowSurface;


// TODO GLRect Class to hide gl textcoords
public class CameraRenderer extends HandlerThread implements SurfaceTexture.OnFrameAvailableListener, Handler.Callback {

    private static final String TAG = "A_GO/CameraRenderer";
    private static final String THREAD_NAME = "CameraRendererThread";

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

    // Camera target texture for FBO
    private GlTexture mCameraPreviewTexture;

    // Camera preview FBO
    private GlFrameBufferObject mCameraPreviewFBO;

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

    // Current CAMERA plugin
    private CameraPlugin mCameraPlugin;

    // Current PREVIEW plugin
    private PreviewPlugin mPreviewPlugin;

    // Current preview size
    private Size mPreviewSize;

    // Current preview viewport
    private final GlIntRect mViewport = new GlIntRect();

    // Current orientation
    private int mOrientation;

    // Current capture zone
    private final GlIntRect mCaptureZone = new GlIntRect();

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
        //Log.d(TAG, "initGL()");
        mEglCore = new EglCore();

        //create preview surface
        mWindowSurface = new WindowSurface(mEglCore, mSurface, true);
        mWindowSurface.makeCurrent();

        mPluginManager.initialize(Plugin.TYPE_CAMERA | Plugin.TYPE_PREVIEW | Plugin.TYPE_UI);
        mCameraPlugin = (CameraPlugin) mPluginManager.getPlugin(Settings.PLUGINS_CAMERA_DEFAULT);
        mPreviewPlugin = (PreviewPlugin) mPluginManager.getPlugin(Settings.getInstance().getString(Settings.PLUGINS_PREVIEW_DEFAULT));


        mPreviewTexture = new SurfaceTexture(mCameraPlugin.getCameraTexture().handle);
        mPreviewTexture.setOnFrameAvailableListener(this);

        GlOperation.setViewport(0, 0, mSurfaceWidth, mSurfaceHeight);

        onSetupComplete();
    }

    public void deinitGL() {
        //Log.d(TAG, "deinitGL()");
        deleteFBOs();
        mPluginManager.destroy();
        mPreviewTexture.release();
        mPreviewTexture.setOnFrameAvailableListener(null);
        mWindowSurface.release();
        mEglCore.release();

    }

    public void onViewportSizeUpdated(final Size previewSize) {
        //Log.d(TAG, "onViewportSizeUpdated("+previewSize+")");

        mPreviewSize = new Size(previewSize.getHeight(), previewSize.getWidth());

        final float surfaceRatio = (float)mSurfaceWidth/(float)mSurfaceHeight;
        final float previewRatio = (float)previewSize.getWidth()/(float)previewSize.getHeight();

        if(surfaceRatio > previewRatio){
            final int viewPortHeight = (int)(mSurfaceWidth / previewRatio);
            mViewport.left = 0;
            mViewport.right = mSurfaceWidth;
            mViewport.bottom = (mSurfaceHeight - viewPortHeight) / 2;
            mViewport.top = mViewport.bottom + viewPortHeight;
        }
        else if(surfaceRatio < previewRatio){
            final int viewPortHeight = (int)(mSurfaceWidth * previewRatio);
            mViewport.left = 0;
            mViewport.right = mSurfaceWidth;
            mViewport.bottom = (mSurfaceHeight - viewPortHeight) / 2;
            mViewport.top = mViewport.bottom + viewPortHeight;
        }
        else{
            mViewport.left = 0;
            mViewport.right = mSurfaceWidth;
            mViewport.bottom = mSurfaceHeight;
            mViewport.top = 0;
        }

        //Log.d(TAG, "Viewport : " + mViewport);

        setupFBOs();
        updateCaptureZone();

    }

    private void updateCaptureZone(){
        final int captureHeight;
        switch (mOrientation) {
            case Orientation.ORIENTATION_90:
            case Orientation.ORIENTATION_270:
                captureHeight = Math.min((int) (mSurfaceWidth / Settings.CAPTURE_RATIO), mPreviewSize.getHeight());
                break;
            default:
                captureHeight = Math.min((int) (mSurfaceWidth * Settings.CAPTURE_RATIO), mPreviewSize.getHeight());
        }

        mCaptureZone.left = 0;
        mCaptureZone.right = mViewport.width();
        mCaptureZone.bottom = (mViewport.height() - captureHeight) / 2;
        mCaptureZone.top = mCaptureZone.bottom + captureHeight;

        //Log.d(TAG, "Capture zone: " + mCaptureZone);
    }


    private void deleteFBOs() {
        //Log.d(TAG, "deleteFBO()");
        if(mCameraPreviewFBO != null){
            mCameraPreviewFBO.free();
        }
        if(mCameraPreviewTexture != null){
            mCameraPreviewTexture.free();
        }
    }

    private void setupFBOs()
    {
        //Log.d(TAG, "setupFBO()");

        deleteFBOs();

        //TODO depending on quality -> change type/format
        mCameraPreviewTexture = new GlTexture(){
            @Override
            public int getType() {
                return GlTexture.TYPE_UNSIGNED_SHORT_5_6_5;
            }

            @Override
            public int getFormat() {
                return GlTexture.FORMAT_RGB;
            }

            @Override
            public int getWidth() {
                return mViewport.width();
            }

            @Override
            public int getHeight() {
                return mViewport.height();
            }

            @Override
            public int getWrapMode(int axeId) {
                return GlTexture.WRAP_CLAMP_TO_EDGE;
            }
        };


        mCameraPreviewFBO = new GlFrameBufferObject();
        mCameraPreviewTexture.bind().configure().allocate();
        mCameraPreviewFBO.attach(mCameraPreviewTexture, GlFrameBufferObject.Attachment.TYPE_COLOR);
        mCameraPreviewTexture.unbind();
        mCameraPreviewFBO.unbind();
    }

    protected void onSetupComplete() {
        //Log.d(TAG, "onSetupComplete()");
        mOnRendererReadyListener.onRendererReady();
    }

    @Override
    public synchronized void start() {
        //Log.d(TAG, "start()");
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
        //Log.d(TAG, "run()");

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
        //long time = System.currentTimeMillis();
        GlOperation.setViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        GlOperation.clearBuffers(GlOperation.BUFFER_COLOR);

        drawCameraPreview();

        switch(mState){
            case STATE_CAPTURE_NEXT_FRAME:
                final Capture capture = CaptureBuilder.getInstance().getCapture();
                capture.mCameraBuffer = mCameraPreviewFBO.read(mCaptureZone.left, mCaptureZone.bottom, mCaptureZone.width(), mCaptureZone.height());
                mMainHandler.sendMessage(mMainHandler.obtainMessage(Messaging.VALIDATION_REQUEST, capture));
                break;
        }

        drawFilter();

        switch(mState){
            case STATE_VALIDATION_IN_PROGRESS:
                // TODO UI effects ?
                break;
            case STATE_VALIDATION_DONE:
                //Log.d(TAG, "Capture done : " + mCurrentCapture);
                // TODO STATE -> Choose/confirm
                //CaptureBuilder.getInstance().getCapture().validationState = Capture.VALIDATION_WAIT;
                mState = STATE_PREVIEW;
                break;
        }

        drawUI();

        //Log.d(TAG, "" + (System.currentTimeMillis() - time) + "ms");
    }

    private final void drawCameraPreview(){
        mCameraPreviewFBO.bind();

        //Camera draw
        GlOperation.setViewport(0, 0, mViewport.width(), mViewport.height());
        mCameraPlugin.setCameraTransformMatrix(mCameraTransformMatrix);
        mCameraPlugin.draw(mViewport, mOrientation);

        mCameraPreviewFBO.unbind();
    }

    private final void drawFilter(){
        GlOperation.setViewport(mViewport.left, mViewport.bottom, mViewport.width(), mViewport.height());
        mPreviewPlugin.setCameraTexture(mCameraPreviewTexture);
        mPreviewPlugin.draw(mViewport, mOrientation);
    }

    private final void drawUI(){
        GlOperation.setViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
    }

    @Override
    public boolean handleMessage(Message message) {
        //Log.d(TAG, "handleMessage(" + message+ ")");
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
            case Messaging.CHANGE_PREVIEW_SIZE:
                if(mState == STATE_PREVIEW){
                    onViewportSizeUpdated((Size)message.obj);
                }
                break;
            case Messaging.CHANGE_PREVIEW_PLUGIN:
                if(mState == STATE_PREVIEW){
                    mPreviewPlugin = (PreviewPlugin) mPluginManager.getPlugin((String)message.obj);
                }
                break;
            case Messaging.CHANGE_CAPTURE: {
                    final Capture capture = (Capture) message.obj;
                    capture.pluginId = mPreviewPlugin.getId();
                    capture.width = mCaptureZone.width();
                    capture.height = mCaptureZone.height();
                }
                break;
            case Messaging.CAPTURE_NEXT_FRAME:
                if(mState == STATE_PREVIEW){
                    mState = STATE_CAPTURE_NEXT_FRAME;
                    final Capture capture = (Capture)message.obj;
                    capture.pluginId = mPreviewPlugin.getId();
                    capture.validationState = Capture.VALIDATION_IN_PROGRESS;
                    capture.width = mCaptureZone.width();
                    capture.height = mCaptureZone.height();
                }
                break;
            case Messaging.VALIDATION_RESULT:
                final Capture capture = (Capture)message.obj;
                switch(((Capture)message.obj).validationState){
                    case Capture.VALIDATION_SUCCEED :
                        mState = STATE_VALIDATION_DONE;
                        break;
                    default:
                        capture.validationState = Capture.VALIDATION_IN_PROGRESS;
                        mState = STATE_PREVIEW;
                }
                break;
        }
        return true;
    }


    protected void shutdown() {
        //Log.d(TAG, "shutdown");
        mHandler.getLooper().quit();
    }

    public SurfaceTexture getPreviewTexture() {
        //Log.d(TAG, "getPreviewTexture()");
        return mPreviewTexture;
    }

    public void setOnRendererReadyListener(OnRendererReadyListener listener) {
        //Log.d(TAG, "setOnRendererReadyListener()");
        mOnRendererReadyListener = listener;

    }


    public void setCameraFragment(CameraFragment cameraFragment) {
        //Log.d(TAG, "setCameraFragment()");
        mCameraFragment = cameraFragment;
    }

    public void setMainHandler(final Handler mainHandler) {
        this.mMainHandler = mainHandler;
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
