package com.appcues

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView

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

    private val composeView = ComposeView(context)

    internal fun setupComposeView(): ComposeView {
        // when index is -1 it means the view is not added yet
        if (indexOfChild(composeView) == -1) {
            addView(composeView)
        }

        return composeView
    }

    internal fun reset() {
        removeView(composeView)
        composeView.setContent { }
    }

    // Below: enforce restrictions on adding child views to this ViewGroup
    private fun checkAddView(child: View?) {
        if (child != composeView) {
            throw UnsupportedOperationException("Cannot add views to ${javaClass.simpleName}.")
        }
    }

    override fun addView(child: View?) {
        checkAddView(child)
        super.addView(child)
    }

    override fun addView(child: View?, index: Int) {
        checkAddView(child)
        super.addView(child, index)
    }

    override fun addView(child: View?, width: Int, height: Int) {
        checkAddView(child)
        super.addView(child, width, height)
    }

    override fun addView(child: View?, params: android.view.ViewGroup.LayoutParams?) {
        checkAddView(child)
        super.addView(child, params)
    }

    override fun addView(child: View?, index: Int, params: android.view.ViewGroup.LayoutParams?) {
        checkAddView(child)
        super.addView(child, index, params)
    }

    override fun addViewInLayout(child: View?, index: Int, params: android.view.ViewGroup.LayoutParams?): Boolean {
        checkAddView(child)
        return super.addViewInLayout(child, index, params)
    }

    override fun addViewInLayout(
        child: View?,
        index: Int,
        params: android.view.ViewGroup.LayoutParams?,
        preventRequestLayout: Boolean
    ): Boolean {
        checkAddView(child)
        return super.addViewInLayout(child, index, params, preventRequestLayout)
    }
}

internal class AppcuesOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    internal val composeView = ComposeView(context)

    init {
        addView(composeView)
    }

    internal fun clearComposition() {
        composeView.setContent { }
    }
}
