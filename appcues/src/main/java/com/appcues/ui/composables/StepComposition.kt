package com.appcues.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
internal fun Step.ComposeStep(modifier: Modifier = Modifier, wrapperInsets: PaddingValues, parent: BoxScope) {
    CompositionLocalProvider(
        LocalAppcuesActions provides actions,
        LocalExperienceStepFormStateDelegate provides formState
    ) {
        // used to get the padding values from step decorating trait and apply to the Column
        val density = LocalDensity.current
        val stickyContentPadding = remember(this) { StickyContentPadding(density) }

        ApplyUnderlayStepTraits(parent, wrapperInsets, stickyContentPadding)

        ComposeStepContent(modifier, wrapperInsets, stickyContentPadding)

        ComposeStickyContent(parent, wrapperInsets, stickyContentPadding)

        ApplyOverlayStepTraits(parent, wrapperInsets, stickyContentPadding)
    }
}

@Composable
private fun Step.ApplyUnderlayStepTraits(
    boxScope: BoxScope,
    wrapperInsets: PaddingValues,
    stickyContentPadding: StickyContentPadding
) {
    stepDecoratingTraits
        .filter { it.stepComposeOrder == StepDecoratingType.UNDERLAY }
        .forEach { it.run { boxScope.DecorateStep(wrapperInsets, stickyContentPadding) } }
}

@Composable
private fun Step.ApplyOverlayStepTraits(
    boxScope: BoxScope,
    wrapperInsets: PaddingValues,
    stickyContentPadding: StickyContentPadding
) {
    stepDecoratingTraits
        .filter { it.stepComposeOrder == StepDecoratingType.OVERLAY }
        .forEach { it.run { boxScope.DecorateStep(wrapperInsets, stickyContentPadding) } }
}

@Composable
private fun Step.ComposeStepContent(
    modifier: Modifier,
    wrapperInsets: PaddingValues,
    stickyContentPadding: StickyContentPadding
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(wrapperInsets)
            .padding(stickyContentPadding.paddingValues.value)
    ) { content.Compose() }
}

@Composable
private fun Step.ComposeStickyContent(
    boxScope: BoxScope,
    wrapperInsets: PaddingValues,
    stickyContentPadding: StickyContentPadding
) {
    topStickyContent?.let {
        Box(
            modifier = Modifier
                .padding(wrapperInsets)
                .alignStepOverlay(boxScope, Alignment.TopCenter, stickyContentPadding),
            contentAlignment = Alignment.BottomCenter
        ) { it.Compose() }
    }

    bottomStickyContent?.let {
        Box(
            modifier = Modifier
                .padding(wrapperInsets)
                .alignStepOverlay(boxScope, Alignment.BottomCenter, stickyContentPadding),
            contentAlignment = Alignment.BottomCenter
        ) { it.Compose() }
    }
}
