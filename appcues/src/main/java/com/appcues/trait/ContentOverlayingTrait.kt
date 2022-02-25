package com.appcues.trait

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable

interface ContentOverlayingTrait : ExperienceTrait {

    @Composable
    fun Overlay(scope: BoxScope)
}
