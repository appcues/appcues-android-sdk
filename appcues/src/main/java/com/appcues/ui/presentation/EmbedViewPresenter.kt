package com.appcues.ui.presentation

import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.appcues.RenderContextManager
import com.appcues.data.model.RenderContext
import org.koin.core.scope.Scope

internal class EmbedViewPresenter(
    scope: Scope,
    renderContext: RenderContext,
    private val renderContextManager: RenderContextManager,
) : ViewPresenter(scope, renderContext) {

    override fun ViewGroup.setupView(): ComposeView? {
        val embedView = renderContextManager.getEmbedView(renderContext) ?: return null

        return ComposeView(context).also {
            embedView.addView(it)
        }
    }

    override fun ViewGroup.removeView() {
        post {
            renderContextManager.getEmbedView(renderContext)?.removeAllViews()
        }
    }
}
