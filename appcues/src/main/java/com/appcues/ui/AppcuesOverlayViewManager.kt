package com.appcues.ui

import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.appcues.R
import com.appcues.logging.Logcues
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.monitor.AppcuesActivityMonitor.ActivityMonitorListener
import com.appcues.ui.composables.AppcuesComposition
import com.appcues.ui.primitive.EmbedChromeClient
import com.appcues.util.getNavigationBarHeight
import com.appcues.util.getStatusBarHeight
import org.koin.core.scope.Scope

class AppcuesOverlayViewManager(private val scope: Scope) : DefaultLifecycleObserver, ActivityMonitorListener {

    private val logcues: Logcues by lazy { scope.get() }

    private var viewModel: AppcuesViewModel? = null

    private var shakeGestureListener: ShakeGestureListener? = null

    private val handleBackPress = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            viewModel?.onBackPressed()
        }
    }

    override fun onActivityChanged(activity: AppCompatActivity) {
        activity.addView()
    }

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

    fun start() {
        AppcuesActivityMonitor.subscribe(this)

        AppcuesActivityMonitor.activity?.addView()
    }

    fun stop() {
        AppcuesActivityMonitor.unsubscribe(this)

        AppcuesActivityMonitor.activity?.removeView()
    }

    private fun onCompositionDismiss() {
        stop()

        viewModel?.onFinish()
    }

    private fun AppCompatActivity?.addView() {
        if (this == null) return

        onBackPressedDispatcher.addCallback(handleBackPress)

        val parentView = getParentView()
        parentView.findViewTreeLifecycleOwner()?.lifecycle?.addObserver(this@AppcuesOverlayViewManager)
        // if view is not there
        if (parentView.findViewById<ComposeView>(R.id.appcues_overlay_layout) == null) {
            // then we add
            val binding = com.appcues.databinding.AppcuesOverlayLayoutBinding.inflate(layoutInflater)
            if (viewModel == null) viewModel = AppcuesViewModel(scope)
            shakeGestureListener = ShakeGestureListener(this)

            parentView.addView(binding.root)

            binding.root.layoutParams =
                android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                ).apply {
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

    private fun AppCompatActivity?.removeView() {
        handleBackPress.remove()

        if (this == null) return

        getParentView().also {
            it.findViewTreeLifecycleOwner()?.lifecycle?.removeObserver(this@AppcuesOverlayViewManager)
            it.post {
                it.findViewById<ViewGroup?>(R.id.appcues_overlay_layout)?.run {
                    it.removeView(this)
                }
            }
        }
    }

    private fun AppCompatActivity.getParentView(): ViewGroup {
        // if there is any difference in API levels we can handle it here
        return window.decorView.rootView as ViewGroup
    }
}
