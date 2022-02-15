package com.appcues.domain.entity

import com.appcues.domain.entity.action.Action
import com.appcues.domain.entity.styling.ComponentContentMode
import com.appcues.domain.entity.styling.ComponentContentMode.FILL
import com.appcues.domain.entity.styling.ComponentDistribution
import com.appcues.domain.entity.styling.ComponentDistribution.CENTER
import com.appcues.domain.entity.styling.ComponentSize
import com.appcues.domain.entity.styling.ComponentStyle
import java.util.UUID

internal sealed class ExperienceComponent(
    open val id: UUID,
    open val style: ComponentStyle,
    open val actions: List<Action>,
) {

    data class TextComponent(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        override val actions: List<Action> = arrayListOf(),
        val text: String,
    ) : ExperienceComponent(id, style, actions)

    data class ButtonComponent(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        override val actions: List<Action> = arrayListOf(),
        val content: ExperienceComponent,
    ) : ExperienceComponent(id, style, actions)

    data class ImageComponent(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        override val actions: List<Action> = arrayListOf(),
        val url: String,
        val accessibilityLabel: String?,
        val intrinsicSize: ComponentSize?,
        val contentMode: ComponentContentMode = FILL,
    ) : ExperienceComponent(id, style, actions)

    data class VerticalStackComponent(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        override val actions: List<Action> = arrayListOf(),
        val items: List<ExperienceComponent>,
        val spacing: Int = 0,
    ) : ExperienceComponent(id, style, actions)

    data class HorizontalStackComponent(
        override val id: UUID,
        override val style: ComponentStyle = ComponentStyle(),
        override val actions: List<Action> = arrayListOf(),
        val items: List<ExperienceComponent>,
        val spacing: Int = 0,
        val distribution: ComponentDistribution = CENTER,
    ) : ExperienceComponent(id, style, actions)
}
