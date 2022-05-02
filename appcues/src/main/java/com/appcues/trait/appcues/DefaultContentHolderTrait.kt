package com.appcues.trait.appcues

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.appcues.trait.ContentHolderTrait
import com.appcues.trait.ContentHolderTrait.ContainerPages

internal class DefaultContentHolderTrait(override val config: Map<String, Any>?) : ContentHolderTrait {

    @Composable
    override fun BoxScope.CreateContentHolder(containerPages: ContainerPages) {
        containerPages.composePage(containerPages.currentPage)
    }
}
