package com.appcues.analytics

import com.appcues.AppcuesCoroutineScope
import com.appcues.analytics.AnalyticsEvent.ScreenView
import com.appcues.analytics.AnalyticsIntent.Anonymous
import com.appcues.analytics.AnalyticsIntent.Event
import com.appcues.analytics.AnalyticsIntent.Identify
import com.appcues.analytics.AnalyticsIntent.UpdateGroup
import com.appcues.analytics.AnalyticsIntent.UpdateProfile
import com.appcues.analytics.AnalyticsIntentQueue.IntentProcessor
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperienceTrigger.Qualification
import com.appcues.data.model.QualificationResult
import com.appcues.monitor.ApplicationMonitor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.Date

internal interface QualificationService {

    // will merge and hit the backend to see if it qualifies, returning QualificationResult
    suspend fun qualify(intents: List<AnalyticsIntent>): QualificationResult
}

internal interface RenderingService {

    // will attempt to show experiences by order
    // clearCache is mostly used when we know the qualification is from a screen_view
    suspend fun show(experiences: List<Experience>, clearCache: Boolean)

    suspend fun reset()
}

internal interface SessionService {

    // returns whether session is started or not
    suspend fun isSessionStarted(): Boolean

    // starts session (can start using incoming intent or existing stored information
    suspend fun startSession(intent: AnalyticsIntent): Boolean

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
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val intentQueue: AnalyticsIntentQueue,
    private val qualificationService: QualificationService,
    private val renderingService: RenderingService,
    private val sessionService: SessionService,
) : IntentProcessor, ApplicationMonitor.Listener {

    companion object {

        const val EVENT_ATTR_SCREEN_TITLE = "screenTitle"
        const val EVENT_CONTEXT_SCREEN_TITLE = "screen_title"
    }

    init {
        intentQueue.setProcessor(this)

        ApplicationMonitor.subscribe(this)
    }

    private val _analyticsFlow = MutableSharedFlow<AnalyticsIntent>(1)
    val analyticsFlow: SharedFlow<AnalyticsIntent>
        get() = _analyticsFlow

    fun anonymous() {
        intentQueue.flushAndProcess(Anonymous)
    }

    fun identify(userId: String, properties: Map<String, Any>? = null) {
        intentQueue.flushAndProcess(Identify(userId, properties))
    }

    fun updateProfile(properties: Map<String, Any>? = null, isInternal: Boolean) {
        intentQueue.queue(UpdateProfile(properties, isInternal))
    }

    fun updateGroup(groupId: String?, properties: Map<String, Any>? = null, isInternal: Boolean) {
        intentQueue.flushAndProcess(UpdateGroup(groupId, properties, isInternal))
    }

    fun track(name: String, properties: Map<String, Any>? = null, interactive: Boolean, isInternal: Boolean = false) {
        val intent = Event(
            name = name,
            attributes = properties,
            context = hashMapOf(),
            isInternal = isInternal
        )

        if (interactive) {
            intentQueue.queueThenFlush(intent)
        } else {
            intentQueue.queue(intent)
        }
    }

    fun screen(title: String, properties: Map<String, Any>? = null, isInternal: Boolean = false) {
        val intent = Event(
            // screen calls are really just a special type of event: "appcues:screen_view"
            name = ScreenView.eventName,
            attributes = (properties?.toMutableMap() ?: hashMapOf()).apply { put(EVENT_ATTR_SCREEN_TITLE, title) },
            context = hashMapOf(EVENT_CONTEXT_SCREEN_TITLE to title),
            isInternal = isInternal
        )

        intentQueue.queueThenFlush(intent)
    }

    fun reset() {
        intentQueue.flushAsync()

        appcuesCoroutineScope.launch {
            sessionService.reset()

            renderingService.reset()
        }
    }

    override fun process(intents: List<AnalyticsIntent>) {
        appcuesCoroutineScope.launch {
            val sessionIntents = arrayListOf<AnalyticsIntent>()
            intents.forEach {
                if (sessionService.isSessionStarted()) {
                    // if session is started it means we can use this intent
                    sessionIntents.add(it)
                } else if (sessionService.startSession(it)) {
                    // session is not started and user is not identified
                    // so we try to start session with existing information we have stored
                    // if true we can also use this intent
                    // + we add a session started event before this specific event
                    sessionIntents.add(Event(AnalyticsEvent.SessionStarted.eventName, null, null, true))
                    sessionIntents.add(it)
                }
            }

            sessionIntents
                // decorate intents
                .map { sessionService.decorateIntent(it) }
                // emit analytics flow
                .onEach { _analyticsFlow.emit(it) }
                // run intents through qualification service
                .let { qualificationService.qualify(intents) }
                // attempt to show experiences set clearCache if trigger reason is SCREEN_VIEW
                .run { renderingService.show(experiences, trigger.reason == Qualification.REASON_SCREEN_VIEW) }
        }
    }

    // when application stops
    override fun onApplicationStopped() {
        intentQueue.flushAsync()
    }
}
