package com.appcues.debugger.screencapture

import android.graphics.Rect
import android.text.format.DateFormat
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.appcues.R
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.ui.ElementSelector
import java.util.Date

internal fun View.screenCaptureDisplayName(timestamp: Date): String {
    var name = ""
    val activity = AppcuesActivityMonitor.activity
    if (activity != null) {
        name = activity.javaClass.simpleName
        if (name != "Activity") {
            name = name.replace("Activity", "")
        }
    } else {
        name = this.javaClass.simpleName
    }
    return "$name (${DateFormat.format("yyyy-MM-dd_HH:mm:ss", timestamp)})"
}

internal fun View.asCaptureView(): Capture.View? {
    val displayMetrics = context.resources.displayMetrics
    val density = displayMetrics.density

    // this is the position of the view relative to the entire screen
    val actualPosition = Rect()
    getGlobalVisibleRect(actualPosition)

    // the bounds of the screen
    val screenRect = Rect(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)

    // if the view is not currently in the screenshot image (scrolled away), ignore
    if (!actualPosition.intersect(screenRect)) {
        return null
    }

    // ignore the Appcues SDK content that has been injected into the view hierarchy
    if (this.id == R.id.appcues_debugger_view) {
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

    val selector = ElementSelector(
        id = null, // for manual tagging
        accessibilityIdentifier = null, // not valid on android
        description = this.contentDescription?.toString(),
        tag = tag?.toString()
    )

    return Capture.View(
        x = actualPosition.left / density,
        y = actualPosition.top / density,
        width = actualPosition.width() / density,
        height = actualPosition.height() / density,
        selector = if (selector.isValid) selector else null,
        type = this.javaClass.name,
        children = children,
    )
}
