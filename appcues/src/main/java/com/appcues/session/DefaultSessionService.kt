package com.appcues.session

import com.appcues.AppcuesConfig
import com.appcues.analytics.AnalyticsEvent
import com.appcues.analytics.AnalyticsIntent
import com.appcues.analytics.AnalyticsIntent.Anonymous
import com.appcues.analytics.AnalyticsIntent.Event
import com.appcues.analytics.AnalyticsIntent.Identify
import com.appcues.analytics.AnalyticsIntent.Screen
import com.appcues.analytics.AnalyticsIntent.UpdateProfile
import com.appcues.analytics.SessionProperties
import com.appcues.analytics.SessionRandomizer
import com.appcues.analytics.SessionService
import com.appcues.data.session.PrefSessionLocalSource
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

internal class DefaultSessionService(
    private val config: AppcuesConfig,
    private val sessionLocalSource: PrefSessionLocalSource,
    private val sessionRandomizer: SessionRandomizer,
) : SessionService {

    companion object {

        private const val KEY_USER_SIGNATURE = "appcues:user_id_signature"
    }

    private var sessionId: UUID? = null
    private var lastActivityAt: Date? = null
    private var currentScreen: String? = null
    private var previousScreen: String? = null
    private var sessionPageviews: Int = 0
    private var sessionRandomId: Int = 0
    private var latestUserProperties: Map<String, Any> = mapOf()

    override suspend fun getSessionProperties(): SessionProperties? {
        val userId = sessionLocalSource.getUserId() ?: return null
        val sessionId = this.sessionId ?: return null

        return SessionProperties(
            sessionId = sessionId,
            userId = userId,
            groupId = sessionLocalSource.getGroupId(),
            userSignature = sessionLocalSource.getUserSignature(),
            latestUserProperties = latestUserProperties,
            properties = hashMapOf(
                "userId" to userId,
                "_sessionId" to sessionId,
                "_isAnonymous" to sessionLocalSource.isAnonymous(),
                "_localId" to sessionLocalSource.getDeviceId(),
                "_lastContentShownAt" to sessionLocalSource.getLastContentShownAt(),
                "_currentScreenTitle" to currentScreen,
                "_lastScreenTitle" to previousScreen,
                "_sessionPageviews" to sessionPageviews,
                "_sessionRandomizer" to sessionRandomId,
            ).filterValues { it != null }.mapValues { it.value as Any }
        )
    }

    override suspend fun checkSession(intent: AnalyticsIntent, onSessionStarted: suspend () -> Unit): Boolean {
        // the order of conditions here is important
        return when {
            // check if the incoming intent is Anonymous or Identify, we force start a new session
            intent is Anonymous -> startAnonymousSession().also { onSessionStarted() }
            intent is Identify -> startSession(intent).also { onSessionStarted() }
            // for any other intent types we check if session is started already
            isSessionStarted() -> true
            // if not we try to start a session based on stored information
            startSession() -> true.also { onSessionStarted() }
            // we could not start or check for existing session
            else -> false
        }.updateSession(intent)
    }

    private fun Boolean.updateSession(intent: AnalyticsIntent): Boolean {
        if (this) {
            when {
                intent is Anonymous -> {
                    latestUserProperties = hashMapOf()
                }
                intent is Identify -> {
                    latestUserProperties = intent.properties.sanitized()
                }
                intent is UpdateProfile -> {
                    latestUserProperties = intent.properties.sanitized()
                }
                intent is Screen -> {
                    previousScreen = currentScreen
                    currentScreen = intent.title
                    sessionPageviews += 1
                }
                intent is Event && intent.name == AnalyticsEvent.SessionStarted.eventName -> {
                    // special handling for session start events
                    sessionPageviews = 0
                    sessionRandomId = sessionRandomizer.get()
                    currentScreen = null
                    previousScreen = null
                }
            }
        }

        return this
    }

    private fun Map<String, Any>?.sanitized(): Map<String, Any> {
        return mutableMapOf<String, Any>().apply {
            putAll(this@sanitized ?: hashMapOf())
            remove(KEY_USER_SIGNATURE)
        }
    }

    private suspend fun startAnonymousSession(): Boolean {
        getAnonymousUserId().also { anonymousId ->
            // only start anonymousSession if new Id is different from what we already have
            if (anonymousId != sessionLocalSource.getUserId()) {
                sessionLocalSource.setUserId(anonymousId)
                sessionLocalSource.isAnonymous(true)
                sessionLocalSource.setUserSignature(null)
                return startSession()
            }
        }

        return false
    }

    private suspend fun getAnonymousUserId(): String {
        val anonymousId = config.anonymousIdFactory?.invoke() ?: sessionLocalSource.getDeviceId()
        return "anon:$anonymousId"
    }

    private suspend fun startSession(identify: Identify): Boolean {
        // only start anonymousSession if new Id is different from what we already have
        if (identify.userId != sessionLocalSource.getUserId()) {
            sessionLocalSource.setUserId(identify.userId)
            sessionLocalSource.isAnonymous(false)
            sessionLocalSource.setUserSignature(identify.properties?.get(KEY_USER_SIGNATURE) as? String)
            return startSession()
        }

        return false
    }

    // validate existing session and update lastActivityAt in case current session is valid
    private fun isSessionStarted(): Boolean {
        val isExpired = lastActivityAt
            ?.let { TimeUnit.MILLISECONDS.toSeconds(Date().time - it.time) >= config.sessionTimeout }
            ?: false

        val isSessionStarted = sessionId != null && !isExpired

        if (isSessionStarted) {
            lastActivityAt = Date()
        }

        return isSessionStarted
    }

    // start session based on previous session info (userId)
    private suspend fun startSession(): Boolean {
        return if (sessionLocalSource.getUserId() == null) {
            sessionId = UUID.randomUUID()
            lastActivityAt = Date()
            true
        } else false
    }

    override suspend fun reset() {
        sessionId = null
        lastActivityAt = null
        currentScreen = null
        previousScreen = null
        sessionPageviews = 0
        sessionRandomId = 0
        latestUserProperties = mapOf()

        sessionLocalSource.reset()
    }
}
