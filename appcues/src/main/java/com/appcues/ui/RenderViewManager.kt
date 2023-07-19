package com.appcues.ui

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.appcues.R
import com.appcues.data.model.RenderContext
import com.appcues.data.model.RenderContext.Embed
import com.appcues.data.model.RenderContext.Modal
import com.appcues.databinding.AppcuesOverlayLayoutBinding
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.monitor.AppcuesActivityMonitor.ActivityMonitorListener
import com.appcues.ui.composables.AppcuesComposition
import com.appcues.ui.primitive.EmbedChromeClient
import com.appcues.ui.utils.getParentView
import org.koin.core.scope.Scope

internal class RenderViewManager(
    private val scope: Scope,
    private val renderContext: RenderContext,
) : DefaultLifecycleObserver, ActivityMonitorListener {

    private var viewModel: AppcuesViewModel? = null

    private var shakeGestureListener: ShakeGestureListener? = null

    override fun onActivityChanged(activity: Activity) {
        viewModel?.onActivityChanged()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        shakeGestureListener?.start()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        shakeGestureListener?.stop()
    }

    fun start(): Boolean {
        val activity = AppcuesActivityMonitor.activity

        // inform the caller that we cannot currently add a view in the app
        return if (activity == null || AppcuesActivityMonitor.isPaused) {
            return false
        } else {
            shakeGestureListener = ShakeGestureListener(activity)
            viewModel = AppcuesViewModel(scope, renderContext, ::onCompositionDismiss)

            AppcuesActivityMonitor.subscribe(this)

            activity.getParentView().run {
                findViewTreeLifecycleOwner()?.lifecycle?.addObserver(this@RenderViewManager)

                setupRenderContextView()
            }
        }
    }

    private fun ViewGroup.setupRenderContextView(): Boolean {
        val binding = getOverlayLayoutBinding()
        val chromeClientView = binding.appcuesOverlayCustomViewContainer

        when (renderContext) {
            is Embed -> {
                // TODO
            }
            Modal -> {
                binding.appcuesOverlayComposeView.isVisible = true
                binding.appcuesOverlayComposeView.setAppcuesContent(chromeClientView, viewModel, shakeGestureListener)
                // remove customers view on accessibility stack
                setAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS)
            }
        }

        return true
    }

    private fun ViewGroup.getOverlayLayoutBinding(): AppcuesOverlayLayoutBinding {
        val overlayView = findViewById<ViewGroup>(R.id.appcues_overlay_layout)
        // given the overlay view is not added to the structure yet, we will add it by calling inflate, otherwise
        // we just bind the exiting reference and return it
        return if (overlayView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            AppcuesOverlayLayoutBinding.inflate(inflater).also {
                // ensures it always comes before appcues_debugger_view
                val debuggerView = findViewById<ViewGroup>(R.id.appcues_debugger_view)
                if (debuggerView != null) addView(it.root, indexOfChild(debuggerView)) else addView(it.root)

                it.root.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
                    // adds margin top and bottom according to visible status and navigation bar
                    ViewCompat.getRootWindowInsets(this@getOverlayLayoutBinding)?.getInsets(WindowInsetsCompat.Type.systemBars())
                        ?.let { insets -> setMargins(insets.left, insets.top, insets.right, insets.bottom) }
                }
            }
        } else {
            AppcuesOverlayLayoutBinding.bind(overlayView)
        }
    }

    private fun ComposeView.setAppcuesContent(
        chromeClientView: FrameLayout,
        viewModel: AppcuesViewModel?,
        shakeGestureListener: ShakeGestureListener?
    ) {
        if (viewModel != null && shakeGestureListener != null) {
            setContent {
                AppcuesComposition(
                    viewModel = remember { viewModel },
                    shakeGestureListener = remember { shakeGestureListener },
                    logcues = scope.get(),
                    chromeClient = EmbedChromeClient(chromeClientView),
                )
            }
        }
    }

    private fun onCompositionDismiss() {
        AppcuesActivityMonitor.activity?.getParentView()?.let {
            it.findViewTreeLifecycleOwner()?.lifecycle?.removeObserver(this@RenderViewManager)

            if (renderContext == Modal) {
                it.getOverlayLayoutBinding().run {
                    appcuesOverlayComposeView.isVisible = false
                    appcuesOverlayComposeView.setContent { }
                }
                it.setAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES)
            }
        }

        AppcuesActivityMonitor.unsubscribe(this)
        viewModel?.onFinish()
        viewModel = null
        shakeGestureListener = null
    }

    private fun ViewGroup.setAccessibility(accessibilityFlag: Int) {
        children.forEach { child ->
            if (child.id != R.id.appcues_overlay_layout && child.id != R.id.appcues_debugger_view)
                child.importantForAccessibility = accessibilityFlag
        }
    }
}
