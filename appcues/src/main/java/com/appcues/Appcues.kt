package com.appcues

import android.content.Context
import com.appcues.action.ActionRegistry
import com.appcues.action.ExperienceAction
import com.appcues.analytics.AnalyticsTracker
import com.appcues.builder.ApiHostBuilderValidator
import com.appcues.di.AppcuesKoinContext
import com.appcues.logging.Logcues
import com.appcues.trait.ExperienceTrait
import com.appcues.trait.TraitRegistry
import com.appcues.ui.ExperienceRenderer
import org.koin.core.scope.Scope

class Appcues internal constructor(koinScope: Scope) {

    private val appcuesConfig by koinScope.inject<AppcuesConfig>()
    private val experienceRenderer by koinScope.inject<ExperienceRenderer>()
    private val logcues by koinScope.inject<Logcues>()
    private val actionRegistry by koinScope.inject<ActionRegistry>()
    private val traitRegistry by koinScope.inject<TraitRegistry>()
    private val analyticsTracker by koinScope.inject<AnalyticsTracker>()
    private val storage by koinScope.inject<Storage>()
    private val sessionMonitor by koinScope.inject<SessionMonitor>()

    init {
        sessionMonitor.start()
    }

    /**
     * Returns the current version of Appcues SDK.
     *
     * @return Current version.
     */
    val version: String
        get() = "${BuildConfig.SDK_VERSION}-${BuildConfig.BUILD_TYPE}"

    /**
     * Identify the user and determine if they should see Appcues content.
     *
     * [userId] Unique value identifying the user.
     * [properties] Optional properties that provide additional context about the user.
     */
    fun identify(userId: String, properties: HashMap<String, Any>? = null) {
        identify(false, userId, properties)
    }

    /**
     * Identify a group for the current user.
     *
     * [groupId] Unique value identifying the group.
     * [properties] Optional properties that provide additional context about the group.
     */
    fun group(groupId: String?, properties: HashMap<String, Any>? = null) {
        storage.groupId = groupId

        (if (groupId.isNullOrEmpty()) null else properties).also {
            analyticsTracker.group(it)
        }
    }

    /**
     * Generate a unique Id for the current user when there is not a known identity to use in
     * the {@link identity(String, HashMap<String, Any>) identity} call. This will cause the SDK
     * to begin tracking activity and checking for qualified content.
     */
    fun anonymous(properties: HashMap<String, Any>? = null) {
        // use the device ID as the default anonymous user ID, unless an override for generating
        // anonymous user IDs is supplied in the config builder
        val anonymousId = appcuesConfig.anonymousIdFactory?.invoke() ?: storage.deviceId
        identify(true, anonymousId, properties)
    }

    /**
     * Clears out the current user in this session.
     * Can be used when the user logs out of your application.
     */
    fun reset() {
        sessionMonitor.reset()
        storage.userId = ""
        storage.isAnonymous = true
        storage.groupId = null
    }

    /**
     * Track an action taken by a user.
     *
     * [name] Name of the event.
     * [properties] Optional properties that provide additional context about the event.
     */
    fun track(name: String, properties: HashMap<String, Any>? = null) {
        analyticsTracker.track(name, properties)
    }

    /**
     * Track an screen viewed by a user.
     *
     * [title] Name of the screen
     * [properties] Optional properties that provide additional context about the event.
     */
    fun screen(title: String, properties: HashMap<String, Any>? = null) {
        analyticsTracker.screen(title, properties)
    }

    /**
     * Forces specific Appcues experience to appear for the current user by passing in the [experienceId].
     *
     * [experienceId] ID of the experience.
     */
    fun show(experienceId: String) {
        experienceRenderer.show(experienceId)
    }

    /**
     * Register a trait that can customize an Experience.
     *
     * [type] type of the action that is sent by the experience. ex: "my-trait"
     * [traitFactory] factory (lambda) responsible for creating the ExperienceTrait registered for given [type]
     *
     * usage:
     * registerTrait("my-trait") { MyCustomExperienceTrait() }
     */
    fun registerTrait(type: String, traitFactory: (config: HashMap<String, Any>?) -> ExperienceTrait) {
        traitRegistry.register(type, traitFactory)
    }

    /**
     * Register an action that can be activated in an Experience.
     *
     * [type] type of the action that is sent by the experience. ex: "my-action"
     * [actionFactory] factory (lambda) responsible for creating the ExperienceAction registered for given [type]
     *
     * usage:
     * registerAction("my-action") { MyCustomExperienceAction() }
     */
    fun registerAction(type: String, actionFactory: (config: HashMap<String, Any>?) -> ExperienceAction) {
        actionRegistry.register(type, actionFactory)
    }

    /**
     * Enables automatic screen tracking. (Works for Activities)
     */
    fun trackScreen() {
        logcues.info("trackScreen()")
    }

    /**
     * Set Appcues to start in Debug mode
     */
    fun debug() {
        logcues.info("debug()")
    }

    private fun identify(isAnonymous: Boolean, userId: String, properties: HashMap<String, Any>?) {
        if (userId.isEmpty()) {
            logcues.info("Invalid userId - empty string") // possibly should be an error
            return
        }

        val userChanged = storage.userId != userId
        storage.userId = userId
        storage.isAnonymous = isAnonymous
        if (userChanged) {
            // when the identified user changes from last known value, we must start a new session
            sessionMonitor.start()

            // group info is reset on new user
            storage.groupId = null
        }

        analyticsTracker.identify(properties)
    }

    class Builder(
        private val context: Context,
        private val accountId: String,
        private val applicationId: String
    ) {

        private var _loggingLevel = LoggingLevel.NONE

        fun logging(level: LoggingLevel) = apply {
            _loggingLevel = level
        }

        private var _apiHostUrl: String? = null

        /**
         * Defines a custom api host for the SDK. If Not defined it will point to appcues
         *
         * [url] Custom Url as api host for the SDK. It will throw [IllegalArgumentException] if the given Url
         *       does not start with 'http' and ends with '/'
         */
        fun apiHost(url: String) = apply {
            ApiHostBuilderValidator().run {
                _apiHostUrl = validate(url)
            }
        }

        private var _anonymousIdFactory: (() -> String)? = null

        /**
         * Set the factory responsible for generating anonymous user IDs.
         *
         * [factory] factory (lambda) that returns an ID as a String.
         */
        fun anonymousIdFactory(factory: () -> String) = apply {
            _anonymousIdFactory = factory
        }

        private var _sessionTimeout: Int = AppcuesConfig.SESSION_TIMEOUT_DEFAULT

        /**
         *  Set the session timeout for the configuration. This timeout value is used to determine if a new session is started
         *  upon the application returning to the foreground. The default value is 1800 seconds (30 minutes).
         *
         *  [sessionTimeout] The timeout length, in seconds.
         */
        fun sessionTimeout(timeout: Int) = apply {
            _sessionTimeout = timeout.coerceAtLeast(0)
        }

        private var _activityStorageMaxSize: Int = AppcuesConfig.ACTIVITY_STORAGE_MAX_SIZE

        /**
         * Set the activity storage max size for the configuration.  This value determines how many analytics requests can be
         * stored on the local device and retried later, in the case of the device network connection being unavailable.
         * Only the most recent requests, up to this count, are retained.
         *
         * [size] The number of items to store, maximum 25, minimum 0.
         */
        fun activityStorageMaxSize(size: Int) = apply {
            _activityStorageMaxSize = size.coerceAtLeast(0).coerceAtMost(AppcuesConfig.ACTIVITY_STORAGE_MAX_SIZE)
        }

        private var _activityStorageMaxAge: Int? = null

        /**
         *  Sets the activity storage max age for the configuration.  This value determines how long an item can be stored
         *  on the local device and retried later, in the case of hte device network connection being unavailable.  Only
         *  requests that are more recent than the max age will be retried - or all, if not set.
         *
         *  [age] The max age, in seconds, since now.  The default is `nil`, meaning no max age.
         */
        fun activityStorageMaxAge(age: Int) = apply {
            _activityStorageMaxAge = age.coerceAtLeast(0)
        }

        private var _interceptor: AppcuesInterceptor? = null

        /**
         * Set the interceptor for additional control over SDK runtime behaviors.
         *
         * [interceptor] The interceptor to use.
         */
        fun interceptor(interceptor: AppcuesInterceptor) = apply {
            _interceptor = interceptor
        }

        fun build(): Appcues {
            return with(AppcuesKoinContext) {
                createAppcues(
                    context = context,
                    appcuesConfig = AppcuesConfig(
                        accountId = accountId,
                        applicationId = applicationId,
                        loggingLevel = _loggingLevel,
                        apiHostUrl = _apiHostUrl,
                        anonymousIdFactory = _anonymousIdFactory,
                        sessionTimeout = _sessionTimeout,
                        activityStorageMaxSize = _activityStorageMaxSize,
                        activityStorageMaxAge = _activityStorageMaxAge,
                        interceptor = _interceptor,
                    )
                )
            }
        }
    }

    enum class LoggingLevel {
        NONE, INFO, DEBUG
    }
}
