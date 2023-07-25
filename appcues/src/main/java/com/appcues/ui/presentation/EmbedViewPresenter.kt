package com.appcues.ui.presentation

import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.isVisible
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

    override fun setViewVisible(isVisible: Boolean) {
        // adhering to requirement for the AppcuesViewModel constructor on line 71 below
        // would got called during any trait error/retry flow - which is not applicable here
        // for embeds, as it is only used for tooltips in flows, currently.
        renderContextManager.getEmbedView(renderContext)?.isVisible = isVisible
    }
}
