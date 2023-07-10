package com.appcues.data.model

internal sealed class RenderContext {
    data class Embed(val frameId: String) : RenderContext()
    object Modal : RenderContext()
}
