package com.appcues.trait

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.appcues.ui.StepDecoratingPadding

interface StepDecoratingTrait : ExperienceTrait {

    @Composable
    fun BoxScope.Overlay(stepDecoratingPadding: StepDecoratingPadding)
}
