package com.appcues.data.model

import com.appcues.data.model.styling.ComponentColor
import com.appcues.data.model.styling.ComponentContentMode
import com.appcues.data.model.styling.ComponentContentMode.FIT
import com.appcues.data.model.styling.ComponentControlPosition
import com.appcues.data.model.styling.ComponentControlPosition.LEADING
import com.appcues.data.model.styling.ComponentDataType
import com.appcues.data.model.styling.ComponentDataType.TEXT
import com.appcues.data.model.styling.ComponentDisplayFormat
import com.appcues.data.model.styling.ComponentDisplayFormat.VERTICAL_LIST
import com.appcues.data.model.styling.ComponentDistribution
import com.appcues.data.model.styling.ComponentDistribution.CENTER
import com.appcues.data.model.styling.ComponentSelectMode
import com.appcues.data.model.styling.ComponentSize
import com.appcues.data.model.styling.ComponentStyle
import java.util.UUID

internal sealed class ExperiencePrimitive(
    open val id: UUID,
    open val style: ComponentStyle,
) {

    data class TextPrimitive(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        val text: String,
    ) : ExperiencePrimitive(id, style)

    data class ButtonPrimitive(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        val content: ExperiencePrimitive,
    ) : ExperiencePrimitive(id, style)

    data class ImagePrimitive(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        val url: String,
        val accessibilityLabel: String?,
        val intrinsicSize: ComponentSize?,
        val contentMode: ComponentContentMode = FIT,
        val blurHash: String? = null,
    ) : ExperiencePrimitive(id, style)

    data class VerticalStackPrimitive(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        val items: List<ExperiencePrimitive>,
        val spacing: Double = 0.0,
    ) : ExperiencePrimitive(id, style)

    data class HorizontalStackPrimitive(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        val items: List<ExperiencePrimitive>,
        val spacing: Double = 0.0,
        val distribution: ComponentDistribution = CENTER,
    ) : ExperiencePrimitive(id, style)

    data class BoxPrimitive(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        val items: List<ExperiencePrimitive>,
    ) : ExperiencePrimitive(id, style)

    data class SpacerPrimitive(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
    ) : ExperiencePrimitive(id, style)

    data class EmbedHtmlPrimitive(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        val embed: String,
        val intrinsicSize: ComponentSize?,
    ) : ExperiencePrimitive(id, style)

    data class TextInputPrimitive(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        val label: TextPrimitive,
        val errorLabel: TextPrimitive? = null,
        val placeholder: ExperiencePrimitive? = null,
        val defaultValue: String? = null,
        val required: Boolean = false,
        val numberOfLines: Int = 1,
        val maxLength: Int? = null,
        val dataType: ComponentDataType = TEXT,
        val textFieldStyle: ComponentStyle = ComponentStyle(),
        val cursorColor: ComponentColor? = null,
    ) : ExperiencePrimitive(id, style)

    data class OptionSelectPrimitive(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        val label: TextPrimitive,
        val errorLabel: TextPrimitive? = null,
        val selectMode: ComponentSelectMode,
        val options: List<OptionItem>,
        val defaultValue: Set<String> = setOf(),
        val minSelections: UInt = 0u,
        val maxSelections: UInt? = null,
        val controlPosition: ComponentControlPosition = LEADING,
        val displayFormat: ComponentDisplayFormat = VERTICAL_LIST,
        val pickerStyle: ComponentStyle? = ComponentStyle(),
        val placeholder: ExperiencePrimitive? = null,
        val selectedColor: ComponentColor? = null,
        val unselectedColor: ComponentColor? = null,
        val accentColor: ComponentColor? = null,
    ) : ExperiencePrimitive(id, style) {

        data class OptionItem(
            val value: String,
            val content: ExperiencePrimitive,
            val selectedContent: ExperiencePrimitive? = null,
        )
    }
}
