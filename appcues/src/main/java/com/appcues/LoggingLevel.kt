package com.appcues

/**
 * Determines the level of output from the Appcues to Logcat.
 */
public enum class LoggingLevel {

    /**
     * Silences all log output.
     */
    NONE,

    /**
     * Provides informational log output about core functions of the SDK.
     */
    INFO,

    /**
     * Diagnostic level output that should normally only be used in local debugging, as it can allow for
     * Exceptions to be unhandled.
     */
    DEBUG
}
