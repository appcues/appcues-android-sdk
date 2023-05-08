package com.appcues.debugger.screencapture

import android.content.res.Resources.NotFoundException
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.appcues.ElementSelector
import com.appcues.ElementTargetingStrategy
import com.appcues.ViewElement
import com.appcues.isAppcuesView
import com.appcues.monitor.AppcuesActivityMonitor

internal data class AndroidViewSelector(
    var contentDescription: String? = null,
    var tag: String? = null,
    var resourceName: String? = null
) : ElementSelector {
    val isValid: Boolean
        get() = contentDescription != null || tag != null || resourceName != null

    override fun toMap(): Map<String, String> {
        return mapOf(
            "contentDescription" to contentDescription,
            "tag" to tag,
            "resourceName" to resourceName,
        ).filterValues { it != null }.mapValues { it.value as String }
    }

    @Suppress("MagicNumber")
    override fun evaluateMatch(target: ElementSelector): Int {
        var weight = 0

        (target as? AndroidViewSelector)?.let {
            if (it.resourceName != null && it.resourceName == resourceName) {
                weight += 1_000
            }

            if (it.tag != null && it.tag == tag) {
                weight += 100
            }

            if (it.contentDescription != null && it.contentDescription == contentDescription) {
                weight += 10
            }
        }

        return weight
    }
}

internal class AndroidTargetingStrategy : ElementTargetingStrategy {

    override fun captureLayout(): ViewElement? {
        return AppcuesActivityMonitor.activity?.window?.decorView?.rootView?.asCaptureView()
    }

    override fun inflateSelectorFrom(properties: Map<String, String>): ElementSelector? {
        val selector = AndroidViewSelector(
            contentDescription = properties["contentDescription"],
            tag = properties["tag"],
            resourceName = properties["resourceName"],
        )
        return if (selector.isValid) selector else null
    }
}

private fun View.asCaptureView(): ViewElement? {
    val displayMetrics = context.resources.displayMetrics
    val density = displayMetrics.density

    // this is the position of the view relative to the entire screen
    val actualPosition = Rect()
    getGlobalVisibleRect(actualPosition)

    // the bounds of the screen
    val screenRect = Rect(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)

    // if the view is not currently in the screenshot image (scrolled away), ignore
    if (Rect.intersects(actualPosition, screenRect).not()) {
        return null
    }

    // ignore the Appcues SDK content that has been injected into the view hierarchy
    if (this.isAppcuesView()) {
        return null
    }

    var children = (this as? ViewGroup)?.children?.mapNotNull {
        if (!it.isShown) {
            // discard hidden views and subviews within
            null
        } else {
            it.asCaptureView()
        }
    }?.toList()

    if (children?.isEmpty() == true) {
        children = null
    }

    return ViewElement(
        x = actualPosition.left.toDp(density),
        y = actualPosition.top.toDp(density),
        width = actualPosition.width().toDp(density),
        height = actualPosition.height().toDp(density),
        selector = selector(),
        type = this.javaClass.name,
        children = children,
    )
}
internal fun View.selector(): ElementSelector? {
    @Suppress("SwallowedException")
    val resourceName: String? = try {
        if (this.isClickable) resources.getResourceEntryName(id) else null
    } catch (ex: NotFoundException) {
        null
    }
    val selector = AndroidViewSelector(
        contentDescription = this.contentDescription?.toString(),
        tag = tag?.toString(),
        resourceName = resourceName,
    )

    return if (selector.isValid) selector else null
}
