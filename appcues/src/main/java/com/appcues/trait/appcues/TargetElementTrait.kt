package com.appcues.trait.appcues

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfig
import com.appcues.data.model.getConfigObject
import com.appcues.data.model.getConfigOrDefault
import com.appcues.debugger.screencapture.selector
import com.appcues.debugger.screencapture.toDp
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.trait.AppcuesTraitException
import com.appcues.trait.MetadataSettingTrait
import com.appcues.ui.ElementSelector

internal class TargetElementTrait(
    override val config: AppcuesConfigMap,
) : MetadataSettingTrait {

    companion object {
        const val TYPE = "@appcues/target-element"
    }

    private val selector: ElementSelector? = config.getConfigObject("selector")
    private val contentDistance = config.getConfigOrDefault("contentDistanceFromTarget", 0.0)
    private val preferredPosition = config.getConfig<String>("contentPreferredPosition").toPosition()

    override fun produceMetadata(): Map<String, Any?> {
        val rootView = AppcuesActivityMonitor.activity?.window?.decorView?.rootView
            ?: throw AppcuesTraitException("could not find root view")

        val selectedView = rootView.findMatch(selector)
            ?: throw AppcuesTraitException("could not find view matching selector ${selector ?: "(null)"}")

        val insets = ViewCompat.getRootWindowInsets(rootView)?.getInsets(WindowInsetsCompat.Type.systemBars()) ?: Insets.NONE

        // this is the position of the view relative to the entire screen
        val actualPosition = Rect()
        selectedView.getGlobalVisibleRect(actualPosition)

        // this global position includes system bars, so we need to subtract those insets to get coordinates relative to the root
        // view of what we are drawing content on top of
        val x = actualPosition.left - insets.left
        val y = actualPosition.top - insets.top

        val displayMetrics = selectedView.context.resources.displayMetrics
        val density = displayMetrics.density

        val targetRectangle = TargetRectangleInfo(
            x = x.toDp(density).toFloat(),
            y = y.toDp(density).toFloat(),
            width = actualPosition.width().toDp(density).toFloat(),
            height = actualPosition.height().toDp(density).toFloat(),
            contentDistance = contentDistance,
            prefPosition = preferredPosition,
        )

        return hashMapOf(TARGET_RECTANGLE_METADATA to targetRectangle)
    }
}

private fun View.findMatch(selector: ElementSelector?): View? {
    return selector?.let {
        val views = viewsMatchingSelector(selector)
        views.firstOrNull()
    }
}

private fun View.viewsMatchingSelector(selector: ElementSelector): List<View> {
    val views = mutableListOf<View>()

    this.selector()?.let {
        if (it.hasAnyMatch(selector)) {
            views.add(this)
        }
    }

    (this as? ViewGroup)?.children?.forEach {
        views.addAll(it.viewsMatchingSelector(selector))
    }

    return views
}
