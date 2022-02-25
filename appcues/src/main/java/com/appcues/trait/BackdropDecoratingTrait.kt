package com.appcues.trait

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable

interface BackdropDecoratingTrait : ExperienceTrait {

    @Composable
    fun Backdrop(scope: BoxScope)
}
