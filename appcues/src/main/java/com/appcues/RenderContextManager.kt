package com.appcues

import android.view.ViewGroup
import com.appcues.data.model.RenderContext
import com.appcues.data.model.RenderContext.Embed
import java.lang.ref.WeakReference

internal class RenderContextManager {

    private val slots: HashMap<RenderContext, WeakReference<ViewGroup>> = hashMapOf()

    fun registerEmbedFrame(frameId: String, view: ViewGroup) {
        slots[Embed(frameId)] = WeakReference(view)
    }

    fun getEmbedView(renderContext: RenderContext): ViewGroup? {
        return when (renderContext) {
            is Embed -> slots[renderContext]?.get()
            else -> null
        }
    }

    fun clear() {
        slots.clear()
    }
}
