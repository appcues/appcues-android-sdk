package com.appcues

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.appcues.action.ActionRegistry
import com.appcues.action.ExperienceAction
import com.appcues.analytics.ActivityScreenTracking
import com.appcues.analytics.AnalyticsTracker
import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.di.AppcuesKoinContext
import com.appcues.logging.Logcues
import com.appcues.trait.ExperienceTrait
import com.appcues.trait.TraitRegistry
import com.appcues.ui.ExperienceRenderer
import org.koin.core.scope.Scope

fun Appcues(
    context: Context,
    accountId: String,
    applicationId: String,
    config: (AppcuesConfig.() -> Unit)? = null,
): Appcues =
    // This creates the Koin Scope and initializes the Appcues instance within, then returns the Appcues instance
    // ready to go with the necessary dependency configuration in its scope.
    AppcuesKoinContext.createAppcuesScope(context, AppcuesConfig(accountId, applicationId).apply { config?.invoke(this) }).get()

class Appcues internal constructor(koinScope: Scope) {

    private val config by koinScope.inject<AppcuesConfig>()
    private val experienceRenderer by koinScope.inject<ExperienceRenderer>()
    private val logcues by koinScope.inject<Logcues>()
    private val actionRegistry by koinScope.inject<ActionRegistry>()
    private val traitRegistry by koinScope.inject<TraitRegistry>()
    private val analyticsTracker by koinScope.inject<AnalyticsTracker>()
    internal val storage by koinScope.inject<Storage>()
    private val sessionMonitor by koinScope.inject<SessionMonitor>()
    private val activityScreenTracking by koinScope.inject<ActivityScreenTracking>()
    private val deeplinkHandler by koinScope.inject<DeeplinkHandler>()
    private val debuggerManager by koinScope.inject<AppcuesDebuggerManager>()

    /**
     * Set the listener to be notified about the display of Experience content.
     */
    var experienceListener: ExperienceListener? by config::experienceListener

    /**
     * Set the interceptor for additional control over SDK runtime behaviors.
     */
    var interceptor: AppcuesInterceptor? by config::interceptor

    init {
        sessionMonitor.start()

        logcues.info("Appcues SDK $version initialized")
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
    fun identify(userId: String, properties: Map<String, Any>? = null) {
        identify(false, userId, properties)
    }

    /**
     * Identify a group for the current user.
     *
     * [groupId] Unique value identifying the group.
     * [properties] Optional properties that provide additional context about the group.
     */
    fun group(groupId: String?, properties: Map<String, Any>? = null) {
        storage.groupId = groupId

        (if (groupId.isNullOrEmpty()) null else properties).also {
            analyticsTracker.group(it)
        }
    }

    /**
     * Generate a unique Id for the current user when there is not a known identity to use in
     * the {@link identity(String, Map<String, Any>) identity} call. This will cause the SDK
     * to begin tracking activity and checking for qualified content.
     */
    fun anonymous(properties: Map<String, Any>? = null) {
        // use the device ID as the default anonymous user ID, unless an override for generating
        // anonymous user IDs is supplied in the config builder
        val anonymousId = config.anonymousIdFactory?.invoke() ?: storage.deviceId
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
    fun track(name: String, properties: Map<String, Any>? = null) {
        analyticsTracker.track(name, properties)
    }

    /**
     * Track an screen viewed by a user.
     *
     * [title] Name of the screen
     * [properties] Optional properties that provide additional context about the event.
     */
    fun screen(title: String, properties: Map<String, Any>? = null) {
        analyticsTracker.screen(title, properties)
    }

    /**
     * Forces specific Appcues experience to appear for the current user by passing in the [experienceId].
     *
     * [experienceId] ID of the experience.
     *
     * * @return true if experience content was able to be shown, false if not.
     */
    suspend fun show(experienceId: String): Boolean {
        return experienceRenderer.show(experienceId)
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
    fun registerTrait(type: String, traitFactory: (config: Map<String, Any>?) -> ExperienceTrait) {
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
    fun registerAction(type: String, actionFactory: (config: Map<String, Any>?) -> ExperienceAction) {
        actionRegistry.register(type, actionFactory)
    }

    /**
     * Enables automatic screen tracking for Activities.
     */
    fun trackScreens() {
        activityScreenTracking.trackScreens()
    }

    /**
     * Set Appcues to start in Debug mode
     */
    fun debug(activity: Activity) {
        debuggerManager.start(activity)
    }

    /**
     * Signals to Appcues that this instance should stop all on going jobs
     *
     * Should be called before losing reference to the instance to ensure internal cleanup.
     */
    fun stop() {
        debuggerManager.stop()
        activityScreenTracking.stop()
        experienceRenderer.stop()
    }

    /**
     * Notify Appcues of a new Intent to check for deep link content.  This should be used to pass along Intents
     * that may be using the custom Appcues scheme, for things like previewing experiences.
     *
     * [activity] the current activity handling the intent
     * [intent] the Intent that the Appcues SDK should check for deep link content.
     *
     * @return true if a deep link was handled by the Appcues SDK, false if not - meaning the host application should process.
     */
    fun onNewIntent(activity: Activity, intent: Intent?): Boolean =
        deeplinkHandler.handle(activity, intent)

    private fun identify(isAnonymous: Boolean, userId: String, properties: Map<String, Any>?) {
        if (userId.isEmpty()) {
            logcues.error("Invalid userId - empty string")
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
}
