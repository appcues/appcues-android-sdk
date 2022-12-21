package com.appcues.ui

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.appcues.R
import com.appcues.databinding.AppcuesOverlayLayoutBinding
import com.appcues.logging.Logcues
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.ui.composables.AppcuesComposition
import com.appcues.ui.primitive.EmbedChromeClient
import com.appcues.util.getNavigationBarHeight
import com.appcues.util.getStatusBarHeight
import org.koin.core.scope.Scope

class AppcuesOverlayViewManager(private val scope: Scope) : DefaultLifecycleObserver {

    private val logcues: Logcues by lazy { scope.get() }

    private var viewModel: AppcuesViewModel? = null

    private var shakeGestureListener: ShakeGestureListener? = null

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        viewModel?.onResume()
        shakeGestureListener?.start()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        viewModel?.onPause()
        shakeGestureListener?.stop()
    }

    fun addView() {
        AppcuesActivityMonitor.activity?.run {
            val parentView = getParentView()
            parentView.findViewTreeLifecycleOwner()?.lifecycle?.addObserver(this@AppcuesOverlayViewManager)
            // if view is not there
            if (parentView.findViewById<ComposeView>(R.id.appcues_overlay_layout) == null) {
                // then we add
                val binding = AppcuesOverlayLayoutBinding.inflate(layoutInflater)
                viewModel = AppcuesViewModel(scope)
                shakeGestureListener = ShakeGestureListener(this)

                parentView.addView(binding.root)

                binding.root.layoutParams =
                    FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT).apply {
                        // adds margin top and bottom according to visible status and navigation bar
                        setMargins(0, getStatusBarHeight(), 0, getNavigationBarHeight())
                    }

                binding.appcuesOverlayComposeView.setContent {
                    AppcuesComposition(
                        viewModel = remember { viewModel!! },
                        shakeGestureListener = remember { shakeGestureListener!! },
                        logcues = logcues,
                        chromeClient = EmbedChromeClient(binding.appcuesOverlayCustomViewContainer),
                        onCompositionDismissed = ::onCompositionDismiss
                    )
                }
            }
        }
    }

    private fun onCompositionDismiss() {
        removeView()
        viewModel?.onFinish()
    }

    private fun removeView() {
        AppcuesActivityMonitor.activity?.getParentView()?.also {
            it.findViewTreeLifecycleOwner()?.lifecycle?.removeObserver(this@AppcuesOverlayViewManager)
            it.post {
                it.findViewById<ViewGroup?>(R.id.appcues_overlay_layout)?.run {
                    it.removeView(this)
                }
            }
        }
    }

    private fun Activity.getParentView(): ViewGroup {
        // if there is any difference in API levels we can handle it here
        return window.decorView.rootView as ViewGroup
    }
}
