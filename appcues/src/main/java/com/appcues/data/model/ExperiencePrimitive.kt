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
        val text: String,
        override val style: ComponentStyle = ComponentStyle(),
    ) : ExperiencePrimitive(id, style)

    data class ButtonPrimitive(
        override val id: UUID,
        val content: ExperiencePrimitive,
        override val style: ComponentStyle = ComponentStyle(),
    ) : ExperiencePrimitive(id, style)

    data class ImagePrimitive(
        override val id: UUID,
        val url: String,
        val accessibilityLabel: String?,
        val intrinsicSize: ComponentSize?,
        val contentMode: ComponentContentMode = FILL,
        override val style: ComponentStyle = ComponentStyle(),
    ) : ExperiencePrimitive(id, style)

    data class VerticalStackPrimitive(
        override val id: UUID,
        val items: List<ExperiencePrimitive>,
        val spacing: Int = 0,
        override val style: ComponentStyle = ComponentStyle(),
    ) : ExperiencePrimitive(id, style)

    data class HorizontalStackPrimitive(
        override val id: UUID,
        val items: List<ExperiencePrimitive>,
        val spacing: Int = 0,
        val distribution: ComponentDistribution = CENTER,
        override val style: ComponentStyle = ComponentStyle(),
    ) : ExperiencePrimitive(id, style)
}
