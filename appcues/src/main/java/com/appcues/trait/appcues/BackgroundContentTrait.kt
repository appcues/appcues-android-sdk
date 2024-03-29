package com.appcues.trait.appcues

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfigPrimitive
import com.appcues.trait.ContainerDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait.ContainerDecoratingType
import com.appcues.trait.ExperienceTraitLevel
import com.appcues.trait.ExperienceTraitLevel.GROUP
import com.appcues.trait.ExperienceTraitLevel.STEP
import com.appcues.trait.StepDecoratingTrait
import com.appcues.trait.StepDecoratingTrait.StepDecoratingType
import com.appcues.trait.StickyContentPadding
import com.appcues.ui.primitive.Compose

internal class BackgroundContentTrait(
    override val config: AppcuesConfigMap,
    private val level: ExperienceTraitLevel,
) : ContainerDecoratingTrait, StepDecoratingTrait {

    companion object {

        const val TYPE = "@appcues/background-content"
    }

    override val stepComposeOrder = StepDecoratingType.UNDERLAY

    override val containerComposeOrder = ContainerDecoratingType.UNDERLAY

    private val content = config.getConfigPrimitive("content")

    @Composable
    override fun BoxScope.DecorateStep(
        containerPadding: PaddingValues,
        safeAreaInsets: PaddingValues,
        stickyContentPadding: StickyContentPadding,
    ) {
        if (level == STEP) Decorate()
    }

    @Composable
    override fun BoxScope.DecorateContainer(
        containerPadding: PaddingValues,
        safeAreaInsets: PaddingValues,
    ) {
        if (level == GROUP) Decorate()
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun BoxScope.Decorate() {
        Box(
            modifier = Modifier
                .matchParentSize()
                // merge all children nodes to this one, and mark as invisibleToUser so talkback wont read.
                // this is the same as old xml: importantForAccessibility = false
                .semantics(true) { invisibleToUser() }
        ) {
            content?.Compose(this)
        }
    }
}
