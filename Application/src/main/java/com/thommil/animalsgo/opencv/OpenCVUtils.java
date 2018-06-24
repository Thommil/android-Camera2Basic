package com.thommil.animalsgo.opencv;

import android.util.Log;

import org.opencv.android.OpenCVLoader;

/**
 *  OpenCV Tooling
 */
public class OpenCVUtils {

    private static final String TAG = "A_GO/OpenCVUtils";

    private static boolean sIsAvailable = false;

    // Initializes OpenCV
    static {
        sIsAvailable = OpenCVLoader.initDebug();
        if (sIsAvailable ) {
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
        Log.d(TAG, "init()");
        return sIsAvailable;
    }

    /**
     * Indicates if OpenCV is available in app
     *
     * @return true if available and initialized
     */
    public static boolean isAvailable() {
        return sIsAvailable;
    }
}
