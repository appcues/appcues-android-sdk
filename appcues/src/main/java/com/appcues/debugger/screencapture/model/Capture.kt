package com.appcues.debugger.screencapture.model

import android.graphics.Bitmap
import com.appcues.Screenshot
import com.appcues.ViewElement
import java.util.Date
import java.util.UUID

internal data class Capture(
    val id: UUID = UUID.randomUUID(),
    val timestamp: Date,
    val displayName: String,
    val screenshot: Screenshot,
    val layout: ViewElement,
    val bitmapToUpload: Bitmap,
) {

    val targetableElementCount: Int = getTargetableElementCount(layout)

    private fun getTargetableElementCount(element: ViewElement): Int {
        val selectableElement = if (element.selector != null) 1 else 0

        return selectableElement + (element.children?.sumOf { getTargetableElementCount(it) } ?: 0)
    }
}
