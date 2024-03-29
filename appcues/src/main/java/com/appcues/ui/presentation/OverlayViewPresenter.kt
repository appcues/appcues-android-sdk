package com.appcues.ui.presentation

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import com.appcues.AppcuesFrameView
import com.appcues.R
import com.appcues.data.model.RenderContext
import com.appcues.di.scope.AppcuesScope

internal class OverlayViewPresenter(scope: AppcuesScope, renderContext: RenderContext) : ViewPresenter(scope, renderContext) {

    override val shouldHandleBack = true

    override fun ViewGroup.setupView(activity: Activity): ComposeView {
        // remove customers view on accessibility stack
        setAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS)

        val overlayView = if (findViewById<View>(R.id.appcues_overlay_view) == null) {
            // create and add the view
            AppcuesFrameView(activity).apply {
                id = R.id.appcues_overlay_view
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
            findViewById(R.id.appcues_overlay_view)
        }

        overlayView.isVisible = true
        return overlayView.composeView
    }

    override fun ViewGroup.removeView() {
        findViewById<AppcuesFrameView?>(R.id.appcues_overlay_view)?.let {
            it.reset()
            it.isVisible = false
            removeView(it)
        }

        // add customers view back to accessibility stack
        setAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES)
    }

    private fun ViewGroup.setAccessibility(accessibilityFlag: Int) {
        children.forEach { child ->
            if (child.id != R.id.appcues_overlay_view && child.id != R.id.appcues_debugger_view)
                child.importantForAccessibility = accessibilityFlag
        }
    }
}
