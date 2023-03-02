package com.appcues.trait

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable

interface ContainerDecoratingTrait : ExperienceTrait {

    enum class ContainerDecoratingType {
        UNDERLAY, OVERLAY
    }

    /**
     * [containerComposeOrder] defines whether this trait will be rendered under or over the container's main layout (steps)
     */
    val containerComposeOrder: ContainerDecoratingType

    /**
     * Decorates the container, it can decorate UNDER the content or OVER the content.
     *
     * Example usage:
     * @sample com.appcues.trait.appcues.BackgroundContentTrait
     *
     * @param containerPadding The padding defined in the style of the container, to apply to main content within.
     * @param safeAreaInsets The safe area information from the wrapper trait.
     */
    @Composable
    fun BoxScope.DecorateContainer(containerPadding: PaddingValues, safeAreaInsets: PaddingValues)
}
