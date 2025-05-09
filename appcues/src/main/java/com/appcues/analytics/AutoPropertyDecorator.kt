package com.appcues.analytics

import android.Manifest
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.core.app.ActivityCompat
import com.appcues.AppcuesConfig
import com.appcues.BuildConfig
import com.appcues.R
import com.appcues.SessionMonitor
import com.appcues.Storage
import com.appcues.data.remote.appcues.request.ActivityRequest
import com.appcues.data.remote.appcues.request.EventRequest
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.util.ContextWrapper
import java.util.Date
import java.util.TimeZone

internal class AutoPropertyDecorator(
    private val config: AppcuesConfig,
    private val contextWrapper: ContextWrapper,
    private val storage: Storage,
    private val sessionMonitor: SessionMonitor,
    private val sessionRandomizer: SessionRandomizer,
) {

    companion object {

        const val IDENTITY_PROPERTY = "_identity"
        const val DEVICE_PROPERTY = "_device"
        const val LAST_SEEN_AT = "_lastSeenAt"
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
        "_operatingSystem" to "Android",
        "_bundlePackageId" to contextWrapper.packageName,
        "_appName" to contextWrapper.getAppName(),
        "_appVersion" to contextWrapper.getAppVersion(),
        "_appBuild" to contextWrapper.getAppBuild().toString(),
        "_sdkVersion" to BuildConfig.SDK_VERSION,
        "_sdkName" to "appcues-android",
        "_osVersion" to "${VERSION.SDK_INT}",
        "_deviceType" to contextWrapper.getString(R.string.appcues_device_type),
        "_deviceModel" to contextWrapper.getDeviceName(),
        "_timezoneOffset" to TimeZone.getDefault().offsetMinutes(),
        "_timezoneCode" to TimeZone.getDefault().id,
    )

    private val pushProperties: Map<String, Any?>
        get() = hashMapOf(
            "_deviceId" to storage.deviceId,
            "_language" to contextWrapper.getLanguage(),
            "_pushToken" to storage.pushToken,
            "_pushEnabledBackground" to (storage.pushToken != null),
            "_pushEnabled" to (contextWrapper.isNotificationEnabled() && !storage.pushToken.isNullOrEmpty())
        )

    private val sessionProperties: Map<String, Any>
        get() = hashMapOf(
            "userId" to storage.userId,
            "_isAnonymous" to storage.isAnonymous,
            "_localId" to storage.deviceId,
            "_updatedAt" to Date(),
            // Last Seen At will deprecate _updatedAt which can't be entirely removed today since it's used for targeting
            LAST_SEEN_AT to Date(),
            "_sessionId" to sessionMonitor.sessionId?.toString(),
            "_lastContentShownAt" to storage.lastContentShownAt,
            "_lastBrowserLanguage" to contextWrapper.getLanguage(),
            "_currentScreenTitle" to currentScreen,
            "_lastScreenTitle" to previousScreen,
            "_sessionPageviews" to sessionPageviews,
            "_sessionRandomizer" to sessionRandomId,
            "_pushPrimerEligible" to getPushPrimerEligible()
        ).filterValues { it != null }.mapValues { it.value as Any }

    private fun getPushPrimerEligible(): Boolean {
        val activity = AppcuesActivityMonitor.activity ?: return false
        // VERSION guard because of Manifest.permission.POST_NOTIFICATIONS
        return if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            false
        }
    }

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

    val deviceProperties: Map<String, Any?>
        get() = hashMapOf<String, Any?>().apply {
            putAll(applicationProperties)
            putAll(pushProperties)
        }

    fun decorateTrack(event: EventRequest) = event.apply {
        when (event.name) {
            AnalyticsEvent.ScreenView.eventName -> {
                previousScreen = currentScreen
                currentScreen = attributes[ActivityRequestBuilder.SCREEN_TITLE_ATTRIBUTE]?.toString()
                sessionPageviews += 1
            }
            AnalyticsEvent.SessionStarted.eventName -> {
                attributes[DEVICE_PROPERTY] = deviceProperties

                sessionPageviews = 0
                sessionRandomId = sessionRandomizer.get()
                currentScreen = null
                previousScreen = null
            }
            AnalyticsEvent.DeviceUpdated.eventName -> {
                attributes[DEVICE_PROPERTY] = deviceProperties
            }
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
                it[LAST_SEEN_AT] = Date()
            }
        )
    }
}

private const val MILLISECONDS_PER_MINUTE = 60_000

private fun TimeZone.offsetMinutes() = getOffset(System.currentTimeMillis()) / MILLISECONDS_PER_MINUTE
