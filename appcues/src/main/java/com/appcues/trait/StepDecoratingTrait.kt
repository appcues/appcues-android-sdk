package com.appcues.trait

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable

public interface StepDecoratingTrait : ExperienceTrait {

    public enum class StepDecoratingType {
        UNDERLAY, OVERLAY
    }

    /**
     * [stepComposeOrder] defines whether this trait will be rendered under or over the step's main content
     */
    public val stepComposeOrder: StepDecoratingType

    /**
     * Decorates Specific step
     *
     * Example usage:
     * @sample com.appcues.trait.appcues.BackgroundContentTrait
     *
     * @param containerPadding The padding defined in the style of the container, to apply to main content within.
     * @param safeAreaInsets The safe area information from the wrapper trait.
     * @param stickyContentPadding Padding amount defined by sticky content elements in this step
     */
    @Composable
    public fun BoxScope.DecorateStep(
        containerPadding: PaddingValues,
        safeAreaInsets: PaddingValues,
        stickyContentPadding: StickyContentPadding
    )
}
