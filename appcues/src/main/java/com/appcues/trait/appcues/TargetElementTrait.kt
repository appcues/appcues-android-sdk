package com.appcues.trait.appcues

import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfigInt
import com.appcues.trait.MetadataSettingTrait

internal class TargetElementTrait(
    override val config: AppcuesConfigMap,
) : MetadataSettingTrait {

    companion object {

        const val TYPE = "@appcues/target-element"

        const val METADATA_TARGET_RECT = "targetRectangle"
    }

    data class TargetRect(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int,
    )

    override fun produceMetadata(): Map<String, Any?> {
        // TODO replace this with dynamic element found based on "selector" config property
        val rect = TargetRect(
            x = config.getConfigInt("x") ?: 0,
            y = config.getConfigInt("y") ?: 0,
            width = config.getConfigInt("width") ?: 0,
            height = config.getConfigInt("height") ?: 0
        )

        return hashMapOf(METADATA_TARGET_RECT to rect)
    }
}
