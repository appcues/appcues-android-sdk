package com.appcues.util

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.StringRes
import androidx.core.app.NotificationManagerCompat
import java.util.Locale

/**
 * Context Wrapper class
 *
 * provide with methods related to context itself, abstracting the idea of android.content.Context
 * for easier testing
 */
internal class ContextWrapper(private val context: Context) {

    val orientation: String
        get() = if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) "landscape" else "portrait"

    val packageManager: PackageManager
        get() = context.packageManager

    val packageName: String
        get() = context.packageName

    fun getApplication(): Application = context.applicationContext as Application

    fun getString(@StringRes id: Int): String {
        return context.getString(id)
    }

    fun getString(@StringRes id: Int, vararg args: Any): String {
        return context.getString(id, *args)
    }

    fun getAppVersion(): String = with(context) {
        packageManager.getPackageInfoCompat(packageName, 0).versionName
    }

    fun getAppBuild(): Long = with(context) {
        if (VERSION.SDK_INT >= VERSION_CODES.P) {
            packageManager.getPackageInfoCompat(packageName, 0).longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0).versionCode.toLong()
        }
    }

    fun getAppName(): String = with(context) {
        applicationInfo.loadLabel(packageManager).toString()
    }

    fun getDeviceName(): String {
        if (Build.MODEL.lowercase().startsWith(Build.MANUFACTURER.lowercase())) {
            return Build.MODEL.capitalize()
        }
        return Build.MANUFACTURER.capitalize() + " " + Build.MODEL
    }

    fun getLanguage(): String {
        return getCurrentLocale(context).toLanguageTag()
    }

    fun isNotificationEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun isIntentSupported(intent: Intent): Boolean {
        return context.packageManager.resolveActivityCompat(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
    }

    fun startIntent(intent: Intent) {
        context.startActivity(intent)
    }

    private fun getCurrentLocale(context: Context): Locale {
        return if (VERSION.SDK_INT >= VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
    }

    private fun String.capitalize() = apply {
        replaceFirstChar { if (it.isLowerCase()) it.titlecase(getCurrentLocale(context)) else it.toString() }
    }
}
