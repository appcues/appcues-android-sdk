package com.appcues.session

import com.appcues.AppcuesConfig
import com.appcues.analytics.AnalyticIntent
import com.appcues.analytics.AnalyticIntent.Anonymous
import com.appcues.analytics.AnalyticIntent.Event
import com.appcues.analytics.AnalyticIntent.Identify
import com.appcues.analytics.AnalyticIntent.Screen
import com.appcues.analytics.AnalyticIntent.UpdateProfile
import com.appcues.analytics.AnalyticsEvent.ExperienceStarted
import com.appcues.analytics.AnalyticsEvent.SessionStarted
import com.appcues.analytics.Session
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
    private var lastIntentAt: Date? = null
    private var currentScreen: String? = null
    private var previousScreen: String? = null
    private var sessionPageviews: Int = 0
    private var sessionRandomId: Int = 0
    private var latestUserProperties: Map<String, Any> = mapOf()

    override suspend fun getSession(intent: AnalyticIntent?, onSessionStarted: (suspend () -> Unit)?): Session? {
        // the order of conditions here is important
        return when (intent) {
            // check if the incoming intent is Anonymous or Identify, we force start a new session
            is Anonymous -> getAnonymousSession(intent, onSessionStarted)
            is Identify -> getIdentifySession(intent, onSessionStarted)
            // for any other intent types we check if session is started already
            // if not we try to start a session based on stored information
            else -> getValidSession(intent) ?: startSession(intent, onSessionStarted)
        }
    }

    private suspend fun updateSession(intent: AnalyticIntent?) {
        if (intent != null) {
            lastIntentAt = Date()
        }

        when (intent) {
            is Anonymous -> {
                latestUserProperties = hashMapOf()
            }
            is Identify -> {
                latestUserProperties = intent.properties.sanitized()
            }
            is UpdateProfile -> {
                latestUserProperties = intent.properties.sanitized()
            }
            is Screen -> {
                previousScreen = currentScreen
                currentScreen = intent.title
                sessionPageviews += 1
            }
            is Event -> when (intent.name) {
                SessionStarted.eventName -> {
                    // special handling for session start events
                    sessionPageviews = 0
                    sessionRandomId = sessionRandomizer.get()
                    currentScreen = null
                    previousScreen = null
                }
                ExperienceStarted.eventName -> {
                    sessionLocalSource.setLastContentShownAt(Date())
                }
            }
            else -> Unit
        }
    }

    private fun Map<String, Any>?.sanitized(): Map<String, Any> {
        return mutableMapOf<String, Any>().apply {
            putAll(this@sanitized ?: hashMapOf())
            remove(KEY_USER_SIGNATURE)
        }
    }

    private suspend fun getIdentifySession(intent: Identify, onSessionStarted: (suspend () -> Unit)?): Session? {
        val session = getValidSession(intent)
        // only start anonymousSession if new Id is different from what we already have
        return if (session == null || intent.userId != session.userId) {
            sessionLocalSource.setUserId(intent.userId)
            sessionLocalSource.isAnonymous(false)
            sessionLocalSource.setUserSignature(intent.properties?.get(KEY_USER_SIGNATURE) as? String)
            return startSession(intent, onSessionStarted)
        } else session
    }

    private suspend fun getAnonymousSession(intent: Anonymous, onSessionStarted: (suspend () -> Unit)?): Session? {
        getAnonymousUserId().also { anonymousId ->
            val session = getValidSession(intent)
            // only start anonymousSession if new Id is different from what we already have
            return if (session == null || anonymousId != session.userId) {
                sessionLocalSource.setUserId(anonymousId)
                sessionLocalSource.isAnonymous(true)
                sessionLocalSource.setUserSignature(null)
                startSession(intent, onSessionStarted)
            } else session
        }
    }

    private suspend fun getAnonymousUserId(): String {
        val anonymousId = config.anonymousIdFactory?.invoke() ?: sessionLocalSource.getDeviceId()
        return "anon:$anonymousId"
    }

    // validate existing session and update lastActivityAt in case current session is valid
    private suspend fun getValidSession(intent: AnalyticIntent?): Session? {
        val userId = sessionLocalSource.getUserId()
        val sessionId = sessionId
        val isExpired = lastIntentAt
            ?.let { TimeUnit.MILLISECONDS.toSeconds(Date().time - it.time) >= config.sessionTimeout }
            ?: false

        return if (sessionId != null && userId != null && !isExpired) {
            updateAndGetSession(intent, userId, sessionId)
        } else null
    }

    private suspend fun updateAndGetSession(intent: AnalyticIntent?, userId: String, sessionId: UUID): Session {
        updateSession(intent)

        return Session(
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

    // start session based on previous session info (userId)
    private suspend fun startSession(intent: AnalyticIntent?, onSessionStarted: (suspend () -> Unit)?): Session? {
        val userId = sessionLocalSource.getUserId()
        return if (userId != null) {
            val newSessionId = UUID.randomUUID()
            sessionId = newSessionId
            lastIntentAt = Date()
            currentScreen = null
            previousScreen = null
            sessionPageviews = 0
            sessionRandomId = 0
            latestUserProperties = mapOf()

            onSessionStarted?.invoke()
            updateAndGetSession(intent, userId, newSessionId)
        } else null
    }

    override suspend fun reset() {
        sessionId = null
        lastIntentAt = null
        currentScreen = null
        previousScreen = null
        sessionPageviews = 0
        sessionRandomId = 0
        latestUserProperties = mapOf()

        sessionLocalSource.reset()
    }
}
