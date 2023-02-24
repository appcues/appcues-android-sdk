package com.appcues.debugger.screencapture

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.text.format.DateFormat
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.appcues.AppcuesConfig
import com.appcues.R
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.ui.ElementSelector
import com.appcues.util.ContextResources
import com.appcues.util.ResultOf
import com.appcues.util.ResultOf.Success
import java.util.Date

internal class ScreenCaptureProcessor(
    private val config: AppcuesConfig,
    private val contextResources: ContextResources,
) {

    fun captureScreen(): Capture? {
        return AppcuesActivityMonitor.activity?.window?.decorView?.rootView?.let {
            prepare(it)

            val timestamp = Date()
            val displayName = it.screenCaptureDisplayName(timestamp)
            val screenshot = it.screenshot()
            val layout = it.asCaptureView()
            val capture = if (screenshot != null && layout != null) {
                Capture(
                    appId = config.applicationId,
                    displayName = displayName,
                    screenshotImageUrl = null,
                    layout = layout,
                    metadata = contextResources.generateCaptureMetadata(),
                    timestamp = timestamp,
                ).apply {
                    this.screenshot = screenshot
                }
            } else null

            restore(it)

            capture
        }
    }

    suspend fun save(capture: Capture): ResultOf<Capture, Error> {

        // upcoming work to execute API calls starts here

        return Success(capture)
    }

    private fun prepare(view: View) {
        // hide the debugger view for screen capture, if present
        val debuggerView = view.findViewById<View>(R.id.appcues_debugger_view)
        debuggerView?.let { it ->
            it.visibility = View.GONE
        }
    }

    private fun restore(view: View) {
        // hide the debugger view for screen capture, if present
        val debuggerView = view.findViewById<View>(R.id.appcues_debugger_view)
        debuggerView?.let { it ->
            it.visibility = View.VISIBLE
        }
    }
}

private fun View.screenCaptureDisplayName(timestamp: Date): String {
    var name: String
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

private fun View.asCaptureView(): Capture.View? {
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

private fun View.screenshot() =
    if (this.width > 0 && this.height > 0) {
        val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        this.draw(canvas)
        bitmap
    } else null
