package com.appcues

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.appcues.action.ActionRegistry
import com.appcues.action.ExperienceAction
import com.appcues.analytics.ActivityScreenTracking
import com.appcues.analytics.AnalyticsEvent
import com.appcues.analytics.AnalyticsTracker
import com.appcues.data.model.ExperienceTrigger
import com.appcues.data.model.RenderContext
import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.debugger.DebugMode.Debugger
import com.appcues.debugger.screencapture.AndroidTargetingStrategy
import com.appcues.di.Bootstrap
import com.appcues.di.scope.AppcuesScope
import com.appcues.di.scope.get
import com.appcues.di.scope.inject
import com.appcues.logging.LogcatDestination
import com.appcues.logging.Logcues
import com.appcues.push.PushOpenedProcessor
import com.appcues.trait.ExperienceTrait
import com.appcues.trait.ExperienceTraitLevel
import com.appcues.trait.TraitRegistry
import com.appcues.ui.AppcuesCustomComponentDirectory
import com.appcues.ui.ExperienceRenderer
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
): Appcues {
    // This creates the Scope and initializes the Appcues instance within, then returns the Appcues instance
    // ready to go with the necessary dependency configuration in its scope.
    val scope = Bootstrap
        .createScope(
            context = context,
            config = AppcuesConfig(accountId, applicationId).apply { config?.invoke(this) }
        )

    val appcues = scope.get<Appcues>()

    try {
        // this is necessary to ensure that not only when we get a new token but whenever we initialize the application appcues should know
        // whats the latest available token
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            // ensures we have token set
            if (it.isSuccessful) {
                val sessionMonitor = scope.get<SessionMonitor>()
                if (sessionMonitor.hasSession()) {
                    // can call setPushToken directly if we are already in a session, so the device_updated event is tracked
                    appcues.setPushToken(it.result)
                } else {
                    // store token on static pushToken that will be used on the next session start to track device props
                    val storage = scope.get<Storage>()
                    storage.pushToken = it.result
                }
            }
        }
    } catch (_: Exception) {
        // do nothing on any exception we may hit here as customer might not even have google messaging services setup
    }

    return appcues
}

/**
 * The main entry point for using Appcues functionality in your application - tracking
 * analytics and rendering experiences.
 */
public class Appcues internal constructor(internal val scope: AppcuesScope) {

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

        /**
         * Register custom view to be rendered if key matches incoming custom view in any flow
         */
        public fun registerCustomComponent(identifier: String, view: AppcuesCustomComponentView) {
            AppcuesCustomComponentDirectory.set(identifier, view)
        }

        internal var pushToken: String? = null
    }

    private val config by scope.inject<AppcuesConfig>()
    private val experienceRenderer by scope.inject<ExperienceRenderer>()
    private val logcues by scope.inject<Logcues>()
    private val actionRegistry by scope.inject<ActionRegistry>()
    private val traitRegistry by scope.inject<TraitRegistry>()
    private val analyticsTracker by scope.inject<AnalyticsTracker>()
    private val storage by scope.inject<Storage>()
    private val sessionMonitor by scope.inject<SessionMonitor>()
    private val activityScreenTracking by scope.inject<ActivityScreenTracking>()
    private val deepLinkHandler by scope.inject<DeepLinkHandler>()
    private val debuggerManager by scope.inject<AppcuesDebuggerManager>()
    private val appcuesCoroutineScope by scope.inject<CoroutineScope>()
    private val analyticsPublisher by scope.inject<AnalyticsPublisher>()
    private val pushOpenedProcessor by scope.inject<PushOpenedProcessor>()

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
        logcues.info("Appcues SDK $version initialized")

        appcuesCoroutineScope.launch {
            analyticsTracker.analyticsFlow.collect {
                @Suppress("TooGenericExceptionCaught")
                try {
                    analyticsPublisher.publish(analyticsListener, it)
                } catch (ex: Exception) {
                    // ignore any exception that occurs in client app code that is observing analytics,
                    // so we always continue collecting on the analyticsFlow
                    logcues.error(ex)
                }
            }
        }

        scope.get<LogcatDestination>().init()
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
     * Clears out the current user in this session.
     * Can be used when the user logs out of your application.
     */
    public fun reset() {
        analyticsTracker.track(
            name = AnalyticsEvent.DeviceUnregistered.eventName,
            properties = mapOf("reason" to "sdk_reset"),
            isInternal = true,
            interactive = false
        )
        // flush any pending analytics for the previous user, prior to reset
        analyticsTracker.flushPendingActivity()

        sessionMonitor.reset()
        storage.userId = ""
        storage.userSignature = null
        storage.isAnonymous = true
        storage.groupId = null

        debuggerManager.reset()

        appcuesCoroutineScope.launch {
            // stopping running experiences runs async, as it may require dismissal of UI.
            experienceRenderer.resetAll()
        }
    }

    /**
     * Track an action taken by a user.
     *
     * @param name Name of the event, cannot be empty.
     * @param properties Optional properties that provide additional context about the event.
     */
    public fun track(name: String, properties: Map<String, Any>? = null) {
        if (name.isEmpty()) {
            logcues.error("Invalid event name - empty string")
            return
        }

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
        return experienceRenderer.show(experienceId, ExperienceTrigger.ShowCall, mapOf())
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
    internal fun registerTrait(type: String, traitFactory: (config: Map<String, Any>?, level: ExperienceTraitLevel) -> ExperienceTrait) {
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
    internal fun registerAction(type: String, actionFactory: (config: Map<String, Any>?) -> ExperienceAction) {
        actionRegistry.register(type, actionFactory)
    }

    /**
     * Register an AppcuesFrameView for a given frameId.
     *
     * @param frameId unique string set in builder that is used to identify this AppcuesFrameView with qualified experiences
     * @param frame frame used to inflate the embedded experience
     */
    public fun registerEmbed(frameId: String, frame: AppcuesFrameView) {
        appcuesCoroutineScope.launch {
            experienceRenderer.start(frame, RenderContext.Embed(frameId))
        }
    }

    /**
     *  Provide the Firebase Cloud Messaging (FCM) device token to Appcues.
     *
     *  @param token A globally unique token that identifies this device to FCM.
     */
    public fun setPushToken(token: String?) {
        if (token != storage.pushToken) {
            storage.pushToken = token

            analyticsTracker.track(AnalyticsEvent.DeviceUpdated.eventName, properties = null, interactive = true, isInternal = true)
        }
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
        debuggerManager.start(activity, Debugger)
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
        appcuesCoroutineScope.launch {
            // stopping running experiences runs async, as it may require dismissal of UI.
            experienceRenderer.resetAll()
        }
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
        if (userChanged) {
            reset()
        }
        storage.userId = userId
        storage.isAnonymous = isAnonymous
        storage.userSignature = mutableProperties?.remove("appcues:user_id_signature") as? String
        analyticsTracker.identify(mutableProperties)
        if (!userChanged) {
            // track a device update on any re-identify of the same user as well, since it will not trigger a new
            // session but this is a way to force an update of any device props that may have changed outside of the SDK
            // i.e. push permission.
            // this is interactive=true so it gets batched together with the identify in a single request
            analyticsTracker.track(AnalyticsEvent.DeviceUpdated.eventName, properties = null, interactive = true, isInternal = true)
        }

        // whenever user identifies we check to see if there is a pending push open action matching the userId,
        // in case there is we run it.
        appcuesCoroutineScope.launch {
            pushOpenedProcessor.processDeferred(userId)
        }
    }
}
