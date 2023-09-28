package com.appcues

import coil.ImageLoader
import com.appcues.LoggingLevel.NONE

/**
 * A configuration object that defines the behavior and policies for Appcues.
 */
public data class AppcuesConfig internal constructor(
    internal val accountId: String,
    internal val applicationId: String,
) {

    internal companion object {

        const val SESSION_TIMEOUT_DEFAULT = 300 // 5 minutes by default
        const val ACTIVITY_STORAGE_MAX_SIZE = 25
    }

    /**
     * Set the logging level for the SDK.
     */
    var loggingLevel: LoggingLevel = NONE

    /**
     * Defines a custom api base path for the SDK.  This path should consist of the scheme, host, and any additional
     * path prefix required. If Not defined it will point to the default Appcues host: https://api.appcues.net/
     */
    var apiBasePath: String? = null

    /**
     * Set the factory responsible for generating anonymous user IDs.
     */
    var anonymousIdFactory: (() -> String)? = null

    /**
     *  Set the session timeout for the configuration, in seconds. This timeout value is used to determine if a new session is started
     *  after a period of inactivity, or upon the application returning to the foreground. The default value is 300 seconds (5 minutes).
     */
    var sessionTimeout: Int = SESSION_TIMEOUT_DEFAULT
        set(value) {
            field = value.coerceAtLeast(0)
        }

    /**
     * Set the activity storage max size for the configuration - maximum 25, minimum 0. This value determines how many analytics
     * requests can be stored on the local device and retried later, in the case of the device network connection being unavailable.
     * Only the most recent requests, up to this count, are retained.
     */
    var activityStorageMaxSize: Int = ACTIVITY_STORAGE_MAX_SIZE
        set(value) {
            field = value.coerceAtLeast(0).coerceAtMost(ACTIVITY_STORAGE_MAX_SIZE)
        }

    /**
     *  Sets the activity storage max age for the configuration, in seconds.  This value determines how long an item can be stored
     *  on the local device and retried later, in the case of the device network connection being unavailable.  Only
     *  requests that are more recent than the max age will be retried - or all, if not set.
     */
    var activityStorageMaxAge: Int? = null
        set(value) {
            field = value?.coerceAtLeast(0)
        }

    /**
     * Set the interceptor for additional control over SDK runtime behaviors.
     */
    var interceptor: AppcuesInterceptor? = null

    /**
     * Set the listener to be notified about the display of Experience content.
     */
    var experienceListener: ExperienceListener? = null

    /**
     * Sets the listener to be notified about published analytics.
     */
    var analyticsListener: AnalyticsListener? = null

    /**
     * Sets the handler to use for link navigation.
     */
    var navigationHandler: NavigationHandler? = null

    /**
     * Define a set of additional properties that should be included in the auto properties sent on every
     * analytics tracking call made by the SDK. The additional property names must be unique from those already
     * captured in the SDK. If a property name conflicts with an existing auto property generated by the SDK,
     * it is ignored.
     */
    var additionalAutoProperties: Map<String, Any> = emptyMap()

    /**
     * Sets a custom path to use when looking for any application-specific fonts in the application assets.
     * The fonts in /assets/fonts will always be made available. Any custom path here would be in addition.
     */
    var fontAssetPath: String? = null

    // internally used for ui test on debug variant
    internal var imageLoader: ImageLoader? = null
}
