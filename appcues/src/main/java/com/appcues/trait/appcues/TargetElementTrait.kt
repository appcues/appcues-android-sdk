package com.appcues.trait.appcues

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
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

    override fun produceMetadata(): Map<String, Any?> {
        // TODO replace this with dynamic element found based on "selector" config property
        val rect = Rect(
            Offset(
                x = config.getConfigInt("x")?.toFloat() ?: 0f,
                y = config.getConfigInt("y")?.toFloat() ?: 0f
            ),
            Size(
                width = config.getConfigInt("width")?.toFloat() ?: 0f,
                height = config.getConfigInt("height")?.toFloat() ?: 0f
            )
        )

        return hashMapOf(METADATA_TARGET_RECT to rect)
    }
}
