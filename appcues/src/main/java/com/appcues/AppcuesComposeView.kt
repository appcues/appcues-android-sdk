package com.appcues

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable

public interface AppcuesComposeView {

    @Composable
    public fun BoxScope.Compose(controller: ExperienceRemoteController, config: Map<String, Any>?)
}
