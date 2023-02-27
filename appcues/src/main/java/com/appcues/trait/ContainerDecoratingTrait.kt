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

    @Composable
    fun BoxScope.DecorateContainer(wrapperInsets: PaddingValues)
}
