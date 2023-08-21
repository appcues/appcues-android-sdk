package com.appcues.ui.presentation

import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.isVisible
import com.appcues.AppcuesFrameView
import com.appcues.data.model.RenderContext
import com.appcues.ui.AppcuesFrameStateMachineOwner
import com.appcues.ui.StateMachineDirectory
import org.koin.core.scope.Scope

internal class EmbedViewPresenter(
    scope: Scope,
    renderContext: RenderContext,
    private val stateMachines: StateMachineDirectory,
) : ViewPresenter(scope, renderContext) {

    override fun ViewGroup.setupView(): ComposeView? {
        return stateMachines.getFrame(renderContext)?.setupComposeView()
    }

    override fun ViewGroup.removeView() {
        stateMachines.getFrame(renderContext)?.let {
            post {
                it.reset()
            }
        }
    }

    override fun setViewVisible(isVisible: Boolean) {
        // adhering to requirement for the AppcuesViewModel constructor on line 71 below
        // would got called during any trait error/retry flow - which is not applicable here
        // for embeds, as it is only used for tooltips in flows, currently.
        stateMachines.getFrame(renderContext)?.isVisible = isVisible
    }

    private fun StateMachineDirectory.getFrame(context: RenderContext): AppcuesFrameView? {
        return (getOwner(context) as? AppcuesFrameStateMachineOwner)?.frame?.get()
    }
}
