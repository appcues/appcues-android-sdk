package com.appcues.ui.presentation

import android.app.Activity
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.appcues.AppcuesCoroutineScope
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperienceTrigger.Preview
import com.appcues.data.model.RenderContext
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.monitor.AppcuesActivityMonitor.ActivityMonitorListener
import com.appcues.ui.ExperienceRenderer
import com.appcues.ui.composables.AppcuesComposition
import com.appcues.ui.primitive.EmbedChromeClient
import com.appcues.ui.utils.getParentView
import kotlinx.coroutines.launch
import org.koin.core.scope.Scope

internal abstract class ViewPresenter(
    private val scope: Scope,
    protected val renderContext: RenderContext,
) : DefaultLifecycleObserver {

    private lateinit var viewModel: AppcuesViewModel

    private val experienceRenderer: ExperienceRenderer = scope.get()
    private val appcuesCoroutineScope: AppcuesCoroutineScope = scope.get()

    private var gestureListener: ShakeGestureListener? = null

    private val currentExperience: Experience?
        get() = experienceRenderer.getState(renderContext)?.currentExperience

    private val activityMonitorListener = object : ActivityMonitorListener {
        override fun onActivityChanged(activity: Activity) {
            viewModel.onActivityChanged()
        }
    }

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            gestureListener?.start()
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            gestureListener?.stop()
        }
    }

    fun present(): Boolean {
        val activity = AppcuesActivityMonitor.activity

        // inform the caller that we cannot currently add a view in the app
        if (activity == null || AppcuesActivityMonitor.isPaused) return false

        AppcuesActivityMonitor.subscribe(activityMonitorListener)

        activity.getParentView().run {
            // grab composeView or exit with false
            val composeView = setupView(activity) ?: return false

            viewModel = AppcuesViewModel(scope, renderContext, ::onCompositionDismiss)

            if (currentExperience?.trigger == Preview) {
                gestureListener = ShakeGestureListener(activity).also {
                    it.addListener(true) { refreshPreview() }
                }
            }

            findViewTreeLifecycleOwner()?.lifecycle?.addObserver(lifecycleObserver)

            composeView.setContent {
                AppcuesComposition(
                    viewModel = viewModel,
                    logcues = scope.get(),
                    chromeClient = EmbedChromeClient(this),
                )
            }

            return true
        }
    }

    fun remove() {
        onCompositionDismiss()
    }

    private fun onCompositionDismiss() {
        AppcuesActivityMonitor.unsubscribe(activityMonitorListener)

        gestureListener?.clearListener()
        gestureListener = null

        AppcuesActivityMonitor.activity?.getParentView()?.run {
            findViewTreeLifecycleOwner()?.lifecycle?.removeObserver(lifecycleObserver)

            post {
                removeView()

                viewModel.onFinish()
            }
        }
    }

    abstract fun ViewGroup.setupView(activity: Activity): ComposeView?

    abstract fun ViewGroup.removeView()

    private fun refreshPreview() {
        currentExperience?.let {
            appcuesCoroutineScope.launch {
                experienceRenderer.preview(it.id.toString())
            }
        }
    }
}
