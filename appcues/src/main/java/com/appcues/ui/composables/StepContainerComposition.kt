package com.appcues.ui.composables

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.appcues.data.model.StepContainer
import com.appcues.trait.ContainerDecoratingTrait.ContainerDecoratingType

@Composable
internal fun StepContainer.ApplyUnderlayContainerTraits(boxScope: BoxScope) {
    containerDecoratingTraits
        .filter { it.containerComposeOrder == ContainerDecoratingType.UNDERLAY }
        .forEach { it.run { boxScope.DecorateContainer() } }
}

@Composable
internal fun StepContainer.ApplyOverlayContainerTraits(boxScope: BoxScope) {
    containerDecoratingTraits
        .filter { it.containerComposeOrder == ContainerDecoratingType.OVERLAY }
        .forEach { it.run { boxScope.DecorateContainer() } }
}
