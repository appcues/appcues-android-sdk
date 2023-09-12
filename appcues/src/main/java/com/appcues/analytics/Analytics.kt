package com.appcues.analytics

import com.appcues.AnalyticType
import com.appcues.AppcuesCoroutineScope
import com.appcues.analytics.AnalyticsIntent.Anonymous
import com.appcues.analytics.AnalyticsIntent.Event
import com.appcues.analytics.AnalyticsIntent.Identify
import com.appcues.analytics.AnalyticsIntent.Screen
import com.appcues.analytics.AnalyticsIntent.UpdateGroup
import com.appcues.analytics.AnalyticsIntent.UpdateProfile
import com.appcues.analytics.AnalyticsQueue.QueueAction
import com.appcues.analytics.AnalyticsQueue.QueueProcessor
import com.appcues.analytics.RenderingService.EventTracker
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperienceTrigger.Qualification
import com.appcues.data.model.QualificationResult
import com.appcues.data.model.RenderContext
import com.appcues.monitor.ApplicationMonitor
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

internal interface QualificationService {

    // will merge and hit the backend to see if it qualifies, returning QualificationResult
    suspend fun qualify(intents: List<AnalyticsActivity>): QualificationResult?
}

internal interface RenderingService {

    // abstraction about who is responsible for tracking experience events
    interface EventTracker {

        fun trackEvent(name: String, properties: Map<String, Any>?, isInteractive: Boolean, isInternal: Boolean)
    }

    fun setEventTracker(eventTracker: EventTracker)

    // will attempt to show experiences by order
    // clearCache is mostly used when we know the qualification is from a screen_view
    suspend fun show(experiences: List<Experience>, clearCache: Boolean)

    sealed class ShowExperienceResult {
        object Success : ShowExperienceResult()
        object Skip : ShowExperienceResult()
        object NoSession : ShowExperienceResult()
        object ExperienceNotFound : ShowExperienceResult()
        data class NoRenderContext(val experience: Experience, val renderContext: RenderContext) : ShowExperienceResult()
        data class Error(val message: String) : ShowExperienceResult()
    }

    suspend fun show(experience: Experience): ShowExperienceResult

    sealed class PreviewExperienceResult {
        object Success : PreviewExperienceResult()
        object ExperienceNotFound : PreviewExperienceResult()
        data class PreviewDeferred(val experience: Experience, val frameId: String?) : PreviewExperienceResult()
        data class Error(val reason: String) : PreviewExperienceResult()
    }

    suspend fun preview(experience: Experience): PreviewExperienceResult

    suspend fun reset()
}

internal interface SessionService {

    // checks session or creates one based on existing information or incoming intent
    suspend fun checkSession(intent: AnalyticsIntent, onSessionStarted: suspend () -> Unit): Boolean

    // produce session properties or null if session is not present
    suspend fun getSessionProperties(): SessionProperties?

    suspend fun reset()
}

internal data class SessionProperties(
    val sessionId: UUID,
    val userId: String,
    val groupId: String?,
    val userSignature: String?,
    val latestUserProperties: Map<String, Any>,
    val properties: Map<String, Any>,
)

internal interface ActivityBuilder {

    suspend fun buildActivity(intent: AnalyticsIntent, sessionProperties: SessionProperties?): AnalyticsActivity?
}

// new model for Intents available for Analytics
internal sealed class AnalyticsIntent(open val isInternal: Boolean, open val properties: Map<String, Any>?, val timestamp: Date = Date()) {

    object Anonymous : AnalyticsIntent(false, null)

    data class Identify(
        val userId: String,
        override val properties: Map<String, Any>?
    ) : AnalyticsIntent(false, properties)

    data class UpdateProfile(
        override val isInternal: Boolean,
        override val properties: Map<String, Any>?
    ) : AnalyticsIntent(isInternal, properties)

    data class UpdateGroup(
        override val isInternal: Boolean,
        val groupId: String?,
        override val properties: Map<String, Any>?
    ) : AnalyticsIntent(isInternal, properties)

    data class Event(
        override val isInternal: Boolean,
        val name: String,
        override val properties: Map<String, Any>?
    ) : AnalyticsIntent(isInternal, properties)

    data class Screen(
        override val isInternal: Boolean,
        val title: String,
        override val properties: Map<String, Any>?
    ) : AnalyticsIntent(isInternal, properties)
}

internal data class AnalyticsActivity(
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
    private val queue: AnalyticsQueue,
    private val qualificationService: QualificationService,
    private val renderingService: RenderingService,
    private val sessionService: SessionService,
    private val activityBuilder: ActivityBuilder,
) : QueueProcessor, ApplicationMonitor.Listener, EventTracker {

    private val intentChannel = Channel<Pair<AnalyticsIntent, QueueAction>>(Channel.UNLIMITED)

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

    private val _activityFlow = MutableSharedFlow<AnalyticsActivity>(1)
    val activityFlow: SharedFlow<AnalyticsActivity>
        get() = _activityFlow

    fun anonymous() = coroutineScope.launch {
        intentChannel.send(Anonymous to QueueAction.FLUSH_THEN_PROCESS)
    }

    fun identify(userId: String, properties: Map<String, Any>? = null) = coroutineScope.launch {
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

    fun track(name: String, properties: Map<String, Any>? = null, interactive: Boolean, isInternal: Boolean = false) =
        coroutineScope.launch {
            val intent = Event(isInternal, name, properties)

            val action = if (interactive) QueueAction.QUEUE_THEN_FLUSH else QueueAction.QUEUE

            intentChannel.send(intent to action)
        }

    fun screen(title: String, properties: Map<String, Any>? = null, isInternal: Boolean = false) = coroutineScope.launch {
        val intent = Screen(isInternal, title, properties)

        intentChannel.send(intent to QueueAction.QUEUE_THEN_FLUSH)
    }

    fun reset() {
        coroutineScope.launch {
            queue.flush()

            sessionService.reset()

            renderingService.reset()
        }
    }

    private suspend fun enqueueIntent(intent: AnalyticsIntent, action: QueueAction) {
        if (!sessionService.checkSession(intent, ::onNewSessionStarted)) return

        activityBuilder.buildActivity(intent, sessionService.getSessionProperties())?.run {
            _activityFlow.emit(this)

            queue.enqueue(this, action)
        }
    }

    private suspend fun onNewSessionStarted() {
        queue.flush()

        sessionService.reset()

        renderingService.reset()

        track(AnalyticsEvent.SessionStarted.eventName, interactive = true, isInternal = true)
    }

    override fun process(items: List<AnalyticsActivity>) {
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
