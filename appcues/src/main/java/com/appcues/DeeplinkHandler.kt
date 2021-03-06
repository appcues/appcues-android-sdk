package com.appcues

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.ui.ExperienceRenderer
import kotlinx.coroutines.launch

internal class DeeplinkHandler(
    private val config: AppcuesConfig,
    private val experienceRenderer: ExperienceRenderer,
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val debuggerManager: AppcuesDebuggerManager,
) {

    fun handle(activity: Activity, intent: Intent?): Boolean {
        val linkAction: String? = intent?.action
        val linkData: Uri? = intent?.data
        var handled = false

        if (linkData == null) return handled

        val validScheme = linkData.scheme == "appcues-${config.applicationId}" || linkData.scheme == "appcues-democues"
        val validHost = linkData.host == "sdk"

        if (linkAction == Intent.ACTION_VIEW && validScheme && validHost) {
            val segments = linkData.pathSegments
            when {
                segments.count() == 2 && segments[0] == "experience_preview" -> {
                    appcuesCoroutineScope.launch {
                        experienceRenderer.preview(segments[1])
                    }
                    handled = true
                }
                segments.count() == 2 && segments[0] == "experience_content" -> {
                    appcuesCoroutineScope.launch {
                        experienceRenderer.show(segments[1])
                    }
                    handled = true
                }
                segments.any() && segments[0] == "debugger" -> {
                    val deeplinkPath = if (segments.count() > 1) segments[1] else null
                    debuggerManager.start(activity, deeplinkPath)
                    handled = true
                }
            }
        }

        return handled
    }
}
