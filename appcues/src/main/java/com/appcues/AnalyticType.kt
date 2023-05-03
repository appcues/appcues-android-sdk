package com.appcues

/**
 * Represents the different types of analytics that can be reported.
 */
public enum class AnalyticType {

    /**
     * Identified user.
     */
    IDENTIFY,

    /**
     * Associated the current user with a group.
     */
    GROUP,

    /**
     * An event representing a user interaction, or internal tracking such as the Experience lifecycle.
     */
    EVENT,

    /**
     * Tracked a screen view in the application.
     */
    SCREEN
}
