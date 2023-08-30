package com.appcues.ui.presentation

import android.app.Activity
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

    override fun ViewGroup.setupView(activity: Activity): ComposeView? {
        return stateMachines.getFrame(renderContext)?.let {
            it.isVisible = true
            it.setupComposeView()
        }
    }

    override fun ViewGroup.removeView() {
        stateMachines.getFrame(renderContext)?.let {
            it.isVisible = false
            it.reset()
        }
    }

    private fun StateMachineDirectory.getFrame(context: RenderContext): AppcuesFrameView? {
        return (getOwner(context) as? AppcuesFrameStateMachineOwner)?.frame?.get()
    }
}
