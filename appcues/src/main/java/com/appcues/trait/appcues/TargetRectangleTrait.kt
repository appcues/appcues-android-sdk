package com.appcues.trait.appcues

import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfig
import com.appcues.data.model.getConfigInt
import com.appcues.data.model.getConfigOrDefault
import com.appcues.trait.MetadataSettingTrait

internal class TargetRectangleTrait(
    override val config: AppcuesConfigMap,
) : MetadataSettingTrait {

    companion object {

        const val TYPE = "@appcues/target-rectangle"
    }

    override suspend fun produceMetadata(): Map<String, Any?> {
        val targetRectangle = TargetRectangleInfo(
            x = config.getConfigInt("x")?.toFloat() ?: 0f,
            y = config.getConfigInt("y")?.toFloat() ?: 0f,
            relativeX = config.getConfig("relativeX") ?: 0.0,
            relativeY = config.getConfig("relativeY") ?: 0.0,
            width = config.getConfigInt("width")?.toFloat() ?: 0f,
            height = config.getConfigInt("height")?.toFloat() ?: 0f,
            relativeWidth = config.getConfig("relativeWidth") ?: 0.0,
            relativeHeight = config.getConfig("relativeHeight") ?: 0.0,
            contentDistance = config.getConfigOrDefault("contentDistanceFromTarget", 0.0),
            prefPosition = config.getConfig<String>("contentPreferredPosition").toPosition()
        )

        return hashMapOf(TARGET_RECTANGLE_METADATA to targetRectangle)
    }
}
