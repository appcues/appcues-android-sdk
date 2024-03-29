package com.appcues

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * A Compose view that displays an Appcues experience.
 */
@Composable
public fun AppcuesFrame(modifier: Modifier = Modifier, appcues: Appcues, frameId: String) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AppcuesFrameView(context).apply {
                appcues.registerEmbed(frameId, this)
            }
        }
    )
}
