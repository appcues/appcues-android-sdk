package com.appcues.analytics

import com.appcues.AnalyticType
import com.appcues.AppcuesCoroutineScope
import com.appcues.AppcuesFrameView
import com.appcues.analytics.AnalyticIntent.Anonymous
import com.appcues.analytics.AnalyticIntent.Event
import com.appcues.analytics.AnalyticIntent.Identify
import com.appcues.analytics.AnalyticIntent.Screen
import com.appcues.analytics.AnalyticIntent.UpdateGroup
import com.appcues.analytics.AnalyticIntent.UpdateProfile
import com.appcues.analytics.AnalyticsQueue.QueueAction
import com.appcues.analytics.AnalyticsQueue.QueueProcessor
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperienceState
import com.appcues.data.model.ExperienceTrigger.Qualification
import com.appcues.data.model.QualificationResult
import com.appcues.data.model.RenderContext
import com.appcues.data.model.StepReference
import com.appcues.logging.Logcues
import com.appcues.monitor.ApplicationMonitor
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

internal interface QualificationService {

    // will merge and hit the backend to see if it qualifies, returning QualificationResult
    suspend fun qualify(intents: List<AnalyticActivity>): QualificationResult?
}

// abstraction about who is responsible for tracking experience events
internal interface EventTracker {

    fun trackEvent(name: String, properties: Map<String, Any>?, isInteractive: Boolean, isInternal: Boolean)
}

internal interface RenderingService {

    fun setEventTracker(eventTracker: EventTracker)

    // will attempt to show experiences by order
    // clearCache is mostly used when we know the qualification is from a screen_view
    suspend fun show(experiences: List<Experience>, clearCache: Boolean)

    sealed class ShowExperienceResult {
        object Success : ShowExperienceResult()
        object Skip : ShowExperienceResult()
        object NoSession : ShowExperienceResult()
        object ExperienceNotFound : ShowExperienceResult()
        data class RequestError(val message: String) : ShowExperienceResult()
        data class NoRenderContext(val experience: Experience, val renderContext: RenderContext) : ShowExperienceResult()
        data class RenderError(val experience: Experience, val message: String) : ShowExperienceResult()
    }

    suspend fun show(experience: Experience): ShowExperienceResult
    suspend fun show(renderContext: RenderContext, stepReference: StepReference)

    sealed class PreviewExperienceResult {
        object Success : PreviewExperienceResult()
        object ExperienceNotFound : PreviewExperienceResult()
        data class RequestError(val message: String) : PreviewExperienceResult()
        data class PreviewDeferred(val experience: Experience, val frameId: String?) : PreviewExperienceResult()
        data class RenderError(val experience: Experience, val message: String) : PreviewExperienceResult()
    }

    suspend fun preview(experience: Experience): PreviewExperienceResult

    suspend fun start(context: RenderContext, frame: AppcuesFrameView)

    suspend fun dismiss(renderContext: RenderContext, markComplete: Boolean, destroyed: Boolean)

    suspend fun reset()

    fun getExperienceState(renderContext: RenderContext): ExperienceState?
}

internal interface SessionService {

    // get valid session or attempt to start one based on incoming intent.
    // reports back to onSessionStarted if a new session started.
    suspend fun getSession(intent: AnalyticIntent?, onSessionStarted: (suspend () -> Unit)? = null): Session?

    suspend fun reset()
}

internal data class Session(
    val sessionId: UUID,
    val userId: String,
    val groupId: String?,
    val userSignature: String?,
    val latestUserProperties: Map<String, Any>,
    val properties: Map<String, Any>,
)

internal interface ActivityBuilder {

    // responsible for taking in intent and session and build into a proper AnalyticActivity
    suspend fun build(intent: AnalyticIntent, session: Session): AnalyticActivity
}

// new model for Intents available for Analytics
internal sealed class AnalyticIntent(open val isInternal: Boolean, open val properties: Map<String, Any>?, val timestamp: Date = Date()) {

    object Anonymous : AnalyticIntent(false, null)

    data class Identify(
        val userId: String,
        override val properties: Map<String, Any>?
    ) : AnalyticIntent(false, properties)

    data class UpdateProfile(
        override val isInternal: Boolean,
        override val properties: Map<String, Any>?
    ) : AnalyticIntent(isInternal, properties)

    data class UpdateGroup(
        override val isInternal: Boolean,
        val groupId: String?,
        override val properties: Map<String, Any>?
    ) : AnalyticIntent(isInternal, properties)

    data class Event(
        override val isInternal: Boolean,
        val name: String,
        override val properties: Map<String, Any>?
    ) : AnalyticIntent(isInternal, properties)

    data class Screen(
        override val isInternal: Boolean,
        val title: String,
        override val properties: Map<String, Any>?
    ) : AnalyticIntent(isInternal, properties)
}

internal data class AnalyticActivity(
    val type: AnalyticType,
    val isInternal: Boolean,
    val timestamp: Date,
    val sessionId: UUID,
    val userId: String,
    val groupId: String?,
    val profileProperties: Map<String, Any>?,
    val groupProperties: Map<String, Any>?,
    val eventName: String?,
    val eventAttributes: Map<String, Any>?,
    val eventContext: Map<String, Any>?,
    val userSignature: String?,
)

internal class Analytics(
    private val coroutineScope: AppcuesCoroutineScope,
    private val logcues: Logcues,
    private val queue: AnalyticsQueue,
    private val qualificationService: QualificationService,
    private val renderingService: RenderingService,
    private val sessionService: SessionService,
    private val activityBuilder: ActivityBuilder,
) : QueueProcessor, ApplicationMonitor.Listener, EventTracker {

    private val intentChannel = Channel<Pair<AnalyticIntent, QueueAction>>(Channel.UNLIMITED)

    init {
        ApplicationMonitor.subscribe(this)
        queue.setProcessor(this)
        renderingService.setEventTracker(this)

        coroutineScope.launch {
            for (element in intentChannel) {
                enqueueIntent(element.first, element.second)
            }
        }
    }

    private val _activityFlow = MutableSharedFlow<AnalyticActivity>(1)
    val activityFlow: SharedFlow<AnalyticActivity>
        get() = _activityFlow

    fun anonymous() = coroutineScope.launch {
        intentChannel.send(Anonymous to QueueAction.FLUSH_THEN_PROCESS)
    }

    fun identify(userId: String, properties: Map<String, Any>? = null) = coroutineScope.launch {
        if (userId.isEmpty()) {
            logcues.error("Invalid userId - empty string")
            return@launch
        }

        val intent = Identify(userId, properties)

        intentChannel.send(intent to QueueAction.FLUSH_THEN_PROCESS)
    }

    fun updateProfile(properties: Map<String, Any>? = null, isInternal: Boolean) = coroutineScope.launch {
        val intent = UpdateProfile(isInternal, properties)

        intentChannel.send(intent to QueueAction.QUEUE)
    }

    fun updateGroup(groupId: String?, properties: Map<String, Any>? = null, isInternal: Boolean) = coroutineScope.launch {
        val intent = UpdateGroup(isInternal, groupId, properties)

        intentChannel.send(intent to QueueAction.FLUSH_THEN_PROCESS)
    }

    fun track(name: String, properties: Map<String, Any>? = null, interactive: Boolean = true, isInternal: Boolean = false) =
        coroutineScope.launch {
            val intent = Event(isInternal, name, properties)

            val action = if (interactive) QueueAction.QUEUE_THEN_FLUSH else QueueAction.QUEUE

            intentChannel.send(intent to action)
        }

    fun screen(title: String, properties: Map<String, Any>? = null, isInternal: Boolean = false) = coroutineScope.launch {
        val intent = Screen(isInternal, title, properties)

        intentChannel.send(intent to QueueAction.QUEUE_THEN_FLUSH)
    }

    suspend fun reset() {
        queue.flush()

        sessionService.reset()

        renderingService.reset()
    }

    private suspend fun enqueueIntent(intent: AnalyticIntent, action: QueueAction) {
        val session = sessionService.getSession(intent, ::onSessionStarted) ?: return

        val activity = activityBuilder.build(intent, session)

        _activityFlow.emit(activity)

        queue.enqueue(activity, action)
    }

    private suspend fun onSessionStarted() {
        queue.flush()

        renderingService.reset()

        track(AnalyticsEvent.SessionStarted.eventName, interactive = true, isInternal = true)
    }

    override fun process(items: List<AnalyticActivity>) {
        coroutineScope.launch {
            // run intents through qualification service
            qualificationService.qualify(items)
                // attempt to show experiences set clearCache if trigger reason is SCREEN_VIEW
                ?.run {
                    val clearCache = trigger.reason == Qualification.REASON_SCREEN_VIEW
                    renderingService.show(experiences, clearCache)
                }
        }
    }

    override fun trackEvent(name: String, properties: Map<String, Any>?, isInteractive: Boolean, isInternal: Boolean) {
        track(name, properties, isInteractive, isInternal)
    }

    // when application stops
    override fun onApplicationStopped() {
        queue.flush()
    }
}
