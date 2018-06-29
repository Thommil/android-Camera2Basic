package com.thommil.animalsgo.data;

/**
 * Tooling class to centralize messaging system
 */
public class Messaging {

    public static final int SYSTEM_ERROR = 0x000;

    // SYSTEM
    public static final int SYSTEM_CONNECT_RENDERER = 0x001;
    public static final int SYSTEM_CONNECT_VALIDATOR = 0x002;
    public static final int SYSTEM_SHUTDOWN = 0x004;

    // RENDERER
    public static final int RENDERER_CHANGE_PLUGIN = 0x010;

    // Validator
    public static final int VALIDATION_REQUEST = 0x100;
    public static final int VALIDATION_RESULT = 0x200;

}
