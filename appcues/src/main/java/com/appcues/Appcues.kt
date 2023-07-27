package com.appcues

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.appcues.action.ActionRegistry
import com.appcues.action.ExperienceAction
import com.appcues.analytics.ActivityScreenTracking
import com.appcues.analytics.AnalyticsTracker
import com.appcues.data.model.ExperienceTrigger
import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.debugger.DebugMode.Debugger
import com.appcues.debugger.screencapture.AndroidTargetingStrategy
import com.appcues.di.AppcuesKoinContext
import com.appcues.logging.Logcues
import com.appcues.trait.ExperienceTrait
import com.appcues.trait.ExperienceTraitLevel
import com.appcues.trait.TraitRegistry
import com.appcues.ui.ExperienceRenderer
import kotlinx.coroutines.launch
import org.koin.core.scope.Scope
import kotlin.DeprecationLevel.ERROR

/**
 * Construct and return an instance of the Appcues SDK.
 *
 * @param [context] The Android Context used by the host application.
 * @param [accountId] The Appcues Account ID found in Studio settings.
 * @param [applicationId] The Appcues Application ID found in Studio settings.
 * @param [config] Optional, additional settings on AppcuesConfig to use when initializing the SDK.
 */
public fun Appcues(
    context: Context,
    accountId: String,
    applicationId: String,
    config: (AppcuesConfig.() -> Unit)? = null,
): Appcues = AppcuesKoinContext
    // This creates the Koin Scope and initializes the Appcues instance within, then returns the Appcues instance
    // ready to go with the necessary dependency configuration in its scope.
    .createAppcuesScope(context, AppcuesConfig(accountId, applicationId).apply { config?.invoke(this) }).get()

/**
 * The main entry point for using Appcues functionality in your application - tracking
 * analytics and rendering experiences.
 */
public class Appcues internal constructor(koinScope: Scope) {

    public companion object {

        /**
         * The current version of Appcues SDK.
         */
        public val version: String
            get() = BuildConfig.SDK_VERSION

        /**
         * Defines an element targeting strategy to use to capture application UI elements
         * and use in target element experiences, such as anchored tooltips. The default implementation
         * provided by the SDK is based on Android View layout information.
         */
        public var elementTargeting: ElementTargetingStrategy = AndroidTargetingStrategy()
    }

    private val config by koinScope.inject<AppcuesConfig>()
    private val experienceRenderer by koinScope.inject<ExperienceRenderer>()
    private val logcues by koinScope.inject<Logcues>()
    private val actionRegistry by koinScope.inject<ActionRegistry>()
    private val traitRegistry by koinScope.inject<TraitRegistry>()
    private val analyticsTracker by koinScope.inject<AnalyticsTracker>()
    private val storage by koinScope.inject<Storage>()
    private val sessionMonitor by koinScope.inject<SessionMonitor>()
    private val activityScreenTracking by koinScope.inject<ActivityScreenTracking>()
    private val deepLinkHandler by koinScope.inject<DeepLinkHandler>()
    private val debuggerManager by koinScope.inject<AppcuesDebuggerManager>()
    private val appcuesCoroutineScope by koinScope.inject<AppcuesCoroutineScope>()
    private val analyticsPublisher by koinScope.inject<AnalyticsPublisher>()
    private val renderContextManager by koinScope.inject<RenderContextManager>()

    /**
     * Set the listener to be notified about the display of Experience content.
     */
    public var experienceListener: ExperienceListener? by config::experienceListener

    /**
     * Sets the listener to be notified about published analytics.
     */
    public var analyticsListener: AnalyticsListener? by config::analyticsListener

    /**
     * Set the interceptor for additional control over SDK runtime behaviors.
     */
    public var interceptor: AppcuesInterceptor? by config::interceptor

    /**
     * Sets the handler to use for link navigation.
     */
    public var navigationHandler: NavigationHandler? by config::navigationHandler

    init {
        sessionMonitor.start()

        logcues.info("Appcues SDK $version initialized")

        appcuesCoroutineScope.launch {
            analyticsTracker.analyticsFlow.collect {
                analyticsPublisher.publish(analyticsListener, it)
            }
        }
    }

    /**
     * The current version of Appcues SDK.
     */
    public val version: String
        get() = Appcues.version

    /**
     * Identify the user and determine if they should see Appcues content.
     *
     * To authenticate requests for this user, provide the Base64 encoded signature
     * for this user as a `String` value for key "appcues:user_id_signature", in the `properties` provided.
     *
     * @param userId Unique value identifying the user.
     * @param properties Optional properties that provide additional context about the user.
     */
    public fun identify(userId: String, properties: Map<String, Any>? = null) {
        identify(false, userId, properties)
    }

    /**
     * Identify a group for the current user.
     *
     * @param groupId Unique value identifying the group.
     * @param properties Optional properties that provide additional context about the group.
     */
    public fun group(groupId: String?, properties: Map<String, Any>? = null) {
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
    public fun anonymous() {
        // use the device ID as the default anonymous user ID, unless an override for generating
        // anonymous user IDs is supplied in the config builder
        val anonymousId = config.anonymousIdFactory?.invoke() ?: storage.deviceId
        identify(true, "anon:$anonymousId", null)
    }

    /**
     * This function has been removed. Calling the anonymous function with a properties parameter
     * is no longer supported. A call to `anonymous()` with no parameters should be used instead.
     */
    @Deprecated("properties are no longer supported for anonymous users.", level = ERROR)
    @Suppress("UnusedPrivateMember", "UNUSED_PARAMETER")
    public fun anonymous(properties: Map<String, Any>?) {
        // removed
    }

    /**
     * Clears out the current user in this session.
     * Can be used when the user logs out of your application.
     */
    public fun reset() {
        sessionMonitor.reset()
        storage.userId = ""
        storage.userSignature = null
        storage.isAnonymous = true
        storage.groupId = null
    }

    /**
     * Track an action taken by a user.
     *
     * @param name Name of the event.
     * @param properties Optional properties that provide additional context about the event.
     */
    public fun track(name: String, properties: Map<String, Any>? = null) {
        analyticsTracker.track(name, properties)
    }

    /**
     * Track an screen viewed by a user.
     *
     * @param title Name of the screen
     * @param properties Optional properties that provide additional context about the screen.
     */
    public fun screen(title: String, properties: Map<String, Any>? = null) {
        analyticsTracker.screen(title, properties)
    }

    /**
     * Renders the specified Appcues experience for the current user.
     *
     * @param experienceId ID of the experience.
     *
     * @return True if experience content was able to be shown, false if not.
     */
    public suspend fun show(experienceId: String): Boolean {
        return experienceRenderer.show(experienceId, ExperienceTrigger.ShowCall)
    }

    /**
     * Register a trait that can customize an Experience.
     *
     * @param type Type of the action that is sent by the experience. ex: "my-trait"
     * @param traitFactory Factory (lambda) responsible for creating the ExperienceTrait registered for given [type]
     *                     config is an optional map that will be mapped and used to invoke the custom trait with
     *                     level informs the trait which level in the experience rendering hierarchy it is being
     *                     applied - EXPERIENCE, GROUP, or STEP. This can optionally be used by a trait to apply
     *                     different rendering logic when applied to a group versus a single step, for example.
     * usage:
     * registerTrait("my-trait") { MyCustomExperienceTrait() }
     */
    public fun registerTrait(type: String, traitFactory: (config: Map<String, Any>?, level: ExperienceTraitLevel) -> ExperienceTrait) {
        traitRegistry.register(type, traitFactory)
    }

    /**
     * Register an action that can be activated in an Experience.
     *
     * @param type Type of the action that is sent by the experience. ex: "my-action"
     * @param actionFactory Factory (lambda) responsible for creating the ExperienceAction registered for given [type]
     *
     * usage:
     * registerAction("my-action") { MyCustomExperienceAction() }
     */
    public fun registerAction(type: String, actionFactory: (config: Map<String, Any>?) -> ExperienceAction) {
        actionRegistry.register(type, actionFactory)
    }

    /**
     * Register an AppcuesFrameView for a given frameId.
     *
     * @param frameId unique string set in builder that is used to identify this AppcuesFrameView with qualified experiences
     * @param frame frame used to inflate the embedded experience
     */
    public fun registerEmbed(frameId: String, frame: AppcuesFrameView) {
        renderContextManager.registerEmbedFrame(frameId, frame)
    }

    /**
     * Enables automatic screen tracking for Activities.
     */
    public fun trackScreens() {
        activityScreenTracking.trackScreens()
    }

    /**
     * Starts the Appcues Debugger over the specified Activity.
     *
     * @param activity The Activity to launch the debugger over.
     */
    public fun debug(activity: Activity) {
        debuggerManager.start(activity, Debugger(null))
    }

    /**
     * Signals to Appcues that this instance should stop all ongoing jobs.
     *
     * This method is only expected to be called if you are intending to fully remove
     * an Appcues SDK instance and create a new one. This is not normally an expected
     * behavior for most use cases of the SDK.
     */
    public fun stop() {
        debuggerManager.stop()
        activityScreenTracking.stop()
        experienceRenderer.stop()
    }

    /**
     * Notify Appcues of a new Intent to check for deep link content.  This should be used to pass along Intents
     * that may be using the custom Appcues scheme, for things like previewing experiences.
     *
     * @param activity Current activity handling the intent
     * @param intent Intent that the Appcues SDK should check for deep link content.
     *
     * @return True if a deep link was handled by the Appcues SDK, false if not - meaning the host application should process.
     */
    public fun onNewIntent(activity: Activity, intent: Intent?): Boolean =
        deepLinkHandler.handle(activity, intent)

    private fun identify(isAnonymous: Boolean, userId: String, properties: Map<String, Any>?) {
        if (userId.isEmpty()) {
            logcues.error("Invalid userId - empty string")
            return
        }

        val mutableProperties = properties?.toMutableMap()
        val userChanged = storage.userId != userId
        storage.userId = userId
        storage.isAnonymous = isAnonymous
        storage.userSignature = mutableProperties?.remove("appcues:user_id_signature") as? String
        if (userChanged) {
            // when the identified user changes from last known value, we must start a new session
            sessionMonitor.start()

            // group info is reset on new user
            storage.groupId = null
        }

        analyticsTracker.identify(mutableProperties)
    }
}
