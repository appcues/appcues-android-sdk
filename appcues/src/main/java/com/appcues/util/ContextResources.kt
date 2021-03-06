package com.appcues.util

import android.content.Context
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.webkit.WebView
import androidx.annotation.StringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

// class used to access resources from classes that are not
// supposed to be tied to android context, this is helpful to keep
// any class clean of Android references, which makes it easier to unit test later
internal class ContextResources(private val context: Context) {

    fun getString(@StringRes id: Int): String {
        return context.getString(id)
    }

    fun getString(@StringRes id: Int, vararg args: Any): String {
        return context.getString(id, *args)
    }

    fun getAppVersion(): String = with(context) {
        packageManager.getPackageInfo(packageName, 0).versionName
    }

    fun getAppBuild(): Long = with(context) {
        if (VERSION.SDK_INT >= VERSION_CODES.P) {
            packageManager.getPackageInfo(packageName, 0).longVersionCode
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

    fun getPackageName(): String = with(context) {
        packageName
    }

    fun getLanguage(): String {
        return getCurrentLocale(context).language
    }

    suspend fun getUserAgent() = withContext(Dispatchers.Main) {
        // this must be on main thread
        return@withContext WebView(context).settings.userAgentString
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
