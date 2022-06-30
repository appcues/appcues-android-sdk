package com.appcues.trait

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable

interface StepDecoratingTrait : ExperienceTrait {

    @Composable
    fun BoxScope.Overlay(stepDecoratingPadding: StepDecoratingPadding)
}
