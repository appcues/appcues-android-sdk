package com.appcues.trait

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable

interface BackdropDecoratingTrait : ExperienceTrait {

    /**
     * Decorates the backdrop of the experience
     *
     * Example usage:
     * @sample com.appcues.trait.appcues.BackdropTrait
     *
     * @param content BackdropDecoratingTraits are chained together as one composition.
     *                Its important to call [content] if you want to apply every decoration on stack
     */
    @Composable
    fun BoxScope.BackdropDecorate(content: @Composable BoxScope.() -> Unit)
}
