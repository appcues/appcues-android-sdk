package com.appcues.data.model

import com.appcues.data.model.styling.ComponentContentMode
import com.appcues.data.model.styling.ComponentContentMode.FILL
import com.appcues.data.model.styling.ComponentDistribution
import com.appcues.data.model.styling.ComponentDistribution.CENTER
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
        val contentMode: ComponentContentMode = FILL,
        val blurHash: String? = null,
    ) : ExperiencePrimitive(id, style)

    data class VerticalStackPrimitive(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        val items: List<ExperiencePrimitive>,
        val spacing: Int = 0,
    ) : ExperiencePrimitive(id, style)

    data class HorizontalStackPrimitive(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        val items: List<ExperiencePrimitive>,
        val spacing: Int = 0,
        val distribution: ComponentDistribution = CENTER,
    ) : ExperiencePrimitive(id, style)

    data class EmbedHtmlPrimitive(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        val embed: String,
        val intrinsicSize: ComponentSize?,
    ) : ExperiencePrimitive(id, style)
}
