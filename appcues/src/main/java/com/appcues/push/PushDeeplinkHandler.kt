package com.appcues.push

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.appcues.AppcuesCoroutineScope
import com.appcues.AppcuesFirebaseMessagingService.AppcuesMessagingData
import com.appcues.R
import com.appcues.SessionMonitor
import com.appcues.Storage
import com.appcues.data.PushRepository
import com.appcues.data.remote.RemoteError.HttpErrorV2
import com.appcues.data.remote.RemoteError.NetworkError
import com.appcues.di.component.AppcuesComponent
import com.appcues.di.component.inject
import com.appcues.di.scope.AppcuesScope
import com.appcues.di.scope.inject
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import kotlinx.coroutines.launch

internal class PushDeeplinkHandler(
    override val scope: AppcuesScope,
) : AppcuesComponent {

    companion object {

        private const val NOTIFICATION_ID_EXTRA = "ID"
        private const val NOTIFICATION_VERSION_EXTRA = "VERSION"
        private const val NOTIFICATION_SHOW_CONTENT_EXTRA = "SHOW_CONTENT"
        private const val NOTIFICATION_TEST_EXTRA = "TEST"
        private const val NOTIFICATION_WORKFLOW_ID_EXTRA = "WORKFLOW_ID"
        private const val NOTIFICATION_WORKFLOW_TASK_ID_EXTRA = "WORKFLOW_TASK_ID"
        private const val NOTIFICATION_WORKFLOW_VERSION_EXTRA = "WORKFLOW_VERSION"
        private const val NOTIFICATION_FORWARD_DEEPLINK_EXTRA = "FORWARD_DEEPLINK"

        private const val NETWORK_ERROR_NOT_FOUND = 404

        fun getNotificationIntent(scheme: String, appcuesData: AppcuesMessagingData) = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("$scheme://sdk/notification")
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
    }

    private val sessionMonitor by inject<SessionMonitor>()
    private val pushOpenedProcessor by inject<PushOpenedProcessor>()
    private val coroutineScope by scope.inject<AppcuesCoroutineScope>()
    private val storage by inject<Storage>()
    private val pushRepository by scope.inject<PushRepository>()

    fun processLink(context: Context, segments: List<String>, extras: Bundle?, query: Map<String, String>): Boolean {
        return when {
            segments.any() && segments[0] == "notification" && extras != null -> {
                processNotification(extras)
                true
            }
            segments.count() == 2 && segments[0] == "push_preview" -> {
                processPreviewPush(context, segments[1], query)
                true
            }
            segments.count() == 2 && segments[0] == "push_content" -> {
                processShowPush(segments[1])
                true
            }
            else -> false
        }
    }

    private fun processNotification(extras: Bundle) {
        val isTest = extras.getBoolean(NOTIFICATION_TEST_EXTRA, false)

        val properties = mapOf<String, Any?>(
            "notification_id" to extras.getString(NOTIFICATION_ID_EXTRA, null),
            "push_notification_version" to extras.getLong(NOTIFICATION_VERSION_EXTRA, -1L).let { if (it == -1L) null else it },
            "workflow_id" to extras.getString(NOTIFICATION_WORKFLOW_ID_EXTRA, null),
            "workflow_task_id" to extras.getString(NOTIFICATION_WORKFLOW_TASK_ID_EXTRA, null),
            "workflow_version" to extras.getLong(NOTIFICATION_WORKFLOW_VERSION_EXTRA, -1L).let { if (it == -1L) null else it },
            "device_id" to storage.deviceId
        ).filterValues { it != null }.mapValues { it.value as Any }

        val deeplink = extras.getString(NOTIFICATION_FORWARD_DEEPLINK_EXTRA, null)
        val experienceId = extras.getString(NOTIFICATION_SHOW_CONTENT_EXTRA, null)

        val action = PushOpenedAction(storage.userId, properties, deeplink, experienceId, isTest)

        if (sessionMonitor.hasSession()) {
            coroutineScope.launch { pushOpenedProcessor.process(action) }
        } else {
            pushOpenedProcessor.defer(action)
        }
    }

    private fun processPreviewPush(context: Context, id: String, query: Map<String, String>) {
        coroutineScope.launch {
            val result = pushRepository.preview(id, query)

            when (result) {
                is Failure -> when {
                    result.reason is HttpErrorV2 && result.reason.code == NETWORK_ERROR_NOT_FOUND ->
                        context.resources.getString(R.string.appcues_preview_push_not_found)
                    result.reason is NetworkError ->
                        context.resources.getString(R.string.appcues_preview_push_failed)
                    else -> context.resources.getString(R.string.appcues_preview_push_failed_generic)
                }
                is Success -> null
            }?.let { errorMessage -> Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show() }
        }
    }

    private fun processShowPush(id: String) {
        coroutineScope.launch { pushRepository.send(id) }
    }
}
