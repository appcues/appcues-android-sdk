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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

class Appcues internal constructor(
    private val logcues: Logcues,
    private val actionRegistry: ActionRegistry,
    private val traitRegistry: TraitRegistry,
    private val experienceRenderer: ExperienceRenderer,
    private val analyticsTracker: AnalyticsTracker,
    private val session: AppcuesSession
) : CoroutineScope {

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable -> logcues.error(Exception(throwable)) }
    override val coroutineContext = SupervisorJob() + Dispatchers.Main + exceptionHandler

    /**
     * Returns the current version of Appcues SDK
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
        launch {
            session.groupId = groupId
            if (groupId == null || groupId.isEmpty()) {
                // null or empty is removing the group (and no properties allowed)
                analyticsTracker.group(null)
            } else {
                analyticsTracker.group(properties)
            }
        }
    }

    /**
     * Generate a unique Id for the current user when there is not a known identity to use in
     * the {@link identity(String, HashMap<String, Any>) identity} call. This will cause the SDK
     * to begin tracking activity and checking for qualified content.
     */
    fun anonymous(properties: HashMap<String, Any>? = null) {
        // todo - allow config to supply the anon ID factory
        identify(true, UUID.randomUUID().toString(), properties)
    }

    /**
     * Clears out the current user in this session.
     * Can be used when the user logs out of your application.
     */
    fun reset() {
        logcues.info("reset()")
    }

    /**
     * Track an action taken by a user.
     *
     * [name] Name of the event.
     * [properties] Optional properties that provide additional context about the event.
     */
    fun track(name: String, properties: HashMap<String, Any>? = null) {
        launch {
            analyticsTracker.track(name, properties)
        }
    }

    /**
     * Track an screen viewed by a user.
     *
     * [title] Name of the screen
     * [properties] Optional properties that provide additional context about the event.
     */
    fun screen(title: String, properties: HashMap<String, Any>? = null) {
        launch {
            analyticsTracker.screen(title, properties)
        }
    }

    /**
     * Forces specific Appcues content to appear for the current user by passing in the [contentId].
     *
     * [contentId] ID of specific flow.
     */
    fun show(contentId: String) {
        launch {
            experienceRenderer.show(contentId)
        }
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

        val userChanged = session.userId != userId
        session.userId = userId
        session.isAnonymous = isAnonymous
        if (userChanged) {
            // group info is reset on new user
            session.groupId = null
        }

        launch {
            analyticsTracker.identify(properties)
        }
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

        fun build(): Appcues {
            return with(AppcuesKoinContext) {
                createAppcues(
                    context = context,
                    appcuesConfig = AppcuesConfig(
                        accountId = accountId,
                        applicationId = applicationId,
                        loggingLevel = _loggingLevel,
                        apiHostUrl = _apiHostUrl
                    )
                )
            }
        }
    }

    enum class LoggingLevel {
        NONE, INFO, DEBUG
    }
}
