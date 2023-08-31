package com.appcues

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.isVisible

/**
 * AppcuesFrameView should is used when customers want to define a specific place for inflating Embed content
 * in their app. By placing an AppcuesFrameView in their layout, then can later register this view with a frameId.
 *
 * appcues.registerEmbed("frame1", appcuesFrameView)
 */
public class AppcuesFrameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    internal val composeView = ComposeView(context)

    init {
        // frame view will start GONE and the view presenter
        // will make VISIBLE as needed, when content is rendering into frame
        isVisible = false
        addView(composeView)
    }

    internal fun reset() {
        composeView.setContent { }
    }
}
