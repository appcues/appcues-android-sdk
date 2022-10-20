package com.appcues.analytics

import android.os.Build.VERSION
import com.appcues.AppcuesConfig
import com.appcues.AppcuesCoroutineScope
import com.appcues.BuildConfig
import com.appcues.R
import com.appcues.SessionMonitor
import com.appcues.Storage
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.data.remote.request.EventRequest
import com.appcues.util.ContextResources
import kotlinx.coroutines.launch
import java.util.Date

internal class AutoPropertyDecorator(
    config: AppcuesConfig,
    appcuesCoroutineScope: AppcuesCoroutineScope,
    private val contextResources: ContextResources,
    private val storage: Storage,
    private val sessionMonitor: SessionMonitor,
    private val sessionRandomizer: SessionRandomizer,
) {

    companion object {
        const val IDENTITY_PROPERTY = "_identity"
        const val UPDATED_AT_PROPERTY = "_updatedAt"
    }

    private var userAgent: String? = null
    private var currentScreen: String? = null
    private var previousScreen: String? = null
    private var sessionPageviews = 0
    private var sessionRandomId: Int = 0

    private val contextProperties = hashMapOf<String, Any>(
        "app_id" to config.applicationId,
        "app_version" to contextResources.getAppVersion(),
    )

    private var applicationProperties = hashMapOf<String, Any>(
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

    private val sessionProperties: Map<String, Any>
        get() = hashMapOf(
            "userId" to storage.userId,
            "_isAnonymous" to storage.isAnonymous,
            "_localId" to storage.deviceId,
            UPDATED_AT_PROPERTY to Date().time,
            "_sessionId" to sessionMonitor.sessionId?.toString(),
            "_lastContentShownAt" to storage.lastContentShownAt?.time,
            "_lastBrowserLanguage" to contextResources.getLanguage(),
            "_currentScreenTitle" to currentScreen,
            "_lastScreenTitle" to previousScreen,
            "_sessionPageviews" to sessionPageviews,
            "_sessionRandomizer" to sessionRandomId,
        ).filterValues { it != null }.mapValues { it.value as Any }

    val autoProperties: Map<String, Any>
        get() = hashMapOf<String, Any>().apply {
            putAll(applicationProperties)
            // add userAgent if exists (userAgent is a mutable property loaded asynchronously) and can be null
            userAgent?.let { put("_userAgent", it) }
            putAll(sessionProperties)
        }

    init {
        appcuesCoroutineScope.launch {
            userAgent = contextResources.getUserAgent()
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
        profileUpdate = (activity.profileUpdate ?: hashMapOf()).apply {
            putAll(autoProperties)
        }
    )
}
