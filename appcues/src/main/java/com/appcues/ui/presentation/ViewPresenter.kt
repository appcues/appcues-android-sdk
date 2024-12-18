package com.appcues.ui.presentation

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.findViewTreeOnBackPressedDispatcherOwner
import androidx.compose.runtime.key
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.appcues.AppcuesConfig
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperienceTrigger.Preview
import com.appcues.data.model.RenderContext
import com.appcues.di.component.AppcuesComponent
import com.appcues.di.component.get
import com.appcues.di.component.inject
import com.appcues.di.scope.AppcuesScope
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.monitor.AppcuesActivityMonitor.ActivityMonitorListener
import com.appcues.ui.ExperienceRenderer
import com.appcues.ui.composables.AppcuesComposition
import com.appcues.ui.presentation.AppcuesViewModel.PresentationBinding
import com.appcues.ui.primitive.EmbedChromeClient
import com.appcues.ui.utils.getParentView
import com.appcues.util.AppcuesViewTreeOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

internal abstract class ViewPresenter(
    override val scope: AppcuesScope,
    protected val renderContext: RenderContext,
) : AppcuesComponent {

    private val experienceRenderer: ExperienceRenderer by inject()
    private val coroutineScope: CoroutineScope by inject()
    private val appcuesViewTreeOwner: AppcuesViewTreeOwner by inject()
    private val appcuesConfig: AppcuesConfig by inject()

    private var parentView: WeakReference<ViewGroup> = WeakReference(null)
    private var viewModel: AppcuesViewModel? = null
    private var gestureListener: ShakeGestureListener? = null

    private val currentExperience: Experience?
        get() = experienceRenderer.getState(renderContext)?.currentExperience

    private val activityMonitorListener = object : ActivityMonitorListener {
        override fun onActivityChanged(activity: Activity) {
            viewModel?.onActivityChanged()
        }

        override fun onConfigurationChanged(activity: Activity) {
            viewModel?.onConfigurationChanged()
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

    private val binding = object : PresentationBinding {
        override val renderContext = this@ViewPresenter.renderContext

        override fun onDismiss() = remove()

        override fun onTap(offset: Offset) = tapPassThrough(offset)
    }

    fun present(): Boolean {
        val activity = AppcuesActivityMonitor.activity

        // inform the caller that we cannot currently add a view in the app
        if (activity == null || AppcuesActivityMonitor.isPaused) return false

        AppcuesActivityMonitor.subscribe(activityMonitorListener)

        activity.getParentView().run {
            // grab composeView or exit with false
            val composeView = setupView(activity) ?: return false

            if (currentExperience?.trigger == Preview) {
                gestureListener = ShakeGestureListener(activity).also {
                    it.addListener(true) { refreshPreview() }
                }
            }

            appcuesViewTreeOwner.init(this, activity)
            setLifecycleObserver(this)
            setOnBackPressDispatcher(this)

            // storing current viewGroup to be re-used during dismiss/remove since getParentView
            // can return another viewGroup depending the situation (eg. One activity calling another on top)
            parentView = WeakReference(this)

            viewModel = AppcuesViewModel(
                presentationBinding = binding,
                coroutineScope = coroutineScope,
                experienceRenderer = experienceRenderer,
                actionProcessor = get(),
                analyticsTracker = get(),
            ).also {
                composeView.setContent {
                    // [currentExperience?.instanceId]: when the instanceId changes it means it could be a "newer" version
                    // of the same experience, instanceId relates to the request/response for a particular qualification
                    // or previewing of a flow.
                    // [LocalLayoutDirection.current]: when this configuration is changed the composition should be discarded
                    // and re-built. it was a problem detected from the react-native-sdk when for some reason the OS emits
                    // multiple layout direction changes when the composition is added to the view.
                    key(currentExperience?.instanceId, LocalLayoutDirection.current) {
                        AppcuesComposition(
                            viewModel = it,
                            logcues = get(),
                            imageLoader = get(),
                            chromeClient = EmbedChromeClient(this),
                            packageNames = appcuesConfig.packageNames
                        )
                    }
                }
            }
            return true
        }
    }

    fun remove() {
        // ensure all this is running on the main thread
        postIfNeeded {
            parentView.get()?.let { view ->
                view.findViewTreeLifecycleOwner()?.lifecycle?.removeObserver(lifecycleObserver)
                view.removeView()
            }

            AppcuesActivityMonitor.unsubscribe(activityMonitorListener)
            onBackPressCallback.remove()
            gestureListener?.clearListener()
            gestureListener = null
            viewModel = null
            parentView = WeakReference(null)
        }
    }

    private fun tapPassThrough(offset: Offset) {
        parentView.get()?.let { view ->
            val now = System.currentTimeMillis()
            val downEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, offset.x, offset.y, 0)
            val upEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_UP, offset.x, offset.y, 0)

            // Dispatch both ACTION_DOWN and ACTION_UP events to simulate a click
            view.dispatchTouchEvent(downEvent)
            view.dispatchTouchEvent(upEvent)

            downEvent.recycle()
            upEvent.recycle()
        }
    }

    abstract val shouldHandleBack: Boolean

    abstract fun ViewGroup.setupView(activity: Activity): ComposeView?

    abstract fun ViewGroup.removeView()

    private fun postIfNeeded(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // already on main
            block()
        } else {
            Handler(Looper.getMainLooper()).post {
                block()
            }
        }
    }

    private fun refreshPreview() {
        currentExperience?.let {
            coroutineScope.launch {
                experienceRenderer.preview(it.id.toString(), it.previewQuery)
            }
        }
    }

    private fun setLifecycleObserver(view: ViewGroup) {
        view.findViewTreeLifecycleOwner()?.run {
            lifecycle.addObserver(lifecycleObserver)
        }
    }

    private fun setOnBackPressDispatcher(view: ViewGroup) {
        if (shouldHandleBack) {
            view.findViewTreeOnBackPressedDispatcherOwner()?.run {
                onBackPressCallback.remove()

                onBackPressedDispatcher.addCallback(onBackPressCallback)
            }
        }
    }

    private val onBackPressCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            viewModel?.requestDismissal()
        }
    }
}
