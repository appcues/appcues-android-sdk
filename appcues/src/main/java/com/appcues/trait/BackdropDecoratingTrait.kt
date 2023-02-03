package com.appcues.trait

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable

interface BackdropDecoratingTrait : ExperienceTrait {

    companion object {
        const val BACKDROP_KEYHOLE_PRIORITY = 99
        const val BACKDROP_BACKGROUND_PRIORITY = 89
        const val BACKDROP_SKIPPABLE_PRIORITY = 59
    }

    val priority: Int

    @Composable
    fun BoxScope.BackdropDecorate(content: @Composable BoxScope.() -> Unit)
}
