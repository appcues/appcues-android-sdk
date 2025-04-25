package com.appcues.push

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import com.appcues.util.appcuesFormatted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

internal class PushDeeplinkHandler(
    override val scope: AppcuesScope,
) : AppcuesComponent {

    companion object {

        private const val NOTIFICATION_VERSION_PARAM = "version"
        private const val NOTIFICATION_SHOW_CONTENT_PARAM = "show_content"
        private const val NOTIFICATION_TEST_PARAM = "test"
        private const val NOTIFICATION_WORKFLOW_ID_PARAM = "workflow_id"
        private const val NOTIFICATION_WORKFLOW_TASK_ID_PARAM = "workflow_task_id"
        private const val NOTIFICATION_WORKFLOW_VERSION_PARAM = "workflow_version"
        private const val NOTIFICATION_FORWARD_DEEPLINK_PARAM = "forward_deeplink"

        private const val NETWORK_ERROR_NOT_FOUND = 404

        fun getNotificationIntent(scheme: String, appcuesData: AppcuesMessagingData) = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            // note: encoding all the params into the URI rather than using extras on the intent here, so that the linking
            // tools used in all cross-platform frameworks will continue to work and preserve the push payload details
            data = Uri.Builder()
                .scheme(scheme)
                .authority("sdk")
                .appendPath("notification")
                .appendPath(appcuesData.notificationId)
                .apply {
                    if (appcuesData.test) { appendQueryParameter(NOTIFICATION_TEST_PARAM, "true") }
                    appcuesData.notificationVersion?.let { appendQueryParameter(NOTIFICATION_VERSION_PARAM, it.toString()) }
                    appcuesData.workflowId?.let { appendQueryParameter(NOTIFICATION_WORKFLOW_ID_PARAM, it) }
                    appcuesData.workflowTaskId?.let { appendQueryParameter(NOTIFICATION_WORKFLOW_TASK_ID_PARAM, it) }
                    appcuesData.workflowVersion?.let { appendQueryParameter(NOTIFICATION_WORKFLOW_VERSION_PARAM, it.toString()) }
                    appcuesData.experienceId?.let { appendQueryParameter(NOTIFICATION_SHOW_CONTENT_PARAM, it) }
                    appcuesData.deeplink?.let { appendQueryParameter(NOTIFICATION_FORWARD_DEEPLINK_PARAM, it) }
                }
                .build()
        }
    }

    private val sessionMonitor by inject<SessionMonitor>()
    private val pushOpenedProcessor by inject<PushOpenedProcessor>()
    private val coroutineScope by scope.inject<CoroutineScope>()
    private val storage by inject<Storage>()
    private val pushRepository by scope.inject<PushRepository>()

    fun processLink(context: Context, segments: List<String>, query: Map<String, String>): Boolean {
        return when {
            segments.count() == 2 && segments[0] == "notification" -> {
                val pushNotificationId = UUID.fromString(segments[1])
                processNotification(pushNotificationId, query)
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

    private fun processNotification(pushNotificationId: UUID, query: Map<String, String>) {
        val isTest = query[NOTIFICATION_TEST_PARAM]?.toBoolean() ?: false

        val properties = mapOf<String, Any?>(
            "push_notification_id" to pushNotificationId.appcuesFormatted(),
            "push_notification_version" to query[NOTIFICATION_VERSION_PARAM]?.toLongOrNull(),
            "workflow_id" to query[NOTIFICATION_WORKFLOW_ID_PARAM],
            "workflow_task_id" to query[NOTIFICATION_WORKFLOW_TASK_ID_PARAM],
            "workflow_version" to query[NOTIFICATION_WORKFLOW_VERSION_PARAM]?.toLongOrNull(),
            "device_id" to storage.deviceId
        ).filterValues { it != null }.mapValues { it.value as Any }

        val deeplink = query[NOTIFICATION_FORWARD_DEEPLINK_PARAM]
        val experienceId = query[NOTIFICATION_SHOW_CONTENT_PARAM]

        val action = PushOpenedAction(pushNotificationId, storage.userId, properties, deeplink, experienceId, isTest)

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
