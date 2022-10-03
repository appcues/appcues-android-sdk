package com.appcues.ui.targetables

import android.util.Log
import android.view.View
import androidx.compose.ui.unit.IntRect
import com.appcues.ui.targetables.TargetableElement.XMLView

internal object AppcuesViewScanner {

    fun scan(view: View): TargetableElement {
        return view.toXMLView().also { it.xRay() }
    }

    /**
     * Testing helper
     */
    private fun TargetableElement.xRay(spacer: Int = 0) {
        var spacerPrefix = ""
        for (i in 0 until spacer) {
            spacerPrefix += "--"
        }
        Log.i("XRay", "$spacerPrefix $this {")
        Log.i("XRay", "$spacerPrefix    position: x = ${bounds.left} | y = ${bounds.top}")
        children.forEach { it.xRay(spacer + 1) }
        Log.i("XRay", "$spacerPrefix }")
    }

    /**
     * Map a regular View to [TargetableElement.XMLView]
     */
    internal fun View.toXMLView(parent: XMLView? = null): XMLView {
        val xPosition = left + (parent?.bounds?.left ?: 0)
        val yPosition = top + (parent?.bounds?.top ?: 0)

        return XMLView(
            parent = parent,
            name = this::class.java.simpleName,
            bounds = IntRect(left = xPosition, top = yPosition, right = xPosition + width, bottom = yPosition + height),
            view = this,
        )
    }
}
