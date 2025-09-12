package com.appcues.data.remote.appcues.response.step.primitive

import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.remote.appcues.response.styling.StyleColorResponse
import com.appcues.data.remote.appcues.response.styling.StyleResponse
import com.appcues.data.remote.appcues.response.styling.StyleSizeResponse
import java.util.UUID

internal sealed class PrimitiveResponse {
    internal data class BlockPrimitiveResponse(
        val id: UUID,
        val content: PrimitiveResponse,
    ) : PrimitiveResponse()

    internal data class BoxPrimitiveResponse(
        val id: UUID,
        val style: StyleResponse? = null,
        val items: List<PrimitiveResponse>,
    ) : PrimitiveResponse()

    internal data class CustomComponentPrimitiveResponse(
        val id: UUID,
        val style: StyleResponse? = null,
        val identifier: String,
        val config: AppcuesConfigMap,
    ) : PrimitiveResponse()

    internal data class StackPrimitiveResponse(
        val id: UUID,
        val style: StyleResponse? = null,
        val orientation: String,
        val items: List<PrimitiveResponse>,
        val distribution: String? = null,
        val spacing: Double = 0.0,
        val sticky: String? = null,
    ) : PrimitiveResponse()

    internal data class TextPrimitiveResponse(
        val id: UUID,
        val style: StyleResponse? = null,
        val text: String? = null,
        val spans: List<TextSpanResponse>? = null,
    ) : PrimitiveResponse()

    internal data class TextSpanResponse(
        val text: String,
        val style: StyleResponse? = null,
    )

    internal data class ButtonPrimitiveResponse(
        val id: UUID,
        val style: StyleResponse? = null,
        val content: PrimitiveResponse,
    ) : PrimitiveResponse()

    internal data class ImagePrimitiveResponse(
        val id: UUID,
        val style: StyleResponse? = null,
        val imageUrl: String,
        val contentMode: String? = null,
        val blurHash: String? = null,
        val intrinsicSize: StyleSizeResponse? = null,
        val accessibilityLabel: String? = null,
    ) : PrimitiveResponse()

    internal data class EmbedPrimitiveResponse(
        val id: UUID,
        val style: StyleResponse? = null,
        val embed: String,
        val intrinsicSize: StyleSizeResponse? = null,
    ) : PrimitiveResponse()

    internal data class SpacerPrimitiveResponse(
        val id: UUID,
        val spacing: Double = 0.0,
    ) : PrimitiveResponse()

    internal data class OptionSelectPrimitiveResponse(
        val id: UUID,
        val style: StyleResponse?,
        val label: TextPrimitiveResponse,
        val errorLabel: TextPrimitiveResponse?,
        val selectMode: String,
        val options: List<OptionItem>,
        val defaultValue: Set<String>?,
        val minSelections: Int?,
        val maxSelections: Int?,
        val controlPosition: String?,
        val displayFormat: String?,
        val placeholder: PrimitiveResponse?,
        val pickerStyle: StyleResponse?,
        val selectedColor: StyleColorResponse?,
        val unselectedColor: StyleColorResponse?,
        val accentColor: StyleColorResponse?,
        val attributeName: String?,
        val leadingFill: Boolean?,
        val randomizeOptionOrder: Boolean?
    ) : PrimitiveResponse() {

        data class OptionItem(
            val value: String,
            val content: PrimitiveResponse,
            val selectedContent: PrimitiveResponse? = null,
        )
    }

    internal data class TextInputPrimitiveResponse(
        val id: UUID,
        val style: StyleResponse? = null,
        val label: TextPrimitiveResponse,
        val errorLabel: TextPrimitiveResponse?,
        val placeholder: TextPrimitiveResponse?,
        val defaultValue: String?,
        val required: Boolean?,
        val numberOfLines: Int?,
        val maxLength: Int?,
        val dataType: String?,
        val textFieldStyle: StyleResponse?,
        val cursorColor: StyleColorResponse?,
        val attributeName: String?,
    ) : PrimitiveResponse()
}
