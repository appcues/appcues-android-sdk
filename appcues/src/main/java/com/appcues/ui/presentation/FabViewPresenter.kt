package com.appcues.ui.presentation

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.appcues.AppcuesFrameView
import com.appcues.R
import com.appcues.data.model.RenderContext
import com.appcues.di.scope.AppcuesScope

internal class FabViewPresenter(scope: AppcuesScope, renderContext: RenderContext) : ViewPresenter(scope, renderContext) {

    override val shouldHandleBack = false

    override fun ViewGroup.setupView(activity: Activity): ComposeView {
        val fabView = if (findViewById<View>(R.id.appcues_fab_view) == null) {
            // create and add the view
            AppcuesFrameView(activity).apply {
                id = R.id.appcues_fab_view
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).also {
                    // adds margin top and bottom according to visible status and navigation bar
                    ViewCompat.getRootWindowInsets(this@setupView)?.getInsets(WindowInsetsCompat.Type.systemBars())
                        ?.let { insets -> it.setMargins(insets.left, insets.top, insets.right, insets.bottom) }
                }
            }.also { overlayView ->
                // if debugger view exists, ensure we are positioned behind it.
                findViewById<View>(R.id.appcues_debugger_view)
                    ?.let { addView(overlayView, indexOfChild(it)) }
                    ?: addView(overlayView)
            }
        } else {
            // this is just a fallback that should never hit, but as a good practice if the view is already there
            // for some reason, we can just use it.
            findViewById(R.id.appcues_fab_view)
        }

        fabView.isVisible = true
        return fabView.composeView
    }

    override fun ViewGroup.removeView() {
        findViewById<AppcuesFrameView?>(R.id.appcues_fab_view)?.let {
            it.reset()
            it.isVisible = false
            removeView(it)
        }
    }
}
