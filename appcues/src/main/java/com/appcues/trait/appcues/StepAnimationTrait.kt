package com.appcues.trait.appcues

import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfig
import com.appcues.data.model.getConfigInt
import com.appcues.trait.MetadataSettingTrait
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.EASE_IN
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.EASE_IN_OUT
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.EASE_OUT
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.LINEAR

internal class StepAnimationTrait(
    override val config: AppcuesConfigMap,
) : MetadataSettingTrait {

    companion object {

        const val TYPE = "@appcues/step-transition-animation"

        const val STEP_TRANSITION_ANIMATION_METADATA = "stepTransitionAnimation"
        const val DEFAULT_ANIMATION_DURATION = 300
    }

    data class StepTransitionAnimationInfo(
        val duration: Int,
        val easing: StepAnimationEasing,
    )

    enum class StepAnimationEasing {
        LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT
    }

    private val duration = config.getConfigInt("duration") ?: DEFAULT_ANIMATION_DURATION
    private val easing = config.getConfig<String>("easing").toEasing()

    override suspend fun produceMetadata(): Map<String, Any?> {
        return StepTransitionAnimationInfo(
            duration = duration,
            easing = easing,
        ).let { hashMapOf(STEP_TRANSITION_ANIMATION_METADATA to it) }
    }

    private fun String?.toEasing(): StepAnimationEasing {
        return when (this) {
            "linear" -> LINEAR
            "easeIn" -> EASE_IN
            "easeOut" -> EASE_OUT
            "easeInOut" -> EASE_IN_OUT
            else -> LINEAR
        }
    }
}
