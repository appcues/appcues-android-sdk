package com.appcues.trait.appcues

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfig
import com.appcues.data.model.getConfigOrDefault
import com.appcues.trait.StepDecoratingPadding
import com.appcues.trait.StepDecoratingTrait
import com.appcues.trait.StepDecoratingTrait.StepDecoratingType
import com.appcues.trait.alignStepOverlay
import com.appcues.ui.composables.LocalCompositionTranslation

internal class LocalizationTrait(
    override val config: AppcuesConfigMap,
) : StepDecoratingTrait {

    companion object {
        const val TYPE = "@appcues/localization"
    }

    override val stepComposeOrder = StepDecoratingType.OVERLAY

    @Composable
    override fun BoxScope.DecorateStep(stepDecoratingPadding: StepDecoratingPadding) {
        val translation = LocalCompositionTranslation.current
        Box(
            modifier = Modifier.alignStepOverlay(this, Alignment.BottomEnd, stepDecoratingPadding),
            contentAlignment = Alignment.BottomEnd
        ) {
            if (onDemand) {
                val translated = translation?.translateComposition?.value ?: false
                Text(
                    if (translated) "View Original" else "Translate",
                    style = TextStyle(color = Color.LightGray),
                    modifier = Modifier.padding(10.dp)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                            translation?.let { compositionTranslation ->
                                compositionTranslation.sourceLanguageTag = sourceLanguageTag
                                compositionTranslation.targetLanguageTag = targetLanguageTag
                                compositionTranslation.translateComposition.value = !translated
                            }
                        }
                )
            }
        }
    }

    val translate: Boolean = config.getConfigOrDefault("translate", true)
    val sourceLanguageTag: String? = config.getConfig("source")
    val targetLanguageTag: String? = config.getConfig("target")
    val onDemand: Boolean = config.getConfigOrDefault("onDemand", false)
}
