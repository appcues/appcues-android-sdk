package com.appcues.domain.entity

import android.os.Parcelable
import com.appcues.domain.entity.styling.ComponentContentMode
import com.appcues.domain.entity.styling.ComponentContentMode.FILL
import com.appcues.domain.entity.styling.ComponentDistribution
import com.appcues.domain.entity.styling.ComponentSize
import com.appcues.domain.entity.styling.ComponentStyle
import kotlinx.parcelize.Parcelize
import java.util.UUID

internal sealed class ExperienceComponent(open val id: UUID) : Parcelable {

    @Parcelize
    data class TextComponent(
        override val id: UUID,
        val text: String,
        val style: ComponentStyle = ComponentStyle(),
    ) : ExperienceComponent(id)

    @Parcelize
    data class ButtonComponent(
        override val id: UUID,
        val content: ExperienceComponent,
        val style: ComponentStyle = ComponentStyle(),
    ) : ExperienceComponent(id)

    @Parcelize
    data class ImageComponent(
        override val id: UUID,
        val url: String,
        val accessibilityLabel: String?,
        val intrinsicSize: ComponentSize?,
        val contentMode: ComponentContentMode = FILL,
        val style: ComponentStyle = ComponentStyle(),
    ) : ExperienceComponent(id)

    @Parcelize
    data class VerticalStackComponent(
        override val id: UUID,
        val items: List<ExperienceComponent>,
        val style: ComponentStyle = ComponentStyle(),
    ) : ExperienceComponent(id)

    @Parcelize
    data class HorizontalStackComponent(
        override val id: UUID,
        val items: List<ExperienceComponent>,
        val distribution: ComponentDistribution,
        val style: ComponentStyle = ComponentStyle(),
    ) : ExperienceComponent(id)
}
