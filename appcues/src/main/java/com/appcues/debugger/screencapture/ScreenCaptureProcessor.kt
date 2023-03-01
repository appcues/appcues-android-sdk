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
import com.appcues.data.remote.RemoteError
import com.appcues.data.remote.customerapi.CustomerApiBaseUrlInterceptor
import com.appcues.data.remote.customerapi.CustomerApiRemoteSource
import com.appcues.data.remote.customerapi.response.PreUploadScreenshotResponse
import com.appcues.data.remote.imageupload.ImageUploadRemoteSource
import com.appcues.data.remote.sdksettings.SdkSettingsRemoteSource
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.ui.ElementSelector
import com.appcues.util.ContextResources
import com.appcues.util.ResultOf
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.util.Date

internal class ScreenCaptureProcessor(
    private val config: AppcuesConfig,
    private val contextResources: ContextResources,
    private val sdkSettingsRemoteSource: SdkSettingsRemoteSource,
    private val customerApiRemoteSource: CustomerApiRemoteSource,
    private val imageUploadRemoteSource: ImageUploadRemoteSource,
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
                    metadata = contextResources.generateCaptureMetadata(it),
                    timestamp = timestamp,
                ).apply {
                    this.screenshot = screenshot
                }
            } else null

            restore(it)

            capture
        }
    }

    suspend fun save(capture: Capture, token: String): ResultOf<Capture, RemoteError> {
        // Saving a screen is a 4-step chain. This is implemented here as a sequence of calls, chaining
        // a success continuation on each call to move to the next step. If any step fails, the RemoteError
        // is bubbled up to the caller of this function instead of the successful result, and an error
        // can be shown and handled.
        return try {
            // step 1 - get the settings, with the path to customer API
            configureCustomerApi()
                // step 2 - use the customer API path to get the link to upload the screenshot
                .getUploadPath(capture, token)
                // step 3 - upload the screenshot
                .uploadImage(capture)
                // step 4 - update the screenshot link and save the screen
                .saveScreen(token)
                .let { Success(it) }
        } catch (e: ScreenCaptureSaveException) {
            Failure(e.error)
        }
    }

    // step 1 - get the settings, with the path to customer API
    private suspend fun configureCustomerApi(): CustomerApiRemoteSource {
        return when (val settings = sdkSettingsRemoteSource.sdkSettings()) {
            is Failure -> throw ScreenCaptureSaveException(settings.reason)
            is Success -> {
                CustomerApiBaseUrlInterceptor.baseUrl = settings.value.services.customerApi.toHttpUrl()
                customerApiRemoteSource
            }
        }
    }

    // step 2 - use the customer API path to get the link to upload the screenshot
    private suspend fun CustomerApiRemoteSource.getUploadPath(capture: Capture, token: String): PreUploadScreenshotResponse {
        return when (val preUploadResponse = preUploadScreenshot(capture, token)) {
            is Failure -> throw ScreenCaptureSaveException(preUploadResponse.reason)
            is Success -> preUploadResponse.value
        }
    }

    // step 3 - upload the screenshot
    private suspend fun PreUploadScreenshotResponse.uploadImage(capture: Capture): Capture {

        val density = contextResources.displayMetrics.density
        val original = capture.screenshot
        val bitmap = Bitmap.createScaledBitmap(
            capture.screenshot,
            original.width.toDp(density),
            original.height.toDp(density),
            true
        )
        return when (
            val uploadResponse = imageUploadRemoteSource.upload(this.upload.presignedUrl, bitmap)
        ) {
            is Failure -> throw ScreenCaptureSaveException(uploadResponse.reason)
            is Success -> capture.copy(screenshotImageUrl = this.url)
        }
    }

    // step 4 - update the screenshot link and save the screen
    private suspend fun Capture.saveScreen(token: String) =
        when (val screenResult = customerApiRemoteSource.screen(this, token)) {
            is Failure -> throw ScreenCaptureSaveException(screenResult.reason)
            is Success -> this
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
    if (Rect.intersects(actualPosition, screenRect).not()) {
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
        x = actualPosition.left.toDp(density),
        y = actualPosition.top.toDp(density),
        width = actualPosition.width().toDp(density),
        height = actualPosition.height().toDp(density),
        selector = if (selector.isValid) selector else null,
        type = this.javaClass.name,
        children = children,
    )
}

private fun View.screenshot() =
    if (this.width > 0 && this.height > 0) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        this.draw(canvas)
        bitmap
    } else null

private class ScreenCaptureSaveException(val error: RemoteError) : Exception("ScreenCaptureException: $error")
