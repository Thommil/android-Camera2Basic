package com.thommil.animalsgo.data;

/**
 * Tooling class to centralize messaging system
 */
public class Messaging {

    public static final int SYSTEM_ERROR = 0x000;

    // SYSTEM
    public static final int SYSTEM_CONNECT_RENDERER = 0x001;
    public static final int SYSTEM_CONNECT_VALIDATOR = 0x002;
    public static final int SYSTEM_ORIENTATION_CHANGE = 0x004;
    public static final int SYSTEM_SHUTDOWN = 0x008;

    // RENDERER
    public static final int CHANGE_PREVIEW_SIZE = 0x010;
    public static final int CHANGE_PREVIEW_PLUGIN = 0x020;
    public static final int CAPTURE_NEXT_FRAME = 0x040;

    // Validator
    public static final int VALIDATION_REQUEST = 0x100;
    public static final int VALIDATION_RESULT = 0x200;

}
