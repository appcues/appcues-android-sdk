package com.appcues.trait

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable

interface StepDecoratingTrait : ExperienceTrait {

    enum class StepDecoratingType {
        UNDERLAY, OVERLAY
    }

    /**
     * [stepComposeOrder] defines whether this trait will be rendered under or over the step's main content
     */
    val stepComposeOrder: StepDecoratingType

    @Composable
    fun BoxScope.DecorateStep(stepDecoratingPadding: StepDecoratingPadding)
}
