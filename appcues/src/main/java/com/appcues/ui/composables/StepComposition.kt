package com.appcues.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.appcues.data.model.Step
import com.appcues.trait.StepDecoratingPadding
import com.appcues.trait.StepDecoratingTrait.StepDecoratingType
import com.appcues.trait.alignStepOverlay
import com.appcues.ui.primitive.Compose

@Composable
internal fun Step.ComposeStepContent(
    index: Int,
    hasFixedHeight: Boolean,
    contentPadding: PaddingValues?,
    stepDecoratingPadding: StepDecoratingPadding
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            // if WrappingContent has a fixed height we fill height
            // else we will scale according to content
            .then(if (hasFixedHeight) Modifier.fillMaxHeight() else Modifier)
            .verticalScroll(rememberScrollState())
            // if we have contentPadding to apply from the WrapContent trait then we apply here
            .then(if (contentPadding != null) Modifier.padding(contentPadding) else Modifier)
            .padding(paddingValues = stepDecoratingPadding.paddingValues.value)
            .testTag("page_$index")
    ) {
        content.Compose()
    }
}

@Composable
internal fun Step.ApplyUnderlayStepTraits(boxScope: BoxScope, stepDecoratingPadding: StepDecoratingPadding) {
    stepDecoratingTraits
        .filter { it.stepComposeOrder == StepDecoratingType.UNDERLAY }
        .forEach { it.run { boxScope.DecorateStep(stepDecoratingPadding) } }
}

@Composable
internal fun Step.ApplyOverlayStepTraits(boxScope: BoxScope, stepDecoratingPadding: StepDecoratingPadding) {
    stepDecoratingTraits
        .filter { it.stepComposeOrder == StepDecoratingType.OVERLAY }
        .forEach { it.run { boxScope.DecorateStep(stepDecoratingPadding) } }
}

@Composable
internal fun Step.ComposeStickyContent(boxScope: BoxScope, stepDecoratingPadding: StepDecoratingPadding) {
    topStickyContent?.let {
        Box(
            modifier = Modifier.alignStepOverlay(boxScope, Alignment.TopCenter, stepDecoratingPadding),
            contentAlignment = Alignment.BottomCenter
        ) { it.Compose() }
    }

    bottomStickyContent?.let {
        Box(
            modifier = Modifier.alignStepOverlay(boxScope, Alignment.BottomCenter, stepDecoratingPadding),
            contentAlignment = Alignment.BottomCenter
        ) { it.Compose() }
    }
}
