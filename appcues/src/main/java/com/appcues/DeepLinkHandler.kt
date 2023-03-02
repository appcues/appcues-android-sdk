package com.appcues

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.appcues.data.model.ExperienceTrigger.DeepLink
import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.debugger.DebugMode.Debugger
import com.appcues.debugger.DebugMode.ScreenCapture
import com.appcues.ui.ExperienceRenderer
import kotlinx.coroutines.launch

internal class DeepLinkHandler(
    private val config: AppcuesConfig,
    private val experienceRenderer: ExperienceRenderer,
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val debuggerManager: AppcuesDebuggerManager,
) {

    fun handle(activity: Activity, intent: Intent?): Boolean {
        val linkAction: String? = intent?.action
        val linkData: Uri? = intent?.data

        if (linkData != null) {
            val validScheme = linkData.scheme == "appcues-${config.applicationId}" || linkData.scheme == "appcues-democues"
            val validHost = linkData.host == "sdk"

            if (linkAction == Intent.ACTION_VIEW && validScheme && validHost) {
                return processLink(linkData, activity)
            }
        }

        return false // link not handled
    }

    // return true if handled
    private fun processLink(linkData: Uri, activity: Activity): Boolean {
        val segments = linkData.pathSegments
        return when {
            segments.count() == 2 && segments[0] == "experience_preview" -> {
                appcuesCoroutineScope.launch {
                    experienceRenderer.preview(segments[1])
                }
                true
            }
            segments.count() == 2 && segments[0] == "experience_content" -> {
                appcuesCoroutineScope.launch {
                    experienceRenderer.show(segments[1], DeepLink)
                }
                true
            }
            segments.any() && segments[0] == "debugger" -> {
                val deepLinkPath = if (segments.count() > 1) segments[1] else null
                debuggerManager.start(activity, Debugger(deepLinkPath))
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
}
