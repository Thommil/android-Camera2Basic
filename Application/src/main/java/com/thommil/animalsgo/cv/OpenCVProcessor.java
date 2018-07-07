package com.thommil.animalsgo.cv;

import com.thommil.animalsgo.data.Capture;

public class OpenCVProcessor implements ImageProcessor {

    static{
        System.loadLibrary("cv-opencv");
    }

    public native void validateCapture(final Capture capture);
}
