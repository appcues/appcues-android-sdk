package com.appcues

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.appcues.data.model.ExperienceTrigger.DeepLink
import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.debugger.DebugMode.Debugger
import com.appcues.debugger.DebugMode.ScreenCapture
import com.appcues.di.scope.AppcuesScope
import com.appcues.di.scope.inject
import com.appcues.push.PushDeeplinkHandler
import com.appcues.ui.ExperienceRenderer
import com.appcues.ui.ExperienceRenderer.PreviewResponse.ExperienceNotFound
import com.appcues.ui.ExperienceRenderer.PreviewResponse.Failed
import com.appcues.ui.ExperienceRenderer.PreviewResponse.PreviewDeferred
import com.appcues.ui.ExperienceRenderer.PreviewResponse.StateMachineError
import com.appcues.ui.ExperienceRenderer.PreviewResponse.Success
import com.appcues.util.ContextWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class DeepLinkHandler(scope: AppcuesScope) {

    companion object {

        fun getDebuggerValidationIntent(scheme: String, token: String): Intent {
            return Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("$scheme://sdk/debugger/$token")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }

    private val config by scope.inject<AppcuesConfig>()
    private val experienceRenderer by scope.inject<ExperienceRenderer>()
    private val appcuesCoroutineScope by scope.inject<CoroutineScope>()
    private val debuggerManager by scope.inject<AppcuesDebuggerManager>()
    private val pushDeeplinkHandler by scope.inject<PushDeeplinkHandler>()
    private val contextWrapper by scope.inject<ContextWrapper>()

    fun handle(activity: Activity, intent: Intent?): Boolean {
        if (intent == null) return false
        val linkAction: String? = intent.action
        val linkData: Uri? = intent.data

        if (linkData != null) {
            val scheme = contextWrapper.getString(R.string.appcues_custom_scheme).ifEmpty { "appcues-${config.applicationId}" }
            val validScheme = linkData.scheme == scheme
            val validHost = linkData.host == "sdk"

            if (linkAction == Intent.ACTION_VIEW && validScheme && validHost) {
                return processLink(linkData, activity)
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
    private fun processLink(linkData: Uri, activity: Activity): Boolean {
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

            pushDeeplinkHandler.processLink(activity, segments, query) -> true

            else -> false
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
