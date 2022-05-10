package com.appcues.trait

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable

interface ContentWrappingTrait : ExperienceTrait {

    /**
     * Create a wrap of the content, the default wrap used by appcues is the modal with different types.
     *
     * When creating a custom WrapContent is important to pass [hasFixedHeight] as true if you are defining the height constraint yourself.
     * or else the SDK will consider the container with the same height as the content we will inflate within
     *
     * [contentPadding] is passed from the WrapContent without applying it yourself so we can modify the correct
     * container in order to keep the vertical scroll nicely at the edge or the container.
     */
    @Composable
    fun WrapContent(content: @Composable (hasFixedHeight: Boolean, contentPadding: PaddingValues?) -> Unit)
}
