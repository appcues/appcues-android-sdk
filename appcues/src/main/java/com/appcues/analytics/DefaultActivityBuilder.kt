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
import com.appcues.analytics.AnalyticIntent.Anonymous
import com.appcues.analytics.AnalyticIntent.Event
import com.appcues.analytics.AnalyticIntent.Identify
import com.appcues.analytics.AnalyticIntent.Screen
import com.appcues.analytics.AnalyticIntent.UpdateGroup
import com.appcues.analytics.AnalyticIntent.UpdateProfile
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

    override suspend fun build(intent: AnalyticIntent, session: Session): AnalyticActivity {
        return AnalyticActivity(
            type = intent.toAnalyticType(),
            isInternal = intent.isInternal,
            timestamp = intent.timestamp,
            sessionId = session.sessionId,
            userId = session.userId,
            groupId = session.groupId,
            userSignature = session.userSignature,
            profileProperties = getProfileProperties(intent, session),
            groupProperties = getGroupProperties(intent),
            eventName = getEventName(intent),
            eventAttributes = getEventAttributes(intent, session),
            eventContext = getEventContext(intent),
        )
    }

    private fun getProfileProperties(intent: AnalyticIntent, session: Session): Map<String, Any>? {
        // no profile properties on update group intents
        if (intent is UpdateGroup) return null

        return hashMapOf<String, Any>().apply {
            put("_updatedAt", Date())
            putAll(session.latestUserProperties)
            putAll(applicationProperties)
            putAll(session.properties)

            // additional props cannot overwrite values for existing internal prop keys
            config.additionalAutoProperties.forEach {
                if (containsKey(it.key).not()) {
                    put(it.key, it.value)
                }
            }
        }
    }

    private fun getGroupProperties(intent: AnalyticIntent): Map<String, Any>? {
        // only updating group require groupProperties and should only set if groupId is not null
        return if (intent is UpdateGroup && intent.groupId != null) intent.properties else null
    }

    private fun getEventName(intent: AnalyticIntent): String? {
        return when (intent) {
            is Event -> intent.name
            // screen event is just a different type of event
            is Screen -> AnalyticsEvent.ScreenView.eventName
            else -> null
        }
    }

    private fun getEventAttributes(intent: AnalyticIntent, session: Session): Map<String, Any>? {
        // when intent is screen or event its attributes are all the properties coming from the intent
        // + the existing profile properties into the _identify key
        return when (intent) {
            is Event -> mutableMapOf<String, Any>().apply {
                intent.properties?.also { putAll(it) }
                getProfileProperties(intent, session)?.also { put("_identify", it) }
            }
            is Screen -> mutableMapOf<String, Any>().apply {
                // screen events got extra attribute key that defines screen title
                put(SCREEN_ATTRIBUTE_TITLE, intent.title)
                intent.properties?.also { putAll(it) }
                getProfileProperties(intent, session)?.also { put("_identify", it) }
            }
            else -> null
        }
    }

    private fun getEventContext(intent: AnalyticIntent): Map<String, Any>? {
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

    private fun AnalyticIntent.toAnalyticType(): AnalyticType {
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
