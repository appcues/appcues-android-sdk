package com.appcues

import android.view.ViewGroup
import com.appcues.data.model.RenderContext
import java.lang.ref.WeakReference

internal class RenderContextManager {

    private val slots: HashMap<RenderContext, WeakReference<ViewGroup>> = hashMapOf()

    fun registerEmbedFrame(frameId: String, view: ViewGroup) {
        slots[RenderContext.Embed(frameId)] = WeakReference(view)
    }

    fun getEmbedView(renderContext: RenderContext.Embed): ViewGroup? {
        return slots[renderContext]?.get().also { if (it == null) slots.remove(renderContext) }
    }

    fun clear() {
        slots.clear()
    }
}
