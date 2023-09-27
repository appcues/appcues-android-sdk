package com.appcues.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.appcues.data.model.Step
import com.appcues.trait.StepDecoratingTrait.StepDecoratingType
import com.appcues.trait.StickyContentPadding
import com.appcues.trait.alignStepOverlay
import com.appcues.ui.primitive.Compose

@Composable
internal fun Step.ComposeStep(
    modifier: Modifier = Modifier,
    containerPadding: PaddingValues,
    safeAreaInsets: PaddingValues,
    parent: BoxScope
) {
    key(id) {
        CompositionLocalProvider(
            LocalAppcuesActions provides actions,
            LocalExperienceStepFormStateDelegate provides formState
        ) {
            // used to get the padding values from step decorating trait and apply to the Column
            val density = LocalDensity.current
            val stickyContentPadding = remember(this) { StickyContentPadding(density) }

            ApplyUnderlayStepTraits(parent, containerPadding, safeAreaInsets, stickyContentPadding)

            ComposeStepContent(modifier, containerPadding, safeAreaInsets, stickyContentPadding)

            ComposeStickyContent(parent, containerPadding, safeAreaInsets, stickyContentPadding)

            ApplyOverlayStepTraits(parent, containerPadding, safeAreaInsets, stickyContentPadding)
        }
    }
}

@Composable
private fun Step.ApplyUnderlayStepTraits(
    boxScope: BoxScope,
    containerPadding: PaddingValues,
    safeAreaInsets: PaddingValues,
    stickyContentPadding: StickyContentPadding
) {
    stepDecoratingTraits
        .filter { it.stepComposeOrder == StepDecoratingType.UNDERLAY }
        .forEach { it.run { boxScope.DecorateStep(containerPadding, safeAreaInsets, stickyContentPadding) } }
}

@Composable
private fun Step.ApplyOverlayStepTraits(
    boxScope: BoxScope,
    containerPadding: PaddingValues,
    safeAreaInsets: PaddingValues,
    stickyContentPadding: StickyContentPadding
) {
    stepDecoratingTraits
        .filter { it.stepComposeOrder == StepDecoratingType.OVERLAY }
        .forEach { it.run { boxScope.DecorateStep(containerPadding, safeAreaInsets, stickyContentPadding) } }
}

@Composable
private fun Step.ComposeStepContent(
    modifier: Modifier,
    containerPadding: PaddingValues,
    safeAreaInsets: PaddingValues,
    stickyContentPadding: StickyContentPadding
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(containerPadding)
            .padding(safeAreaInsets)
            .padding(stickyContentPadding.paddingValues.value)
    ) { content.Compose() }
}

@Composable
private fun Step.ComposeStickyContent(
    boxScope: BoxScope,
    containerPadding: PaddingValues,
    safeAreaInsets: PaddingValues,
    stickyContentPadding: StickyContentPadding
) {
    topStickyContent?.let {
        Box(
            modifier = Modifier
                .padding(containerPadding)
                .padding(safeAreaInsets)
                .alignStepOverlay(boxScope, Alignment.TopCenter, stickyContentPadding),
            contentAlignment = Alignment.BottomCenter
        ) { it.Compose() }
    }

    bottomStickyContent?.let {
        Box(
            modifier = Modifier
                .padding(containerPadding)
                .padding(safeAreaInsets)
                .alignStepOverlay(boxScope, Alignment.BottomCenter, stickyContentPadding),
            contentAlignment = Alignment.BottomCenter
        ) { it.Compose() }
    }
}
