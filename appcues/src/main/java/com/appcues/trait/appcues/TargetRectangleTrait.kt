package com.appcues.trait.appcues

import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfigInt
import com.appcues.trait.MetadataSettingTrait
import com.appcues.trait.appcues.TargetElementTrait.TargetRect

internal class TargetRectangleTrait(
    override val config: AppcuesConfigMap,
) : MetadataSettingTrait {

    companion object {

        const val TYPE = "@appcues/target-rectangle"
    }

    override fun produceMetadata(): Map<String, Any?> {
        val rect = TargetRect(
            x = config.getConfigInt("x") ?: 0,
            y = config.getConfigInt("y") ?: 0,
            width = config.getConfigInt("width") ?: 0,
            height = config.getConfigInt("height") ?: 0
        )

        return hashMapOf(TargetElementTrait.METADATA_TARGET_RECT to rect)
    }
}
