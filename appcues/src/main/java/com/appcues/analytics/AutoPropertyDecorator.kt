package com.appcues.analytics

import android.content.Context
import android.os.Build
import android.os.Build.MANUFACTURER
import android.os.Build.MODEL
import android.os.Build.VERSION_CODES
import android.webkit.WebView
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import com.appcues.AppcuesConfig
import com.appcues.BuildConfig
import com.appcues.R
import com.appcues.SessionMonitor
import com.appcues.Storage
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.data.remote.request.EventRequest
import java.util.Date

internal class AutoPropertyDecorator(
    context: Context,
    config: AppcuesConfig,
    private val storage: Storage,
    private val sessionMonitor: SessionMonitor,
) {

    private val contextProperties = hashMapOf<String, Any>(
        "app_id" to config.applicationId,
        "app_version" to context.getAppVersion(),
    )

    private val applicationProperties = hashMapOf<String, Any>(
        "_appId" to config.applicationId,
        "_operatingSystem" to "android",
        "_bundlePackageId" to context.packageName,
        "_appName" to context.getAppName(),
        "_appVersion" to context.getAppVersion(),
        "_appBuild" to context.getAppBuild().toString(),
        "_sdkVersion" to BuildConfig.SDK_VERSION,
        "_sdkName" to "appcues-android",
        "_deviceType" to context.resources.getString(R.string.device_type),
        "_deviceModel" to getDeviceName(),
        "_userAgent2" to WebView(context).settings.userAgentString
    )

    private val sessionProperties: Map<String, Any>
        get() = hashMapOf(
            "userId" to storage.userId,
            "_isAnonymous" to storage.isAnonymous,
            "_localId" to storage.deviceId,
            "_updatedAt" to Date().time,
            "_sessionId" to sessionMonitor.sessionId?.toString(),
            "_lastContentShownAt" to storage.lastContentShownAt?.time
            // more items to work through below:
            // _sessionPageviews
            // _sessionRandomizer
            // _currentPageTitle
            // _lastPageTitle
            // _lastBrowserLanguage
        ).filterValues { it != null }.mapValues { it.value as Any }

    private val autoProperties: HashMap<String, Any>
        get() = hashMapOf<String, Any>().apply {
            putAll(applicationProperties)
            putAll(sessionProperties)
        }

    fun decorateTrack(event: EventRequest) = event.apply {
        attributes["_identity"] = autoProperties
        context.putAll(contextProperties)

        // special handling for screen_view events
        if (event.name == "appcues:screen_view") {
            // copy "screen" into context
            attributes["screenTitle"]?.let {
                context["screen"] = it
            }
        }
    }

    fun decorateIdentify(activity: ActivityRequest) = activity.apply {
        profileUpdate = (profileUpdate ?: hashMapOf()).apply {
            putAll(autoProperties)
        }
    }

    private fun getDeviceName(): String {
        if (MODEL.lowercase().startsWith(MANUFACTURER.lowercase())) {
            return MODEL.capitalize(Locale.current)
        }
        return MANUFACTURER.capitalize(Locale.current) + " " + MODEL
    }
}

fun Context.getAppVersion(): String = packageManager.getPackageInfo(packageName, 0).versionName
fun Context.getAppBuild() =
    if (Build.VERSION.SDK_INT >= VERSION_CODES.P) {
        packageManager.getPackageInfo(packageName, 0).longVersionCode
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, 0).versionCode.toLong()
    }
fun Context.getAppName(): String = applicationInfo.loadLabel(packageManager).toString()
