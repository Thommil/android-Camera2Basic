package com.thommil.animalsgo.data;

/**
 * Tooling class to centralize messaging system
 */
public class Messaging {

    public static final int SYSTEM_ERROR = 0x000;

    // SYSTEM
    public static final int SYSTEM_CONNECT_RENDERER = 0x0001;
    public static final int SYSTEM_CONNECT_VALIDATOR = 0x0002;
    public static final int SYSTEM_ORIENTATION_CHANGE = 0x0004;
    public static final int SYSTEM_SHUTDOWN = 0x0008;

    // RENDERER
    public static final int CHANGE_CAMERA_PLUGIN = 0x0010;
    public static final int CHANGE_PREVIEW_SIZE = 0x0020;
    public static final int CHANGE_PREVIEW_PLUGIN = 0x0040;
    public static final int CHANGE_CAPTURE = 0x0080;
    public static final int CHANGE_ZOOM = 0x0100;
    public static final int CAPTURE_NEXT_FRAME = 0x0200;

    // Validator
    public static final int VALIDATION_REQUEST = 0x1000;
    public static final int VALIDATION_RESULT = 0x2000;

}
