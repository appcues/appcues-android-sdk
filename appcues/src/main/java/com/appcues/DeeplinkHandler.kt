package com.appcues

import android.content.Intent
import android.net.Uri
import com.appcues.ui.ExperienceRenderer
import kotlinx.coroutines.launch

internal class DeeplinkHandler(
    private val config: AppcuesConfig,
    private val appcues: Appcues,
    private val experienceRenderer: ExperienceRenderer,
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
) {

    fun handle(intent: Intent?) {
        val linkAction: String? = intent?.action
        val linkData: Uri? = intent?.data

        if (linkAction == Intent.ACTION_VIEW &&
            linkData?.scheme == "appcues-${config.applicationId}" &&
            linkData.host == "sdk"
        ) {
            val segments = linkData.pathSegments
            when {
                segments.count() == 2 && segments[0] == "experience_preview" -> {
                    appcuesCoroutineScope.launch {
                        experienceRenderer.preview(segments[1])
                    }
                }
                segments.count() == 2 && segments[0] == "experience_content" -> {
                    appcuesCoroutineScope.launch {
                        experienceRenderer.show(segments[1])
                    }
                }
                segments.count() == 1 && segments[0] == "debugger" -> appcues.debug()
            }
        }
    }
}
