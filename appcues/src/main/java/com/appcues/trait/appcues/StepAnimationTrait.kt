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

        const val METADATA_ANIMATION_DURATION = "animationDuration"
        const val METADATA_ANIMATION_EASING = "animationEasing"
        const val DEFAULT_ANIMATION = 300
    }

    override fun produceMetadata(): Map<String, Any?> {
        return hashMapOf(
            METADATA_ANIMATION_DURATION to config.getConfigInt("duration"),
            METADATA_ANIMATION_EASING to config.getConfig<String>("easing").toEasing()
        )
    }

    enum class StepAnimationEasing {
        LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT
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
