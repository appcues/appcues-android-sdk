package com.appcues.trait

import androidx.compose.runtime.Composable

interface ContentWrappingTrait : ExperienceTrait {

    @Composable
    fun WrapContent(content: @Composable () -> Unit)
}
