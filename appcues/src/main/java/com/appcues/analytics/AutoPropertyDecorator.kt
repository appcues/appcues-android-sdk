package com.appcues.analytics

import android.os.Build.VERSION
import com.appcues.AppcuesConfig
import com.appcues.BuildConfig
import com.appcues.R
import com.appcues.SessionMonitor
import com.appcues.Storage
import com.appcues.data.remote.appcues.request.ActivityRequest
import com.appcues.data.remote.appcues.request.EventRequest
import com.appcues.util.ContextWrapper
import java.util.Date

internal class AutoPropertyDecorator(
    private val config: AppcuesConfig,
    private val contextWrapper: ContextWrapper,
    private val storage: Storage,
    private val sessionMonitor: SessionMonitor,
    private val sessionRandomizer: SessionRandomizer,
) {

    companion object {

        const val IDENTITY_PROPERTY = "_identity"
        const val UPDATED_AT_PROPERTY = "_updatedAt"
    }

    private var currentScreen: String? = null
    private var previousScreen: String? = null
    private var sessionPageviews = 0
    private var sessionRandomId: Int = 0
    private var sessionLatestUserProperties: Map<String, Any> = mapOf()

    private val contextProperties = hashMapOf<String, Any>(
        "app_id" to config.applicationId,
        "app_version" to contextWrapper.getAppVersion(),
    )

    private var applicationProperties = hashMapOf<String, Any>(
        "_appId" to config.applicationId,
        "_operatingSystem" to "android",
        "_bundlePackageId" to contextWrapper.getPackageName(),
        "_appName" to contextWrapper.getAppName(),
        "_appVersion" to contextWrapper.getAppVersion(),
        "_appBuild" to contextWrapper.getAppBuild().toString(),
        "_sdkVersion" to BuildConfig.SDK_VERSION,
        "_sdkName" to "appcues-android",
        "_osVersion" to "${VERSION.SDK_INT}",
        "_deviceType" to contextWrapper.getString(R.string.appcues_device_type),
        "_deviceModel" to contextWrapper.getDeviceName(),
    )

    private val sessionProperties: Map<String, Any>
        get() = hashMapOf(
            "userId" to storage.userId,
            "_isAnonymous" to storage.isAnonymous,
            "_localId" to storage.deviceId,
            UPDATED_AT_PROPERTY to Date(),
            "_sessionId" to sessionMonitor.sessionId?.toString(),
            "_lastContentShownAt" to storage.lastContentShownAt,
            "_lastBrowserLanguage" to contextWrapper.getLanguage(),
            "_currentScreenTitle" to currentScreen,
            "_lastScreenTitle" to previousScreen,
            "_sessionPageviews" to sessionPageviews,
            "_sessionRandomizer" to sessionRandomId,
        ).filterValues { it != null }.mapValues { it.value as Any }

    val autoProperties: Map<String, Any>
        get() = hashMapOf<String, Any>().apply {
            putAll(sessionLatestUserProperties)
            putAll(applicationProperties)
            putAll(sessionProperties)
            config.additionalAutoProperties.forEach {
                // additional props cannot overwrite values for existing internal prop keys
                // putIfAbsent exists, but only on API 24+
                if (containsKey(it.key).not()) {
                    put(it.key, it.value)
                }
            }
        }

    fun decorateTrack(event: EventRequest) = event.apply {
        if (event.name == AnalyticsEvent.ScreenView.eventName) {
            // special handling for screen_view events
            previousScreen = currentScreen
            currentScreen = attributes[ActivityRequestBuilder.SCREEN_TITLE_ATTRIBUTE]?.toString()
            sessionPageviews += 1
        } else if (event.name == AnalyticsEvent.SessionStarted.eventName) {
            // special handling for session start events
            sessionPageviews = 0
            sessionRandomId = sessionRandomizer.get()
            currentScreen = null
            previousScreen = null
        }

        attributes[IDENTITY_PROPERTY] = autoProperties
        context.putAll(contextProperties)
    }

    fun decorateIdentify(activity: ActivityRequest) = activity.copy(
        profileUpdate = (activity.profileUpdate ?: hashMapOf()).also {
            sessionLatestUserProperties = it
        }.apply {
            putAll(autoProperties)
        }
    )

    fun decorateGroup(activity: ActivityRequest): ActivityRequest {
        // only apply group auto props when the user is associated with a non-null group
        if (activity.groupId == null) return activity

        return activity.copy(
            groupUpdate = (activity.groupUpdate ?: hashMapOf()).also {
                it["_lastSeenAt"] = Date()
            }
        )
    }
}
