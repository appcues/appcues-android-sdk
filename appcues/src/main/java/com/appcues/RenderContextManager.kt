package com.appcues

import com.appcues.data.model.RenderContext
import com.appcues.data.model.RenderContext.Embed
import java.lang.ref.WeakReference

internal class RenderContextManager {

    private val slots: HashMap<RenderContext, WeakReference<AppcuesFrameView>> = hashMapOf()

    fun registerEmbedFrame(frameId: String, frame: AppcuesFrameView) {
        slots[Embed(frameId)] = WeakReference(frame)
    }

    fun getEmbedFrame(renderContext: RenderContext): AppcuesFrameView? {
        return when (renderContext) {
            is Embed -> slots[renderContext]?.get()
            else -> null
        }
    }

    fun clear() {
        slots.clear()
    }
}
