package com.appcues.trait

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable

/**
 * A trait that modifies the backdrop of an experience.
 */
internal interface BackdropDecoratingTrait : ExperienceTrait {

    /**
     * Indicates whether or not this trait will block interactions from passing through the backdrop
     * to application content behind the experience.
     */
    val isBlocking: Boolean

    /**
     * Decorates the backdrop of the experience
     *
     * Example usage:
     * @sample com.appcues.trait.appcues.BackdropTrait
     *
     * @param isBlocking Specifies whether the current content has any blocking BackdropDecoratingTraits being applied.
     *                   This can be used by any other decorating traits to decide how to apply behaviors.
     * @param content BackdropDecoratingTraits are chained together as one composition.
     *                Its important to call [content] if you want to apply every decoration on stack
     */
    @Composable
    fun BoxScope.BackdropDecorate(isBlocking: Boolean, content: @Composable BoxScope.() -> Unit)
}
