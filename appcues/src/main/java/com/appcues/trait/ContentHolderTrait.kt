package com.appcues.trait

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable

interface ContentHolderTrait : ExperienceTrait {

    @Composable
    fun BoxScope.CreateContentHolder(pages: List<@Composable () -> Unit>, pageIndex: Int)
}
