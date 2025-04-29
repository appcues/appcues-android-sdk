package com.appcues

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.isVisible

/**
 * AppcuesFrameView should be used when customers want to define a specific place for inflating Embed content
 * in their app. By placing an AppcuesFrameView in their layout, then can later register this view with a frameId.
 *
 * appcues.registerEmbed("frame1", appcuesFrameView)
 */
public class AppcuesFrameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    internal var composeView = ComposeView(context)

    /**
     * When retainContent is `true` (default), the embed frame content is cached and re-rendered through any
     * re-register of this frame until the next screen_view qualification occurs. This default behavior enables
     * common cell-reuse type of use cases, such as embeds in recycler views.
     *
     * Set this value `false` to require each new register of the same frame ID to qualify for new content
     * independently of any previous usage of the frame view.
     */
    public var retainContent: Boolean = true

    init {
        // frame view will start GONE and the view presenter
        // will make VISIBLE as needed, when content is rendering into frame
        isVisible = false
        addView(composeView)
    }

    internal fun reset() {
        // just clearing the compose view like
        //
        //   composeView.setContent { }
        //
        // appears to be problematic, and can live the view in a state where new content
        // is not rendered correctly upon next usage.
        //
        // thus, below we remove the view entirely and create a new one. This ensures
        // that a cell re-use type of scenario can re-render content as needed.

        removeView(composeView)
        composeView = ComposeView(context)
        addView(composeView)
    }
}
