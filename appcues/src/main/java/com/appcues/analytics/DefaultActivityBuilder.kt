package com.appcues.analytics

import android.os.Build.VERSION
import com.appcues.AnalyticType
import com.appcues.AnalyticType.EVENT
import com.appcues.AnalyticType.GROUP
import com.appcues.AnalyticType.IDENTIFY
import com.appcues.AnalyticType.SCREEN
import com.appcues.AppcuesConfig
import com.appcues.BuildConfig
import com.appcues.R
import com.appcues.analytics.AnalyticsIntent.Anonymous
import com.appcues.analytics.AnalyticsIntent.Event
import com.appcues.analytics.AnalyticsIntent.Identify
import com.appcues.analytics.AnalyticsIntent.Screen
import com.appcues.analytics.AnalyticsIntent.UpdateGroup
import com.appcues.analytics.AnalyticsIntent.UpdateProfile
import com.appcues.util.ContextResources
import java.util.Date

internal class DefaultActivityBuilder(
    private val config: AppcuesConfig,
    contextResources: ContextResources,
) : ActivityBuilder {

    companion object {

        private const val SCREEN_ATTRIBUTE_TITLE = "screenTitle"
        private const val SCREEN_CONTEXT_TITLE = "screen_title"
    }

    private val contextProperties = hashMapOf<String, Any>(
        "app_id" to config.applicationId,
        "app_version" to contextResources.getAppVersion(),
    )

    private val applicationProperties = hashMapOf<String, Any?>(
        "_appId" to config.applicationId,
        "_operatingSystem" to "android",
        "_bundlePackageId" to contextResources.getPackageName(),
        "_appName" to contextResources.getAppName(),
        "_appVersion" to contextResources.getAppVersion(),
        "_appBuild" to contextResources.getAppBuild().toString(),
        "_sdkVersion" to BuildConfig.SDK_VERSION,
        "_sdkName" to "appcues-android",
        "_lastBrowserLanguage" to contextResources.getLanguage(),
        "_userAgent" to System.getProperty("http.agent"),
        "_osVersion" to "${VERSION.SDK_INT}",
        "_deviceType" to contextResources.getString(R.string.appcues_device_type),
        "_deviceModel" to contextResources.getDeviceName(),
    ).filterValues { it != null }.mapValues { it.value as Any }

    override suspend fun buildActivity(intent: AnalyticsIntent, sessionProperties: SessionProperties?): AnalyticsActivity? {
        if (sessionProperties == null) return null

        return AnalyticsActivity(
            type = intent.toAnalyticType(),
            isInternal = intent.isInternal,
            timestamp = intent.timestamp,
            sessionId = sessionProperties.sessionId,
            userId = sessionProperties.userId,
            groupId = sessionProperties.groupId,
            userSignature = sessionProperties.userSignature,
            profileProperties = getProfileProperties(intent, sessionProperties),
            groupProperties = getGroupProperties(intent),
            eventName = getEventName(intent),
            eventAttributes = getEventAttributes(intent, sessionProperties),
            eventContext = getEventContext(intent),
        )
    }

    private fun getProfileProperties(intent: AnalyticsIntent, sessionProperties: SessionProperties): Map<String, Any>? {
        // no profile properties on update group intents
        if (intent is UpdateGroup) return null

        return hashMapOf<String, Any>().apply {
            put("_updatedAt", Date())
            putAll(sessionProperties.latestUserProperties)
            putAll(applicationProperties)
            putAll(sessionProperties.properties)

            // additional props cannot overwrite values for existing internal prop keys
            config.additionalAutoProperties.forEach {
                if (containsKey(it.key).not()) {
                    put(it.key, it.value)
                }
            }
        }
    }

    private fun getGroupProperties(intent: AnalyticsIntent): Map<String, Any>? {
        // only updating group require groupProperties
        return if (intent is UpdateGroup) intent.properties else null
    }

    private fun getEventName(intent: AnalyticsIntent): String? {
        return when (intent) {
            is Event -> intent.name
            // screen event is just a different type of event
            is Screen -> AnalyticsEvent.ScreenView.eventName
            else -> null
        }
    }

    private fun getEventAttributes(intent: AnalyticsIntent, sessionProperties: SessionProperties): Map<String, Any>? {
        // when intent is screen or event its attributes are all the properties coming from the intent
        // + the existing profile properties into the _identify key
        return when (intent) {
            is Event -> mutableMapOf<String, Any>().apply {
                intent.properties?.also { putAll(it) }
                getProfileProperties(intent, sessionProperties)?.also { put("_identify", it) }
            }
            is Screen -> mutableMapOf<String, Any>().apply {
                // screen events got extra attribute key that defines screen title
                put(SCREEN_ATTRIBUTE_TITLE, intent.title)
                intent.properties?.also { putAll(it) }
                getProfileProperties(intent, sessionProperties)?.also { put("_identify", it) }
            }
            else -> null
        }
    }

    private fun getEventContext(intent: AnalyticsIntent): Map<String, Any>? {
        return when (intent) {
            is Event ->
                mutableMapOf<String, Any>().apply {
                    putAll(contextProperties)
                }
            is Screen -> mutableMapOf<String, Any>().apply {
                // screen events got extra context key that defines screen title
                put(SCREEN_CONTEXT_TITLE, intent.title)
                putAll(contextProperties)
            }
            else -> null
        }
    }

    private fun AnalyticsIntent.toAnalyticType(): AnalyticType {
        return when (this) {
            Anonymous -> IDENTIFY
            is Identify -> IDENTIFY
            is UpdateProfile -> IDENTIFY
            is UpdateGroup -> GROUP
            is Event -> EVENT
            is Screen -> SCREEN
        }
    }
}
