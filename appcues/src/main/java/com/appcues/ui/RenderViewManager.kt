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

    private val renderContextManager: RenderContextManager by scope.inject()

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
            AppcuesActivityMonitor.subscribe(this)

            shakeGestureListener = ShakeGestureListener(activity)

            activity.getParentView().run {
                post {
                    findViewTreeLifecycleOwner()?.lifecycle?.addObserver(this@RenderViewManager)

                    setupRenderContextView()
                }
            }
        }
    }

    private fun ViewGroup.setupRenderContextView(): Boolean {
        val binding = getOverlayLayoutBinding()
        val chromeClientView = binding.appcuesOverlayCustomViewContainer

        if (viewModel == null) viewModel = AppcuesViewModel(scope, renderContext, ::onCompositionDismiss)

        when (renderContext) {
            is Embed -> {
                val embedView = renderContextManager.getEmbedView(renderContext) ?: return false
                val composeView = ComposeView(context).apply { setAppcuesComposition(chromeClientView) }

                embedView.addView(composeView)
            }
            Modal -> {
                binding.appcuesOverlayComposeView.isVisible = true
                binding.appcuesOverlayComposeView.setAppcuesComposition(chromeClientView)
                // remove customers view on accessibility stack
                setAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS)
            }
        }

        return true
    }

    private fun ViewGroup.getOverlayLayoutBinding(): AppcuesOverlayLayoutBinding {
        val overlayView = findViewById<ViewGroup>(R.id.appcues_overlay_layout)
        return if (overlayView == null) {
            // then we add
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val binding = AppcuesOverlayLayoutBinding.inflate(inflater)

            // ensures it always comes before appcues_debugger_view
            findViewById<ViewGroup>(R.id.appcues_debugger_view)?.let {
                addView(binding.root, indexOfChild(it))
            } ?: run {
                addView(binding.root)
            }

            binding.root.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                .apply {
                    // adds margin top and bottom according to visible status and navigation bar
                    ViewCompat.getRootWindowInsets(this@getOverlayLayoutBinding)?.getInsets(WindowInsetsCompat.Type.systemBars())
                        ?.let { insets ->
                            setMargins(insets.left, insets.top, insets.right, insets.bottom)
                        }
                }

            binding
        } else {
            AppcuesOverlayLayoutBinding.bind(overlayView)
        }
    }

    private fun ComposeView.setAppcuesComposition(chromeClientView: FrameLayout) {
        setContent {
            AppcuesComposition(
                viewModel = remember { viewModel!! },
                shakeGestureListener = remember { shakeGestureListener!! },
                logcues = scope.get(),
                chromeClient = EmbedChromeClient(chromeClientView),
            )
        }
    }

    private fun onCompositionDismiss() {
        AppcuesActivityMonitor.unsubscribe(this)
        val parentView = AppcuesActivityMonitor.activity?.getParentView()

        parentView?.post {
            parentView.findViewTreeLifecycleOwner()?.lifecycle?.removeObserver(this@RenderViewManager)

            if (renderContext == Modal) {
                parentView.getOverlayLayoutBinding().let {
                    it.appcuesOverlayComposeView.isVisible = false
                    it.appcuesOverlayComposeView.setContent { }
                }
                parentView.setAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES)
            }
        }

        viewModel?.let {
            it.onFinish()
            viewModel = null
        }
    }

    private fun ViewGroup.setAccessibility(accessibilityFlag: Int) {
        children.forEach { child ->
            if (child.id != R.id.appcues_overlay_layout && child.id != R.id.appcues_debugger_view)
                child.importantForAccessibility = accessibilityFlag
        }
    }
}
