package com.appcues.trait.appcues

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfig
import com.appcues.data.model.getConfigDouble
import com.appcues.data.model.getConfigInt
import com.appcues.data.model.getConfigStyle
import com.appcues.trait.AppcuesTraitException
import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.trait.appcues.EffectsTrait.PresentationStyle.CONFETTI
import com.appcues.trait.appcues.effects.ConfettiEffect

internal class EffectsTrait(
    override val config: AppcuesConfigMap,
) : BackdropDecoratingTrait {

    companion object {

        const val TYPE = "@appcues/effects"
        const val DEFAULT_DURATION = 2000
        const val DEFAULT_INTENSITY = 1.0
    }

    private enum class PresentationStyle {
        CONFETTI
    }

    override val isBlocking = false

    private val presentationStyle = config.getConfig<String?>("presentationStyle").toPresentationStyle()

    private val duration = config.getConfigInt("duration") ?: DEFAULT_DURATION

    private val intensity = config.getConfigDouble("intensity") ?: DEFAULT_INTENSITY

    private val style = config.getConfigStyle("style")

    @Composable
    override fun BoxScope.BackdropDecorate(isBlocking: Boolean, content: @Composable BoxScope.() -> Unit) {
        // other backdrop decorate traits renders first (putting this one on top)
        content()

        when (presentationStyle) {
            CONFETTI -> ConfettiEffect(style, duration, intensity)
        }
    }

    private fun String?.toPresentationStyle(): PresentationStyle {
        return when (this) {
            "confetti" -> CONFETTI
            else -> throw AppcuesTraitException("invalid effects presentation style: $this")
        }
    }
}
