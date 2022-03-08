package com.appcues.analytics

import android.content.Context
import android.os.Build
import android.os.Build.MANUFACTURER
import android.os.Build.MODEL
import android.os.Build.VERSION_CODES
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import com.appcues.AppcuesConfig
import com.appcues.BuildConfig
import com.appcues.R
import com.appcues.data.remote.request.EventRequest

internal class AutoPropertyDecorator(
    context: Context,
    config: AppcuesConfig,
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
    )

    fun decorate(event: EventRequest) = event.apply {
        attributes["_identity"] = applicationProperties
        context.putAll(contextProperties)
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
