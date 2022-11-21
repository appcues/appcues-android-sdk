package com.appcues.ui

import android.app.ActionBar.LayoutParams
import android.util.Log
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.appcues.AppcuesActivityMonitor
import com.appcues.R
import com.appcues.logging.Logcues
import com.appcues.ui.composables.AppcuesComposition
import com.appcues.ui.utils.getRootView
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
        Log.i("Appcues", "ON_RESUME")
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        viewModel?.onPause()
        shakeGestureListener?.stop()
        Log.i("Appcues", "ON_PAUSE")
    }

    fun addView() {
        Log.i("Appcues", "addView")
        AppcuesActivityMonitor.activity?.getRootView()?.also {
            it.findViewTreeLifecycleOwner()?.lifecycle?.addObserver(this@AppcuesOverlayViewManager)
            // if view is not there
            if (it.findViewById<ComposeView>(R.id.appcues_overlay_view) == null) {
                // then we add
                it.addView(
                    ComposeView(it.context).apply {
                        viewModel = AppcuesViewModel(scope)
                        shakeGestureListener = ShakeGestureListener(context)

                        id = R.id.appcues_overlay_view

                        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
                            // adds margin top and bottom according to visible status and navigation bar
                            setMargins(0, context.getStatusBarHeight(), 0, context.getNavigationBarHeight())
                        }

                        setContent {
                            AppcuesComposition(
                                viewModel = remember { viewModel!! },
                                shakeGestureListener = remember { shakeGestureListener!! },
                                logcues = logcues,
                                applySystemMargins = false
                            ) {
                                removeView()
                                viewModel?.onFinish()
                            }
                        }
                    }
                )
            }
        }
    }

    private fun removeView() {
        AppcuesActivityMonitor.activity?.getRootView()?.also {
            it.findViewTreeLifecycleOwner()?.lifecycle?.removeObserver(this@AppcuesOverlayViewManager)
            it.post {
                it.findViewById<ComposeView?>(R.id.appcues_overlay_view)?.run {
                    it.removeView(this)
                }
            }
        }
    }
}
