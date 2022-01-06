package com.appcues

import android.content.Context
import com.appcues.action.ExperienceAction
import com.appcues.di.DependencyProvider
import com.appcues.logging.Logcues
import com.appcues.monitor.ActivityMonitor
import com.appcues.trait.ExperienceTrait

class Appcues internal constructor(dependencyProvider: DependencyProvider) {

    private val logcues: Logcues = dependencyProvider.get()

    private val activityMonitor: ActivityMonitor = dependencyProvider.get()

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
    fun group(groupId: String, properties: HashMap<String, Any>? = null) {
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
        logcues.i("track(name: $name, properties: $properties")
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
        logcues.i("show(contentId: $contentId): Activity: ${activityMonitor.activity?.localClassName}")
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

        fun build(): Appcues {
            return Appcues(
                dependencyProvider = DependencyProvider(
                    context = context,
                    config = AppcuesConfig(
                        accountId = accountId,
                        applicationId = applicationId,
                        loggingLevel = _loggingLevel
                    )
                )
            )
        }
    }

    enum class LoggingLevel {
        NONE, BASIC
    }
}
