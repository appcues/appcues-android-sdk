package com.appcues

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.appcues.AppcuesFirebaseMessagingService.AppcuesMessagingData
import com.appcues.analytics.AnalyticsEvent.PushOpened
import com.appcues.analytics.AnalyticsTracker
import com.appcues.data.model.ExperienceTrigger.DeepLink
import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.debugger.DebugMode.Debugger
import com.appcues.debugger.DebugMode.ScreenCapture
import com.appcues.di.scope.AppcuesScope
import com.appcues.di.scope.inject
import com.appcues.ui.ExperienceRenderer
import com.appcues.ui.ExperienceRenderer.PreviewResponse.ExperienceNotFound
import com.appcues.ui.ExperienceRenderer.PreviewResponse.Failed
import com.appcues.ui.ExperienceRenderer.PreviewResponse.PreviewDeferred
import com.appcues.ui.ExperienceRenderer.PreviewResponse.StateMachineError
import com.appcues.ui.ExperienceRenderer.PreviewResponse.Success
import com.appcues.util.ContextWrapper
import kotlinx.coroutines.launch

internal class DeepLinkHandler(scope: AppcuesScope) {

    companion object {

        private const val NOTIFICATION_ID_EXTRA = "ID"
        private const val NOTIFICATION_VERSION_EXTRA = "VERSION"
        private const val NOTIFICATION_SHOW_CONTENT_EXTRA = "SHOW_CONTENT"
        private const val NOTIFICATION_TEST_EXTRA = "TEST"
        private const val NOTIFICATION_WORKFLOW_ID_EXTRA = "WORKFLOW_ID"
        private const val NOTIFICATION_WORKFLOW_TASK_ID_EXTRA = "WORKFLOW_TASK_ID"
        private const val NOTIFICATION_WORKFLOW_VERSION_EXTRA = "WORKFLOW_VERSION"
        private const val NOTIFICATION_FORWARD_DEEPLINK_EXTRA = "FORWARD_DEEPLINK"

        fun getNotificationIntent(appcuesData: AppcuesMessagingData) = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("appcues-${appcuesData.appId}://sdk/notification")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK

            putExtra(NOTIFICATION_ID_EXTRA, appcuesData.notificationId)
            putExtra(NOTIFICATION_VERSION_EXTRA, appcuesData.notificationVersion)
            putExtra(NOTIFICATION_WORKFLOW_ID_EXTRA, appcuesData.workflowId)
            putExtra(NOTIFICATION_WORKFLOW_TASK_ID_EXTRA, appcuesData.workflowTaskId)
            putExtra(NOTIFICATION_WORKFLOW_VERSION_EXTRA, appcuesData.workflowVersion)
            putExtra(NOTIFICATION_FORWARD_DEEPLINK_EXTRA, appcuesData.deeplink)
            putExtra(NOTIFICATION_SHOW_CONTENT_EXTRA, appcuesData.experienceId)
            putExtra(NOTIFICATION_TEST_EXTRA, appcuesData.test)
        }

        fun getDebuggerValidationIntent(appId: String, token: String): Intent {
            return Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("appcues-$appId://sdk/debugger/$token")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

        fun getGenericIntent(uriString: String): Intent {
            return Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(uriString)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }

    private val config by scope.inject<AppcuesConfig>()
    private val experienceRenderer by scope.inject<ExperienceRenderer>()
    private val appcuesCoroutineScope by scope.inject<AppcuesCoroutineScope>()
    private val debuggerManager by scope.inject<AppcuesDebuggerManager>()
    private val analyticsTracker by scope.inject<AnalyticsTracker>()
    private val contextWrapper by scope.inject<ContextWrapper>()
    private val storage by scope.inject<Storage>()

    fun handle(activity: Activity, intent: Intent?): Boolean {
        if (intent == null) return false
        val linkAction: String? = intent.action
        val linkData: Uri? = intent.data
        val extras = intent.extras

        if (linkData != null) {
            val validScheme = linkData.scheme == "appcues-${config.applicationId}" || linkData.scheme == "appcues-democues"
            val validHost = linkData.host == "sdk"

            if (linkAction == Intent.ACTION_VIEW && validScheme && validHost) {
                return processLink(linkData, activity, extras)
            }
        }

        return false // link not handled
    }

    private fun Uri.getQueryMap(): Map<String, String> {
        val queryMap = mutableMapOf<String, String>()
        queryParameterNames.forEach { key ->
            getQueryParameter(key)?.let { value ->
                queryMap[key] = value
            }
        }

        return queryMap
    }

    // return true if handled
    private fun processLink(linkData: Uri, activity: Activity, extras: Bundle?): Boolean {
        val segments = linkData.pathSegments
        val query = linkData.getQueryMap()

        return when {
            segments.count() == 2 && segments[0] == "experience_preview" -> {
                appcuesCoroutineScope.launch {
                    previewExperience(segments[1], activity, query)
                }
                true
            }

            segments.count() == 2 && segments[0] == "experience_content" -> {
                appcuesCoroutineScope.launch {
                    experienceRenderer.show(segments[1], DeepLink, query)
                }
                true
            }

            segments.any() && segments[0] == "notification" && extras != null -> {
                processNotification(extras)
                true
            }

            segments.any() && segments[0] == "debugger" -> {
                val deepLinkPath = if (segments.count() > 1) segments[1] else null
                debuggerManager.start(activity, Debugger, deepLinkPath)
                true
            }

            segments.any() && segments[0] == "capture_screen" -> {
                val token = linkData.getQueryParameter("token")
                if (token != null) {
                    debuggerManager.start(activity, ScreenCapture(token))
                    true
                } else {
                    false
                }
            }

            else -> false
        }
    }

    private fun processNotification(extras: Bundle) {
        if (!extras.getBoolean(NOTIFICATION_TEST_EXTRA)) {
            analyticsTracker.track(
                PushOpened.eventName,
                properties = mapOf<String, Any?>(
                    "notification_id" to extras.getString(NOTIFICATION_ID_EXTRA, null),
                    "push_notification_version" to extras.getLong(NOTIFICATION_VERSION_EXTRA, -1L).let { if (it == -1L) null else it },
                    "workflow_id" to extras.getString(NOTIFICATION_WORKFLOW_ID_EXTRA, null),
                    "workflow_task_id" to extras.getString(NOTIFICATION_WORKFLOW_TASK_ID_EXTRA, null),
                    "workflow_version" to extras.getLong(NOTIFICATION_WORKFLOW_VERSION_EXTRA, -1L).let { if (it == -1L) null else it },
                    "device_id" to storage.deviceId
                ).filterValues { it != null }.mapValues { it.value as Any },
                interactive = false,
                isInternal = true
            )
        }

        extras.getString("forward_deeplink")?.let {
            contextWrapper.startIntent(getGenericIntent(it))
        }

        extras.getString("show_content")?.let {
            appcuesCoroutineScope.launch {
                experienceRenderer.show(it, DeepLink, mapOf())
            }
        }
    }

    private suspend fun previewExperience(experienceId: String, activity: Activity, query: Map<String, String>) {
        experienceRenderer.preview(experienceId, query).run {
            val resources = activity.resources

            when (this) {
                is Failed -> resources.getString(R.string.appcues_preview_flow_failed)
                is PreviewDeferred -> {
                    if (frameId != null) {
                        resources.getString(R.string.appcues_preview_embed_message, frameId, experience.name)
                    } else {
                        resources.getString(R.string.appcues_preview_flow_failed)
                    }
                }

                is StateMachineError -> resources.getString(R.string.appcues_preview_flow_failed_reason, experience.name, error.message)
                is ExperienceNotFound -> resources.getString(R.string.appcues_preview_flow_not_found) // do nothing. previewing experience
                is Success -> null
            }?.let { errorMessage -> Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show() }
        }
    }
}
