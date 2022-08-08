package com.appcues.trait.appcues

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.appcues.trait.ContentHolderTrait
import com.appcues.trait.ContentHolderTrait.ContainerPages
import com.appcues.trait.ExperienceTrait.ExperienceTraitLevel

internal class DefaultContentHolderTrait(override val config: Map<String, Any>?) : ContentHolderTrait {

    companion object {

        const val TYPE = "@appcues/default-content-holder"
    }

    override val type = TYPE

    override val level = ExperienceTraitLevel.GROUP

    @Composable
    override fun BoxScope.CreateContentHolder(containerPages: ContainerPages) {
        containerPages.composePage(containerPages.currentPage)
    }
}
