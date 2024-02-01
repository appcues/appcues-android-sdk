package com.appcues.debugger.screencapture

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Size
import android.view.PixelCopy
import android.view.View
import android.view.Window
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.appcues.Appcues
import com.appcues.Screenshot
import com.appcues.debugger.screencapture.model.Capture
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.ui.utils.getParentView
import com.appcues.util.withDensity
import kotlinx.coroutines.CompletableDeferred
import java.util.Date

internal class GetCaptureUseCase {

    suspend operator fun invoke(): Capture? {
        val activity = AppcuesActivityMonitor.activity ?: return null
        return activity.getParentView().let {
            val screenshot = Appcues.elementTargeting.captureScreenshot() ?: it.screenshot(activity.window)
            val layout = Appcues.elementTargeting.captureLayout()

            val capture = if (screenshot != null && layout != null) {
                Capture(
                    timestamp = Date(),
                    displayName = it.screenCaptureDisplayName(),
                    screenshot = screenshot,
                    layout = layout,
                    bitmapToUpload = screenshot.getUploadBitmap()
                )
            } else null

            capture
        }
    }

    private fun View.screenCaptureDisplayName(): String {
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
        return name
    }

    // default capture screenshot when the current strategy returns null
    private suspend fun View.screenshot(window: Window): Screenshot? {
        return if (this.width > 0 && this.height > 0) {
            awaitCaptureBitmap(this, window)?.let {
                val insets = ViewCompat.getRootWindowInsets(this)?.getInsets(WindowInsetsCompat.Type.systemBars()) ?: Insets.NONE

                Screenshot(
                    bitmap = it,
                    size = withDensity { Size(width, height).toDp() },
                    insets = withDensity { insets.toDp() }
                )
            }
        } else {
            null
        }
    }

    // converts async function (captureBitmap) to a suspend function that will await for completion
    private suspend fun awaitCaptureBitmap(view: View, window: Window): Bitmap? {
        return with(CompletableDeferred<Bitmap?>()) {
            captureBitmap(view, window) { complete(it) }

            await()
        }
    }

    private fun captureBitmap(view: View, window: Window, bitmapCallback: (Bitmap?) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Above Android O, use PixelCopy
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val location = IntArray(2)
            view.getLocationInWindow(location)

            try {
                PixelCopy.request(
                    window,
                    Rect(location[0], location[1], location[0] + view.width, location[1] + view.height),
                    bitmap,
                    {
                        if (it == PixelCopy.SUCCESS) {
                            bitmapCallback.invoke(bitmap)
                        }
                    },
                    Handler(Looper.getMainLooper())
                )
            } catch (_: IllegalArgumentException) {
                // if something failed with PixelCopy for any reason, try legacy bitmap creation
                captureLegacyBitmap(view, bitmapCallback)
            }
        } else {
            captureLegacyBitmap(view, bitmapCallback)
        }
    }

    private fun captureLegacyBitmap(view: View, bitmapCallback: (Bitmap?) -> Unit) {
        try {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            canvas.setBitmap(null)
            bitmapCallback.invoke(bitmap)
        } catch (_: IllegalArgumentException) {
            bitmapCallback.invoke(null)
        }
    }

    // since original bitmap is measured in pixels, we want to scale it down based on
    // density values so we upload a smaller image that builder can use
    private fun Screenshot.getUploadBitmap(): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, size.width, size.height, true)
    }
}
