package com.appcues.trait

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface ContentWrappingTrait : ExperienceTrait {

    /**
     * Creates a wrapper for the [content].
     *
     * Example usage:
     * @sample com.appcues.trait.appcues.ModalTrait
     *
     * @param content The content of the wrapper.
     *                [modifier] gives flexibility of the content main box down the stream of composition.
     *                [wrapperInsets] defines safe area padding for the content inside.
     */
    @Composable
    fun WrapContent(content: @Composable (modifier: Modifier, wrapperInsets: PaddingValues) -> Unit)
}
