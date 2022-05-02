package com.appcues.analytics

import android.content.Context
import android.os.Build.MANUFACTURER
import android.os.Build.MODEL
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.webkit.WebView
import com.appcues.AppcuesConfig
import com.appcues.AppcuesCoroutineScope
import com.appcues.BuildConfig
import com.appcues.R
import com.appcues.SessionMonitor
import com.appcues.Storage
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.data.remote.request.EventRequest
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import kotlin.random.Random

internal class AutoPropertyDecorator(
    private val context: Context,
    config: AppcuesConfig,
    private val storage: Storage,
    private val sessionMonitor: SessionMonitor,
    appcuesCoroutineScope: AppcuesCoroutineScope,
) {
    companion object {
        // _sessionRandomizer is defined in the web SDK as: A random number between 1 and 100,
        // generated every time a user visits your site in a new browser window or tab.
        // It appears to be used for targeting a % of sessions as a sample.
        private const val SESSION_RANDOMIZER_LOWER_BOUND = 0
        private const val SESSION_RANDOMIZER_UPPER_BOUND = 100

        const val IDENTITY_PROPERTY = "_identity"
        const val UPDATED_AT_PROPERTY = "_updatedAt"
    }

    private var currentScreen: String? = null
    private var previousScreen: String? = null
    private var sessionPageviews = 0
    private var sessionRandomizer: Int? = 0

    private val contextProperties = hashMapOf<String, Any>(
        "app_id" to config.applicationId,
        "app_version" to context.getAppVersion(),
    )

    private var applicationProperties = hashMapOf<String, Any>(
        "_appId" to config.applicationId,
        "_operatingSystem" to "android",
        "_bundlePackageId" to context.packageName,
        "_appName" to context.getAppName(),
        "_appVersion" to context.getAppVersion(),
        "_appBuild" to context.getAppBuild().toString(),
        "_sdkVersion" to BuildConfig.SDK_VERSION,
        "_sdkName" to "appcues-android",
        "_osVersion" to "${VERSION.SDK_INT}",
        "_deviceType" to context.resources.getString(R.string.device_type),
        "_deviceModel" to getDeviceName(),
    )

    private val sessionProperties: Map<String, Any>
        get() = hashMapOf(
            "userId" to storage.userId,
            "_isAnonymous" to storage.isAnonymous,
            "_localId" to storage.deviceId,
            UPDATED_AT_PROPERTY to Date().time,
            "_sessionId" to sessionMonitor.sessionId?.toString(),
            "_lastContentShownAt" to storage.lastContentShownAt?.time,
            "_lastBrowserLanguage" to getCurrentLocale(context).language,
            "_currentScreenTitle" to currentScreen,
            "_lastScreenTitle" to previousScreen,
            "_sessionPageviews" to sessionPageviews,
            "_sessionRandomizer" to sessionRandomizer,
        ).filterValues { it != null }.mapValues { it.value as Any }

    val autoProperties: Map<String, Any>
        get() = hashMapOf<String, Any>().apply {
            putAll(applicationProperties)
            putAll(sessionProperties)
        }

    init {
        appcuesCoroutineScope.launch {
            // this must be on main thread
            applicationProperties["_userAgent"] = WebView(context).settings.userAgentString
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
            sessionRandomizer = Random.nextInt(SESSION_RANDOMIZER_LOWER_BOUND, SESSION_RANDOMIZER_UPPER_BOUND)
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

    private fun getDeviceName(): String {
        if (MODEL.lowercase().startsWith(MANUFACTURER.lowercase())) {
            return MODEL.capitalize(getCurrentLocale(context))
        }
        return MANUFACTURER.capitalize(getCurrentLocale(context)) + " " + MODEL
    }

    private fun getCurrentLocale(context: Context): Locale {
        return if (VERSION.SDK_INT >= VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
    }
}

fun Context.getAppVersion(): String = packageManager.getPackageInfo(packageName, 0).versionName
fun Context.getAppBuild() =
    if (VERSION.SDK_INT >= VERSION_CODES.P) {
        packageManager.getPackageInfo(packageName, 0).longVersionCode
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, 0).versionCode.toLong()
    }
fun Context.getAppName(): String = applicationInfo.loadLabel(packageManager).toString()
fun String.capitalize(locale: Locale) = apply { replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() } }
