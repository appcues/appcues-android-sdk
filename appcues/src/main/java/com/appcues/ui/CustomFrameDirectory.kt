package com.appcues.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.appcues.AppcuesComposeView
import com.appcues.ExperienceRemoteController
import com.appcues.data.model.AppcuesConfigMap
import kotlin.collections.set

internal class CustomFrameDirectory {

    private var views = mutableMapOf<String, AppcuesComposeView>()

    fun set(identifier: String, view: AppcuesComposeView) {
        views[identifier] = view
    }

    @Composable
    fun BoxScope.ComposeView(identifier: String, controller: ExperienceRemoteController, config: AppcuesConfigMap) {
        val view = views[identifier] ?: return

        with(view) { Compose(controller, config) }
    }
}
