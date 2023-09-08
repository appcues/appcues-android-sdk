package com.appcues.analytics

import com.appcues.AppcuesCoroutineScope
import com.appcues.analytics.AnalyticsEvent.ScreenView
import com.appcues.analytics.AnalyticsIntent.Anonymous
import com.appcues.analytics.AnalyticsIntent.Event
import com.appcues.analytics.AnalyticsIntent.Identify
import com.appcues.analytics.AnalyticsIntent.UpdateGroup
import com.appcues.analytics.AnalyticsIntent.UpdateProfile
import com.appcues.analytics.AnalyticsIntentQueue.IntentProcessor
import com.appcues.analytics.AnalyticsIntentQueue.QueueAction
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperienceTrigger.Qualification
import com.appcues.data.model.QualificationResult
import com.appcues.monitor.ApplicationMonitor
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.Date

internal interface QualificationService {

    // will merge and hit the backend to see if it qualifies, returning QualificationResult
    suspend fun qualify(intents: List<AnalyticsIntent>): QualificationResult?
}

internal interface RenderingService {

    // will attempt to show experiences by order
    // clearCache is mostly used when we know the qualification is from a screen_view
    suspend fun show(experiences: List<Experience>, clearCache: Boolean)

    suspend fun reset()
}

internal interface SessionService {

    suspend fun checkSession(intent: AnalyticsIntent, onSessionStarted: suspend () -> Unit): Boolean

    // decorates incoming intent and returns a new intent with session/auto props information
    suspend fun decorateIntent(intent: AnalyticsIntent): AnalyticsIntent

    suspend fun reset()
}

// new model for Intents available for Analytics
internal sealed class AnalyticsIntent(open val isInternal: Boolean, val timestamp: Date = Date()) {

    object Anonymous : AnalyticsIntent(false)

    data class Identify(
        val userId: String,
        val properties: Map<String, Any>?
    ) : AnalyticsIntent(false)

    data class UpdateProfile(
        val properties: Map<String, Any>?,
        override val isInternal: Boolean
    ) : AnalyticsIntent(isInternal)

    data class UpdateGroup(
        val groupId: String?,
        val properties: Map<String, Any>?,
        override val isInternal: Boolean
    ) : AnalyticsIntent(isInternal)

    data class Event(
        val name: String,
        val attributes: Map<String, Any>?,
        val context: Map<String, Any>?,
        override val isInternal: Boolean
    ) : AnalyticsIntent(isInternal)
}

internal class Analytics(
    private val coroutineScope: AppcuesCoroutineScope,
    private val intentQueue: AnalyticsIntentQueue,
    private val qualificationService: QualificationService,
    private val renderingService: RenderingService,
    private val sessionService: SessionService,
) : IntentProcessor, ApplicationMonitor.Listener {

    companion object {

        const val EVENT_ATTR_SCREEN_TITLE = "screenTitle"
        const val EVENT_CONTEXT_SCREEN_TITLE = "screen_title"
    }

    private val intentChannel = Channel<Pair<AnalyticsIntent, QueueAction>>(Channel.UNLIMITED)

    init {
        intentQueue.setProcessor(this)

        ApplicationMonitor.subscribe(this)

        coroutineScope.launch {
            for (element in intentChannel) {
                enqueueIntent(element.first, element.second)
            }
        }
    }

    private val _analyticsFlow = MutableSharedFlow<AnalyticsIntent>(1)
    val analyticsFlow: SharedFlow<AnalyticsIntent>
        get() = _analyticsFlow

    fun anonymous() = coroutineScope.launch {
        intentChannel.send(Anonymous to QueueAction.FLUSH_THEN_PROCESS)
    }

    fun identify(userId: String, properties: Map<String, Any>? = null) = coroutineScope.launch {
        val intent = Identify(userId, properties)

        intentChannel.send(intent to QueueAction.FLUSH_THEN_PROCESS)
    }

    fun updateProfile(properties: Map<String, Any>? = null, isInternal: Boolean) = coroutineScope.launch {
        val intent = UpdateProfile(properties, isInternal)

        intentChannel.send(intent to QueueAction.QUEUE)
    }

    fun updateGroup(groupId: String?, properties: Map<String, Any>? = null, isInternal: Boolean) = coroutineScope.launch {
        val intent = UpdateGroup(groupId, properties, isInternal)

        intentChannel.send(intent to QueueAction.FLUSH_THEN_PROCESS)
    }

    fun track(name: String, properties: Map<String, Any>? = null, interactive: Boolean, isInternal: Boolean = false) =
        coroutineScope.launch {
            val intent = Event(
                name = name,
                attributes = properties,
                context = hashMapOf(),
                isInternal = isInternal
            )

            val action = if (interactive) QueueAction.QUEUE_THEN_FLUSH else QueueAction.QUEUE

            intentChannel.send(intent to action)
        }

    fun screen(title: String, properties: Map<String, Any>? = null, isInternal: Boolean = false) = coroutineScope.launch {
        val intent = Event(
            // screen calls are really just a special type of event: "appcues:screen_view"
            name = ScreenView.eventName,
            attributes = (properties?.toMutableMap() ?: hashMapOf()).apply { put(EVENT_ATTR_SCREEN_TITLE, title) },
            context = hashMapOf(EVENT_CONTEXT_SCREEN_TITLE to title),
            isInternal = isInternal
        )

        intentChannel.send(intent to QueueAction.QUEUE_THEN_FLUSH)
    }

    fun reset() {
        coroutineScope.launch {
            intentQueue.flush()

            sessionService.reset()

            renderingService.reset()
        }
    }

    private suspend fun enqueueIntent(intent: AnalyticsIntent, action: QueueAction) {
        if (!sessionService.checkSession(intent, ::onNewSessionStarted)) return

        sessionService.decorateIntent(intent).also {
            _analyticsFlow.emit(it)

            intentQueue.queue(it, action)
        }
    }

    private suspend fun onNewSessionStarted() {
        intentQueue.flush()

        sessionService.reset()

        renderingService.reset()

        track(AnalyticsEvent.SessionStarted.eventName, interactive = true, isInternal = true)
    }

    override fun process(intents: List<AnalyticsIntent>) {
        coroutineScope.launch {
            // run intents through qualification service
            qualificationService.qualify(intents)
                // attempt to show experiences set clearCache if trigger reason is SCREEN_VIEW
                ?.run { renderingService.show(experiences, trigger.reason == Qualification.REASON_SCREEN_VIEW) }
        }
    }

    // when application stops
    override fun onApplicationStopped() {
        intentQueue.flush()
    }
}
