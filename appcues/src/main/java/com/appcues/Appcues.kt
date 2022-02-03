package com.appcues

import android.app.Activity
import android.content.Context
import com.appcues.action.ExperienceAction
import com.appcues.builder.ApiHostBuilderValidator
import com.appcues.di.AppcuesKoinContext
import com.appcues.di.newAppcuesInstance
import com.appcues.di.startKoinOnce
import com.appcues.logging.Logcues
import com.appcues.monitor.ActivityMonitor
import com.appcues.trait.ExperienceTrait

class Appcues internal constructor(
    private val logcues: Logcues,
    private val activityMonitor: ActivityMonitor,
) {

    /**
     * Returns the current version of Appcues SDK
     */
    val version: String
        get() = "0.1.0"

    /**
     * Identify the user and determine if they should see Appcues content.
     *
     * [userId] Unique value identifying the user.
     * [properties] Optional properties that provide additional context about the user.
     */
    fun identify(userId: String, properties: HashMap<String, Any>? = null) {
        logcues.i("identify(userId: $userId, properties: $properties)")
    }

    /**
     * Identify a group for the current user.
     *
     * [groupId] Unique value identifying the group.
     * [properties] Optional properties that provide additional context about the group.
     */
    fun group(groupId: String?, properties: HashMap<String, Any>? = null) {
        logcues.i("group(groupId: $groupId, properties: $properties)")
    }

    /**
     * Generate a unique Id for the current user when there is not a known identity to use in
     * the {@link identity(String, HashMap<String, Any>) identity} call. This will cause the SDK
     * to begin tracking activity and checking for qualified content.
     */
    fun anonymous(properties: HashMap<String, Any>? = null) {
        logcues.i("anonymous(properties: $properties)")
    }

    /**
     * Clears out the current user in this session.
     * Can be used when the user logs out of your application.
     */
    fun reset() {
        logcues.i("reset()")
    }

    /**
     * Track an action taken by a user.
     *
     * [name] Name of the event.
     * [properties] Optional properties that provide additional context about the event.
     */
    fun track(name: String, properties: HashMap<String, Any>? = null) {
        logcues.i("track(name: $name, properties: $properties)")
    }

    /**
     * Track an screen viewed by a user.
     *
     * [title] Name of the screen
     * [properties] Optional properties that provide additional context about the event.
     */
    fun screen(title: String, properties: HashMap<String, Any>? = null) {
        logcues.i("screen(title: $title, properties: $properties)")
    }

    /**
     * Forces specific Appcues content to appear for the current user by passing in the [contentId].
     *
     * [contentId] ID of specific flow.
     */
    fun show(contentId: String) {
        activityMonitor.getCustomerViewModel()?.show(contentId)
    }

    /**
     * Register a trait that modifies an Experience.
     *
     * [experienceTrait] Trait to register
     */
    fun register(experienceTrait: ExperienceTrait) {
        logcues.i("register(trait: $experienceTrait)")
    }

    /**
     * Register an action that can be activated in an Experience.
     *
     * [experienceAction] Action to register.
     */
    fun register(experienceAction: ExperienceAction) {
        logcues.i("register(action: $experienceAction)")
    }

    /**
     * Enables automatic screen tracking. (Works for Activities)
     */
    fun trackScreen() {
        logcues.i("trackScreen()")
    }

    /**
     * Set Appcues to start in Debug mode
     */
    fun debug() {
        logcues.i("debug()")
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

        private var _activity: Activity? = null

        fun activity(activity: Activity) = apply {
            _activity = activity
        }

        fun build(): Appcues {
            return with(AppcuesKoinContext) {
                startKoinOnce(context)

                newAppcuesInstance(
                    appcuesConfig = AppcuesConfig(
                        accountId = accountId,
                        applicationId = applicationId,
                        loggingLevel = _loggingLevel,
                        apiHostUrl = _apiHostUrl,
                        activity = _activity
                    )
                )
            }
        }
    }

    enum class LoggingLevel {
        NONE, BASIC
    }
}
