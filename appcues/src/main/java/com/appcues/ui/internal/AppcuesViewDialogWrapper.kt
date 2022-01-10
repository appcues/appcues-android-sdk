package com.appcues.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Outline
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.ViewRootForInspector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import com.appcues.R
import java.util.UUID

@Composable
internal fun AppcuesView(
    dispatchTouchEvent: (MotionEvent) -> Unit,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val properties = AppcuesDialogProperties()
    val composition = rememberCompositionContext()
    val currentContent by rememberUpdatedState(content)
    val dialogId = rememberSaveable { UUID.randomUUID() }
    val dialog = remember(view, density) {
        AppcuesViewDialogWrapper(
            properties,
            view,
            dispatchTouchEvent,
            layoutDirection,
            density,
            dialogId
        ).apply {
            setContent(composition) {
                currentContent()
            }
        }
    }

    DisposableEffect(dialog) {
        dialog.show()

        onDispose {
            dialog.dismiss()
            dialog.disposeComposition()
        }
    }

    SideEffect {
        dialog.updateParameters(
            properties = properties,
            layoutDirection = layoutDirection
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private class AppcuesViewDialogWrapper(
    private var properties: AppcuesDialogProperties,
    private val composeView: View,
    dispatchTouchEvent: (MotionEvent) -> Unit,
    layoutDirection: LayoutDirection,
    density: Density,
    dialogId: UUID
) : Dialog(ContextThemeWrapper(composeView.context, R.style.Appcues_DialogWrapperTheme)), ViewRootForInspector {

    private val dialogLayout: DialogLayout

    private val maxSupportedElevation = 30.dp

    override val subCompositionView: AbstractComposeView get() = dialogLayout

    private fun requireWindow(): Window {
        return window ?: error("AppcuesViewDialogWrapper has no window")
    }

    init {
        requireWindow().setBackgroundDrawableResource(android.R.color.transparent)
        //window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED)
        dialogLayout = DialogLayout(context).apply {
            // Set unique id for AbstractComposeView. This allows state restoration for the state
            // defined inside the Dialog via rememberSaveable()
            setTag(R.id.compose_view_saveable_id_tag, "FullScreenDialogWrapper:$dialogId")
            // Enable children to draw their shadow by not clipping them
            clipChildren = false
            // Allocate space for elevation
            with(density) { elevation = maxSupportedElevation.toPx() }
            // Simple outline to force window manager to allocate space for shadow.
            // Note that the outline affects clickable area for the dismiss listener. In case of
            // shapes like circle the area for dismiss might be to small (rectangular outline
            // consuming clicks outside of the circle).
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, result: Outline) {
                    result.setRect(0, 0, view.width, view.height)
                    // We set alpha to 0 to hide the view's shadow and let the composable to draw
                    // its own shadow. This still enables us to get the extra space needed in the
                    // surface.
                    result.alpha = 0f
                }
            }

            // Set target parent to receive all clicks from this Dialog
            setDispatchTouchEvent(dispatchTouchEvent)
        }

        /**
         * Disables clipping for [this] and all its descendant [ViewGroup]s until we reach a
         * [DialogLayout] (the [ViewGroup] containing the Compose hierarchy).
         */
        fun ViewGroup.disableClipping() {
            clipChildren = false
            if (this is DialogLayout) return
            for (i in 0 until childCount) {
                (getChildAt(i) as? ViewGroup)?.disableClipping()
            }
        }

        // Turn of all clipping so shadows can be drawn outside the window
        (requireWindow().decorView as? ViewGroup)?.disableClipping()

        // Initial setup
        setContentView(dialogLayout)
        ViewTreeLifecycleOwner.set(dialogLayout, ViewTreeLifecycleOwner.get(composeView))
        ViewTreeViewModelStoreOwner.set(dialogLayout, ViewTreeViewModelStoreOwner.get(composeView))
        ViewTreeSavedStateRegistryOwner.set(dialogLayout, ViewTreeSavedStateRegistryOwner.get(composeView))
        updateParameters(properties, layoutDirection)
    }

    private fun setLayoutDirection(layoutDirection: LayoutDirection) {
        dialogLayout.layoutDirection = when (layoutDirection) {
            LayoutDirection.Ltr -> android.util.LayoutDirection.LTR
            LayoutDirection.Rtl -> android.util.LayoutDirection.RTL
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // set window to MATCH_PARENT width and height
        requireWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
    }

    fun setContent(parentComposition: CompositionContext, children: @Composable () -> Unit) {
        dialogLayout.setContent(parentComposition, children)
    }

    private fun setSecurePolicy() {
        composeView.isFlagSecureEnabled().run {
            val secureFlag = if (this) WindowManager.LayoutParams.FLAG_SECURE else WindowManager.LayoutParams.FLAG_SECURE.inv()
            requireWindow().setFlags(secureFlag, WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    private fun View.isFlagSecureEnabled(): Boolean {
        val windowParams = rootView.layoutParams as? WindowManager.LayoutParams
        if (windowParams != null) {
            return (windowParams.flags and WindowManager.LayoutParams.FLAG_SECURE) != 0
        }
        return false
    }

    fun updateParameters(
        properties: AppcuesDialogProperties,
        layoutDirection: LayoutDirection
    ) {
        this.properties = properties
        setSecurePolicy()
        setLayoutDirection(layoutDirection)
    }

    fun disposeComposition() {
        dialogLayout.disposeComposition()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event).also {
            if (it) {
                dismiss()
            }
        }
    }

    override fun cancel() {
        // Prevents the dialog from dismissing itself
        return
    }

    override fun onBackPressed() {
        if (properties.dismissOnBackPress) {
            dismiss()
        }
    }
}

private data class AppcuesDialogProperties(
    val dismissOnBackPress: Boolean = true
)

private class DialogLayout(
    context: Context
) : AbstractComposeView(context) {

    private var content: @Composable () -> Unit by mutableStateOf({})

    override var shouldCreateCompositionOnAttachedToWindow: Boolean = false
        private set

    fun setContent(parent: CompositionContext, content: @Composable () -> Unit) {
        setParentCompositionContext(parent)
        this.content = content
        shouldCreateCompositionOnAttachedToWindow = true
        createComposition()

    }

    fun setDispatchTouchEvent(dispatchTouchEvent: (MotionEvent) -> Unit) {
        rootView.setOnTouchListener { v, event ->
            dispatchTouchEvent(event)
            v.performClick()
        }
    }

    @Composable
    override fun Content() {
        content()
    }
}
