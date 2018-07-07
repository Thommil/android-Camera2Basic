package com.thommil.animalsgo.cv;

import com.thommil.animalsgo.data.Capture;

public class OpenCVProcessor implements ImageProcessor {

    static{
        System.loadLibrary("animals-go");
    }

    public native void validateCapture(final Capture capture);
}
