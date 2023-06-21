package com.appcues.ui

import android.app.Activity
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.View
import android.view.ViewGroup
import android.view.inspector.WindowInspector
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.appcues.R
import com.appcues.databinding.AppcuesOverlayLayoutBinding
import com.appcues.logging.Logcues
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.monitor.AppcuesActivityMonitor.ActivityMonitorListener
import com.appcues.ui.composables.AppcuesComposition
import com.appcues.ui.primitive.EmbedChromeClient
import org.koin.core.scope.Scope
import java.lang.reflect.Method

internal class AppcuesOverlayViewManager(private val scope: Scope) : DefaultLifecycleObserver, ActivityMonitorListener {

    private val logcues: Logcues by lazy { scope.get() }

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
        if (activity == null || AppcuesActivityMonitor.isPaused) {
            return false
        }

        AppcuesActivityMonitor.subscribe(this)
        activity.addView()
        return true
    }

    private fun onCompositionDismiss() {
        AppcuesActivityMonitor.unsubscribe(this)
        AppcuesActivityMonitor.activity?.removeView()
        viewModel?.onFinish()
    }

    private fun Activity?.addView() {
        if (this == null) return

        val parentView = getParentView()
        parentView.findViewTreeLifecycleOwner()?.lifecycle?.addObserver(this@AppcuesOverlayViewManager)
        // if view is not there
        if (parentView.findViewById<ComposeView>(R.id.appcues_overlay_layout) == null) {
            // then we add
            val binding = AppcuesOverlayLayoutBinding.inflate(layoutInflater)
            if (viewModel == null) viewModel = AppcuesViewModel(scope, ::onCompositionDismiss)
            shakeGestureListener = ShakeGestureListener(this)

            // ensures it always comes before appcues_debugger_view
            parentView.findViewById<ViewGroup>(R.id.appcues_debugger_view)?.let {
                parentView.addView(binding.root, parentView.indexOfChild(it))
            } ?: run {
                parentView.addView(binding.root)
            }

            val insets = ViewCompat.getRootWindowInsets(parentView)?.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.root.layoutParams =
                android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    // adds margin top and bottom according to visible status and navigation bar
                    ViewCompat.getRootWindowInsets(parentView)?.getInsets(WindowInsetsCompat.Type.systemBars())?.let { insets ->
                        setMargins(insets.left, insets.top, insets.right, insets.bottom)
                    }
                }

            binding.appcuesOverlayComposeView.setContent {
                AppcuesComposition(
                    viewModel = remember { viewModel!! },
                    shakeGestureListener = remember { shakeGestureListener!! },
                    logcues = logcues,
                    chromeClient = EmbedChromeClient(binding.appcuesOverlayCustomViewContainer),
                )
            }

            // remove customers view on accessibility stack
            parentView.setAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS)
        }
    }

    private fun Activity?.removeView() {
        if (this == null) return

        getParentView().also {
            it.findViewTreeLifecycleOwner()?.lifecycle?.removeObserver(this@AppcuesOverlayViewManager)
            it.post {
                it.findViewById<ViewGroup?>(R.id.appcues_overlay_layout)?.run {
                    it.removeView(this)
                }

                // remove customers view on accessibility stack
                it.setAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES)
            }
        }
    }

    private fun ViewGroup.setAccessibility(accessibilityFlag: Int) {
        children.forEach { child ->
            if (child.id != R.id.appcues_overlay_layout && child.id != R.id.appcues_debugger_view)
                child.importantForAccessibility = accessibilityFlag
        }
    }
}

internal fun Activity.getParentView(): ViewGroup {

    // try to find the most applicable decorView to inject Appcues content into. Typically there is just a single
    // decorView on the Activity window. However, if something like a dialog modal has been shown, this can add another
    // window with another decorView on top of the Activity. If we want to support showing content above that layer, we need
    // to find the top most decorView like below.
    
    val decorView = if (VERSION.SDK_INT >= VERSION_CODES.Q) {
        // this is the preferred method on API 29+ with the new WindowInspector function
        WindowInspector.getGlobalWindowViews().last()
    } else {
        @Suppress("SwallowedException", "TooGenericExceptionCaught")
        try {
            // this is the less desirable method for API 21-28, using reflection to try to get the root views
            val windowManagerClass = Class.forName("android.view.WindowManagerGlobal")
            val windowManager = windowManagerClass.getMethod("getInstance").invoke(null)
            val getViewRootNames: Method = windowManagerClass.getMethod("getViewRootNames")
            val getRootView: Method = windowManagerClass.getMethod("getRootView", String::class.java)
            val rootViewNames = getViewRootNames.invoke(windowManager) as Array<Any?>
            val rootViews = rootViewNames.map { getRootView(windowManager, it) as View }
            rootViews.last()
        } catch (_: Exception) {
            // if all else fails, use the decorView on the window, which is typically the only one
            window.decorView
        }
    }

    // This is the case of some other decorView showing on top - like a modal
    // dialog. In this case, we need to apply the fix-ups below to ensure that our
    // content can render correctly inside of this other view. In each case, we use
    // the applicable value from the Activity default window.
    if (ViewTreeLifecycleOwner.get(decorView) == null) {
        val lifecycleOwner = window.decorView.findViewTreeLifecycleOwner()
        ViewTreeLifecycleOwner.set(decorView, lifecycleOwner)
    }

    if (ViewTreeViewModelStoreOwner.get(decorView) == null) {
        val viewModelStoreOwner = window.decorView.findViewTreeViewModelStoreOwner()
        ViewTreeViewModelStoreOwner.set(decorView, viewModelStoreOwner)
    }

    if (decorView.findViewTreeSavedStateRegistryOwner() == null) {
        val savedStateRegistryOwner = window.decorView.findViewTreeSavedStateRegistryOwner()
        decorView.setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
    }

    return decorView.rootView as ViewGroup
}
