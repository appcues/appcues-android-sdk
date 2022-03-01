package com.appcues.trait

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable

interface ContainerDecoratingTrait : ExperienceTrait {

    @Composable
    fun Overlay(scope: BoxScope)
}
