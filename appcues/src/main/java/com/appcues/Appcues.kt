package com.appcues

import android.content.Context
import com.appcues.action.ActionRegistry
import com.appcues.action.ExperienceAction
import com.appcues.builder.ApiHostBuilderValidator
import com.appcues.di.AppcuesKoinContext
import com.appcues.logging.Logcues
import com.appcues.trait.ExperienceTrait

class Appcues internal constructor(
    private val logcues: Logcues,
    private val appcuesScope: AppcuesScope,
    private val actionRegistry: ActionRegistry
) {

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
        logcues.info("identify(userId: $userId, properties: $properties)")
    }

    /**
     * Identify a group for the current user.
     *
     * [groupId] Unique value identifying the group.
     * [properties] Optional properties that provide additional context about the group.
     */
    fun group(groupId: String?, properties: HashMap<String, Any>? = null) {
        logcues.info("group(groupId: $groupId, properties: $properties)")
    }

    /**
     * Generate a unique Id for the current user when there is not a known identity to use in
     * the {@link identity(String, HashMap<String, Any>) identity} call. This will cause the SDK
     * to begin tracking activity and checking for qualified content.
     */
    fun anonymous(properties: HashMap<String, Any>? = null) {
        logcues.info("anonymous(properties: $properties)")
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
        logcues.info("track(name: $name, properties: $properties)")
    }

    /**
     * Track an screen viewed by a user.
     *
     * [title] Name of the screen
     * [properties] Optional properties that provide additional context about the event.
     */
    fun screen(title: String, properties: HashMap<String, Any>? = null) {
        logcues.info("screen(title: $title, properties: $properties)")
    }

    /**
     * Forces specific Appcues content to appear for the current user by passing in the [contentId].
     *
     * [contentId] ID of specific flow.
     */
    fun show(contentId: String) {
        appcuesScope.show(contentId)
    }

    /**
     * Register a trait that modifies an Experience.
     *
     * [experienceTrait] Trait to register
     */
    fun register(experienceTrait: ExperienceTrait) {
        logcues.info("register(trait: $experienceTrait)")
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
