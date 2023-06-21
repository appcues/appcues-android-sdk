package com.appcues.data.model

internal sealed class RenderContext {
    object Modal : RenderContext()
    data class Embed(val frameId: String) : RenderContext()

    override fun toString(): String {
        return when (this) {
            is Embed -> "embed(frameId:$frameId)"
            Modal -> "modal"
        }
    }
}
