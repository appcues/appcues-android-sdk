package com.appcues.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
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
    parent: BoxScope,
    hasVerticalScroll: Boolean,
) {
    key(id) {
        CompositionLocalProvider(
            LocalAppcuesActions provides actions,
            LocalExperienceStepFormStateDelegate provides formState,
        ) {
            // used to get the padding values from step decorating trait and apply to the Column
            val density = LocalDensity.current
            val stickyContentPadding = remember(this) { StickyContentPadding(density) }

            ApplyUnderlayStepTraits(parent, containerPadding, safeAreaInsets, stickyContentPadding)

            ComposeStepContent(modifier, containerPadding, safeAreaInsets, stickyContentPadding, hasVerticalScroll)

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
    stickyContentPadding: StickyContentPadding,
    hasVerticalScroll: Boolean,
) {
    BoxWithConstraints() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .padding(containerPadding)
                .padding(safeAreaInsets)
                .padding(stickyContentPadding.paddingValues.value)
                .stepVerticalScroll(hasVerticalScroll, maxHeight)
        ) { content.Compose() }
    }
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

// The `enabled` parameter allows the parent trait to control whether or not step content is
// scrollable. Embeds, for instance, would not allow vertical scrolling, as they just size the
// content to the required height.
//
// If enabled, the maxHeight param is used to check if the sized content will actually need
// scrolling. If the content height is less than the maxHeight, then no scrolling is enabled. This
// is so that other gestures like swipe dismiss can be applied.
//
// If enabled and the content is taller than the available space, then scrolling is enabled.
private fun Modifier.stepVerticalScroll(enabled: Boolean, maxHeightDp: Dp) = composed {
    if (enabled) {
        val contentHeight = remember { mutableIntStateOf(0) }
        val maxHeight = with(LocalDensity.current) { maxHeightDp.toPx() }
        Modifier
            // Measures the height of the content to determine if scroll needed
            .onGloballyPositioned { coordinates ->
                contentHeight.intValue = coordinates.size.height
            }
            .let {
                if (contentHeight.intValue < maxHeight) {
                    it
                } else {
                    it.verticalScroll(rememberScrollState())
                }
            }
    } else {
        Modifier
    }
}
