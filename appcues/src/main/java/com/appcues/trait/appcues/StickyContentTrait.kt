package com.appcues.trait.appcues

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.appcues.data.mapper.step.StepContentMapper
import com.appcues.data.model.getConfig
import com.appcues.data.model.getConfigPrimitive
import com.appcues.trait.ExperienceTrait.ExperienceTraitLevel
import com.appcues.trait.StepDecoratingPadding
import com.appcues.trait.StepDecoratingTrait
import com.appcues.trait.StepDecoratingTrait.StepDecoratingType
import com.appcues.trait.alignStepOverlay
import com.appcues.ui.primitive.Compose

internal class StickyContentTrait(
    override val config: Map<String, Any>?,
    stepContentMapper: StepContentMapper,
) : StepDecoratingTrait {

    companion object {

        const val TYPE = "@appcues/sticky-content"
    }

    override val type = TYPE

    override val level = ExperienceTraitLevel.STEP

    override val stepComposeOrder = StepDecoratingType.OVERLAY

    private val edge = config.getConfig<String>("edge")
    private val content = config.getConfigPrimitive("content", stepContentMapper)

    @Composable
    override fun BoxScope.DecorateStep(stepDecoratingPadding: StepDecoratingPadding) {
        content?.let {
            Box(
                modifier = Modifier.alignStepOverlay(this, edge.edgeToAlignment(), stepDecoratingPadding),
                contentAlignment = Alignment.BottomCenter
            ) { it.Compose() }
        }
    }

    private fun String?.edgeToAlignment(): Alignment {
        return when (this) {
            "top" -> Alignment.TopCenter
            "bottom" -> Alignment.BottomCenter
            else -> Alignment.BottomCenter
        }
    }
}
