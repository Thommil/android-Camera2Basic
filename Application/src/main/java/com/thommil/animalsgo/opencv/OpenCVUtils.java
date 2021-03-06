package com.thommil.animalsgo.opencv;

import android.util.Log;

import org.opencv.android.OpenCVLoader;

/**
 *  OpenCV Tooling
 */
public class OpenCVUtils {

    private static final String TAG = "A_GO/OpenCVUtils";

    private static boolean OPENCV_AVAILABLE = false;

    // Initializes OpenCV
    static {
        OPENCV_AVAILABLE = OpenCVLoader.initDebug();
        if (OPENCV_AVAILABLE ) {
            Log.i(TAG, "OpenCV initialize success");
        } else {
            Log.i(TAG, "OpenCV initialize failed");
        }
    }

    /**
     * Initialize OpenCV
     *
     * @return true if available and initialized
     */
    public static boolean init() {
        return OPENCV_AVAILABLE;
    }

    /**
     * Indicates if OpenCV is available in app
     *
     * @return true if available and initialized
     */
    public static boolean isAvailable() {
        return OPENCV_AVAILABLE;
    }
}
