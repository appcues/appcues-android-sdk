package com.appcues

import android.app.Application
import android.content.Context
import com.appcues.action.Action
import com.appcues.logging.Logcues
import com.appcues.monitor.ActivityMonitor
import com.appcues.trait.Trait

class Appcues private constructor(private val context: Context, private val config: AppcuesConfig) {

    private val activityMonitor = ActivityMonitor(context as Application)

    /**
     * Returns the current version of Appcues SDK
     */
    val version: String
        get() = "1.0.0"

    /**
     * Identify the user and determine if they should see Appcues content.
     *
     * [userId] Unique value identifying the user.
     * [properties] Optional properties that provide additional context about the user.
     */
    fun identify(userId: String, properties: HashMap<String, Any>? = null) {
        Logcues.i("identify(userId: $userId, properties: $properties)")
    }

    /**
     * Identify a group for the current user.
     *
     * [groupId] Unique value identifying the group.
     * [properties] Optional properties that provide additional context about the group.
     */
    fun group(groupId: String, properties: HashMap<String, Any>? = null) {
        Logcues.i("group(groupId: $groupId, properties: $properties)")
    }

    /**
     * Generate a unique Id for the current user when there is not a known identity to use in
     * the {@link identity(String, HashMap<String, Any>) identity} call. This will cause the SDK
     * to begin tracking activity and checking for qualified content.
     */
    fun anonymous(properties: HashMap<String, Any>? = null) {
        Logcues.i("anonymous(properties: $properties)")
    }

    /**
     * Clears out the current user in this session.
     * Can be used when the user logs out of your application.
     */
    fun reset() {
        Logcues.i("reset()")
    }

    /**
     * Track an action taken by a user.
     *
     * [name] Name of the event.
     * [properties] Optional properties that provide additional context about the event.
     */
    fun track(name: String, properties: HashMap<String, Any>? = null) {
        Logcues.i("track(name: $name, properties: $properties")
    }

    /**
     * Track an screen viewed by a user.
     *
     * [title] Name of the screen
     * [properties] Optional properties that provide additional context about the event.
     */
    fun screen(title: String, properties: HashMap<String, Any>? = null) {
        Logcues.i("screen(title: $title, properties: $properties)")
    }

    /**
     * Forces specific Appcues content to appear for the current user by passing in the [contentId].
     *
     * [contentId] ID of specific flow.
     */
    fun show(contentId: String) {
        Logcues.i("show(contentId: $contentId): Activity: ${activityMonitor.resumedActivity}")
    }

    /**
     * Register a trait that modifies an Experience.
     *
     * [trait] Trait to register
     */
    fun register(trait: Trait) {
        Logcues.i("register(trait: $trait)")
    }

    /**
     * Register an action that can be activated in an Experience.
     *
     * [action] Action to register.
     */
    fun register(action: Action) {
        Logcues.i("register(action: $action)")
    }

    /**
     * Enables automatic screen tracking. (Works for Activities)
     */
    fun trackScreen() {
        Logcues.i("trackScreen()")
    }

    /**
     * Set Appcues to start in Debug mode
     */
    fun debug() {
        Logcues.i("debug()")
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

            Logcues.setLoggingLevel(_loggingLevel)

            return Appcues(
                context = context.applicationContext,
                config = AppcuesConfig(
                    accountId = accountId,
                    applicationId = applicationId
                )
            )
        }
    }

    enum class LoggingLevel {
        NONE, BASIC
    }
}