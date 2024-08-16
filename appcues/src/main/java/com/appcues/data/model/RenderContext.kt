package com.appcues.data.model

import com.appcues.data.model.RenderContext.Embed
import com.appcues.data.model.RenderContext.Launcher
import com.appcues.data.model.RenderContext.Modal

internal sealed class RenderContext {
    data class Embed(val frameId: String) : RenderContext()
    object Modal : RenderContext()
    object Launcher : RenderContext()
}

internal fun RenderContext.getFrameId(): String? {
    return when (this) {
        is Embed -> frameId
        Modal -> null
        Launcher -> null
    }
}
