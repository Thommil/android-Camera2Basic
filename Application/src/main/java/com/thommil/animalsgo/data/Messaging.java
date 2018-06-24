package com.thommil.animalsgo.data;

/**
 * Tooling class to centralize messaging system
 */
public class Messaging {

    public static final int SYSTEM_ERROR = 0x000;

    // System
    public static final int SYSTEM_CONNECT_VIEW = 0x001;
    public static final int SYSTEM_CONNECT_VALIDATOR = 0x002;
    public static final int SYSTEM_SHUTDOWN = 0x004;

    // View

    // Validator
    public static final int VALIDATOR_REQUEST = 0x010;
    public static final int VALIDATOR_RESULT = 0x020;

    public static class Snapshot {
        // TODO Snapshot definition
    }
}
