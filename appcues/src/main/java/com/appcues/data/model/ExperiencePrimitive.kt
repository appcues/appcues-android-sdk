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
    open val actions: List<Action>,
) {

    data class TextPrimitive(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        override val actions: List<Action> = arrayListOf(),
        val text: String,
    ) : ExperiencePrimitive(id, style, actions)

    data class ButtonPrimitive(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        override val actions: List<Action> = arrayListOf(),
        val content: ExperiencePrimitive,
    ) : ExperiencePrimitive(id, style, actions)

    data class ImagePrimitive(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        override val actions: List<Action> = arrayListOf(),
        val url: String,
        val accessibilityLabel: String?,
        val intrinsicSize: ComponentSize?,
        val contentMode: ComponentContentMode = FILL,
    ) : ExperiencePrimitive(id, style, actions)

    data class VerticalStackPrimitive(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        override val actions: List<Action> = arrayListOf(),
        val items: List<ExperiencePrimitive>,
        val spacing: Int = 0,
    ) : ExperiencePrimitive(id, style, actions)

    data class HorizontalStackPrimitive(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        override val actions: List<Action> = arrayListOf(),
        val items: List<ExperiencePrimitive>,
        val spacing: Int = 0,
        val distribution: ComponentDistribution = CENTER,
    ) : ExperiencePrimitive(id, style, actions)
}
