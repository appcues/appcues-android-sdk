package com.appcues.trait.appcues

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.appcues.data.mapper.step.StepContentMapper
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfigPrimitive
import com.appcues.trait.ContainerDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait.ContainerDecoratingType
import com.appcues.trait.ExperienceTrait.ExperienceTraitLevel
import com.appcues.trait.StepDecoratingPadding
import com.appcues.trait.StepDecoratingTrait
import com.appcues.trait.StepDecoratingTrait.StepDecoratingType
import com.appcues.ui.primitive.Compose

internal class BackgroundContentTrait(
    override val config: AppcuesConfigMap,
    stepContentMapper: StepContentMapper,
) : ContainerDecoratingTrait, StepDecoratingTrait {

    companion object {

        const val TYPE = "@appcues/background-content"
    }

    override val type = TYPE

    override val stepComposeOrder = StepDecoratingType.UNDERLAY

    override val containerComposeOrder = ContainerDecoratingType.UNDERLAY

    override val level = ExperienceTraitLevel.GROUP

    private val content = config.getConfigPrimitive("content", stepContentMapper)

    @Composable
    override fun BoxScope.DecorateStep(stepDecoratingPadding: StepDecoratingPadding) {
        Decorate()
    }

    @Composable
    override fun BoxScope.DecorateContainer() {
        Decorate()
    }

    @Composable
    private fun BoxScope.Decorate() {
        content?.Compose(this)
    }
}
