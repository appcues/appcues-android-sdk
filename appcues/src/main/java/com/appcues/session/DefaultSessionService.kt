package com.appcues.session

import android.os.Build.VERSION
import com.appcues.AppcuesConfig
import com.appcues.BuildConfig
import com.appcues.R
import com.appcues.analytics.AnalyticsEvent
import com.appcues.analytics.AnalyticsIntent
import com.appcues.analytics.AnalyticsIntent.Anonymous
import com.appcues.analytics.AnalyticsIntent.Event
import com.appcues.analytics.AnalyticsIntent.Identify
import com.appcues.analytics.AnalyticsIntent.UpdateGroup
import com.appcues.analytics.AnalyticsIntent.UpdateProfile
import com.appcues.analytics.SessionRandomizer
import com.appcues.analytics.SessionService
import com.appcues.data.session.PrefSessionLocalSource
import com.appcues.util.ContextResources
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

internal class DefaultSessionService(
    private val config: AppcuesConfig,
    private val sessionLocalSource: PrefSessionLocalSource,
    private val contextResources: ContextResources,
    private val sessionRandomizer: SessionRandomizer,
) : SessionService {

    private var sessionId: UUID? = null
    private var lastActivityAt: Date? = null
    private var currentScreen: String? = null
    private var previousScreen: String? = null
    private var sessionPageviews: Int = 0
    private var sessionRandomId: Int = 0
    private var sessionLatestUserProperties: Map<String, Any> = mapOf()

    private val contextProperties = hashMapOf<String, Any>(
        "app_id" to config.applicationId,
        "app_version" to contextResources.getAppVersion(),
    )

    private val applicationProperties = hashMapOf<String, Any>(
        "_appId" to config.applicationId,
        "_operatingSystem" to "android",
        "_bundlePackageId" to contextResources.getPackageName(),
        "_appName" to contextResources.getAppName(),
        "_appVersion" to contextResources.getAppVersion(),
        "_appBuild" to contextResources.getAppBuild().toString(),
        "_sdkVersion" to BuildConfig.SDK_VERSION,
        "_sdkName" to "appcues-android",
        "_osVersion" to "${VERSION.SDK_INT}",
        "_deviceType" to contextResources.getString(R.string.appcues_device_type),
        "_deviceModel" to contextResources.getDeviceName(),
    )

    private suspend fun getSessionProperties() = hashMapOf(
        "userId" to sessionLocalSource.getUserId(),
        "_isAnonymous" to sessionLocalSource.isAnonymous(),
        "_localId" to sessionLocalSource.getDeviceId(),
        "_updatedAt" to Date(),
        "_sessionId" to sessionId?.toString(),
        "_lastContentShownAt" to sessionLocalSource.getLastContentShownAt(),
        "_lastBrowserLanguage" to contextResources.getLanguage(),
        "_currentScreenTitle" to currentScreen,
        "_lastScreenTitle" to previousScreen,
        "_sessionPageviews" to sessionPageviews,
        "_sessionRandomizer" to sessionRandomId,
    ).filterValues { it != null }.mapValues { it.value as Any }

    private suspend fun getAutoProperties() = hashMapOf<String, Any>().apply {
        putAll(sessionLatestUserProperties)
        putAll(applicationProperties)
        // since its system property it can be null, we check before putting on the map
        System.getProperty("http.agent")?.let { put("_userAgent", it) }
        putAll(getSessionProperties())

        // additional props cannot overwrite values for existing internal prop keys
        config.additionalAutoProperties.forEach {
            if (containsKey(it.key).not()) {
                put(it.key, it.value)
            }
        }
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
        }
    }

    private suspend fun startAnonymousSession(): Boolean {
        val anonymousUserId = getAnonymousUserId()
        // only start anonymousSession if new Id is different from what we already have
        return if (anonymousUserId != sessionLocalSource.getUserId()) {
            sessionLocalSource.setUserId(getAnonymousUserId())
            sessionLocalSource.isAnonymous(true)
            sessionLocalSource.setUserSignature(null)
            true
        } else false
    }

    private suspend fun startSession(identify: Identify): Boolean {
        // only start anonymousSession if new Id is different from what we already have
        return if (identify.userId != sessionLocalSource.getUserId()) {
            sessionLocalSource.setUserId(identify.userId)
            sessionLocalSource.isAnonymous(false)
            sessionLocalSource.setUserSignature(identify.properties?.get("appcues:user_id_signature") as? String)
            true
        } else false
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

    private suspend fun getAnonymousUserId(): String {
        val anonymousId = config.anonymousIdFactory?.invoke() ?: sessionLocalSource.getDeviceId()
        return "anon:$anonymousId"
    }

    override suspend fun decorateIntent(intent: AnalyticsIntent): AnalyticsIntent {
        return when (intent) {
            Anonymous -> decorateIdentify(null, null)
            is UpdateProfile -> decorateIdentify(null, intent.properties)
            is Identify -> decorateIdentify(intent.userId, intent.properties)
            is Event -> decorateEvent(intent)
            // group does not require any additional decoration
            is UpdateGroup -> intent.also {
                sessionLocalSource.setGroupId(it.groupId)
            }
        }
    }

    private suspend fun decorateIdentify(userId: String?, properties: Map<String, Any>?): Identify {
        return Identify(
            userId = userId ?: (sessionLocalSource.getUserId() ?: getAnonymousUserId()),
            properties = (properties?.toMutableMap() ?: mutableMapOf())
                .also { sessionLatestUserProperties = it }
                .apply { putAll(getAutoProperties()) }
        )
    }

    suspend fun decorateEvent(intent: Event): Event {
        when (intent.name) {
            AnalyticsEvent.ScreenView.eventName -> {
                previousScreen = currentScreen
                currentScreen = intent.name
                sessionPageviews += 1
            }
            AnalyticsEvent.SessionStarted.eventName -> {
                // special handling for session start events
                sessionPageviews = 0
                sessionRandomId = sessionRandomizer.get()
                currentScreen = null
                previousScreen = null
            }
        }

        return intent.copy(
            name = intent.name,
            attributes = (intent.attributes?.toMutableMap() ?: mutableMapOf())
                .apply { this["_identify"] = getAutoProperties() },
            context = (intent.context?.toMutableMap() ?: mutableMapOf())
                .apply { putAll(contextProperties) }
        )
    }

    override suspend fun reset() {
        sessionId = null
        lastActivityAt = null
        currentScreen = null
        previousScreen = null
        sessionPageviews = 0
        sessionRandomId = 0
        sessionLatestUserProperties = mapOf()

        sessionLocalSource.reset()
    }
}
