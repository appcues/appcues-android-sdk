package com.appcues.data.remote.appcues.response.step.primitive

import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.Type.BLOCK
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.Type.BOX
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.Type.BUTTON
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.Type.CUSTOM_COMPONENT
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.Type.EMBED
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.Type.IMAGE
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.Type.OPTION_SELECT
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.Type.SPACER
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.Type.STACK
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.Type.TEXT
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.Type.TEXT_INPUT
import com.appcues.data.remote.appcues.response.styling.StyleColorResponse
import com.appcues.data.remote.appcues.response.styling.StyleResponse
import com.appcues.data.remote.appcues.response.styling.StyleSizeResponse
import java.util.UUID

internal sealed class PrimitiveResponse(
    val type: Type,
) {

    enum class Type(val jsonName: String) {
        STACK("stack"),
        BOX("box"),
        CUSTOM_COMPONENT("customComponent"),
        BUTTON("button"),
        TEXT("text"),
        IMAGE("image"),
        EMBED("embed"),
        SPACER("spacer"),
        TEXT_INPUT("textInput"),
        OPTION_SELECT("optionSelect"),
        BLOCK("block"),
    }

    internal data class BlockPrimitiveResponse(
        val id: UUID,
        val content: PrimitiveResponse,
    ) : PrimitiveResponse(BLOCK)

    internal data class BoxPrimitiveResponse(
        val id: UUID,
        val style: StyleResponse? = null,
        val items: List<PrimitiveResponse>,
    ) : PrimitiveResponse(BOX)

    internal data class CustomComponentPrimitiveResponse(
        val id: UUID,
        val style: StyleResponse? = null,
        val identifier: String,
        val config: AppcuesConfigMap,
    ) : PrimitiveResponse(CUSTOM_COMPONENT)

    internal data class StackPrimitiveResponse(
        val id: UUID,
        val style: StyleResponse? = null,
        val orientation: String,
        val items: List<PrimitiveResponse>,
        val distribution: String? = null,
        val spacing: Double = 0.0,
        val sticky: String? = null,
    ) : PrimitiveResponse(STACK)

    internal data class TextPrimitiveResponse(
        val id: UUID,
        val style: StyleResponse? = null,
        val text: String? = null,
        val spans: List<TextSpanResponse>? = null,
    ) : PrimitiveResponse(TEXT)

    internal data class TextSpanResponse(
        val text: String,
        val style: StyleResponse? = null,
    )

    internal data class ButtonPrimitiveResponse(
        val id: UUID,
        val style: StyleResponse? = null,
        val content: PrimitiveResponse,
    ) : PrimitiveResponse(BUTTON)

    internal data class ImagePrimitiveResponse(
        val id: UUID,
        val style: StyleResponse? = null,
        val imageUrl: String,
        val contentMode: String? = null,
        val blurHash: String? = null,
        val intrinsicSize: StyleSizeResponse? = null,
        val accessibilityLabel: String? = null,
    ) : PrimitiveResponse(IMAGE)

    internal data class EmbedPrimitiveResponse(
        val id: UUID,
        val style: StyleResponse? = null,
        val embed: String,
        val intrinsicSize: StyleSizeResponse? = null,
    ) : PrimitiveResponse(EMBED)

    internal data class SpacerPrimitiveResponse(
        val id: UUID,
        val spacing: Double = 0.0,
    ) : PrimitiveResponse(SPACER)

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
        val leadingFill: Boolean?
    ) : PrimitiveResponse(OPTION_SELECT) {

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
    ) : PrimitiveResponse(TEXT_INPUT)
}
