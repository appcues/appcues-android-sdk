package com.appcues.domain.entity

import android.os.Parcelable
import com.appcues.domain.entity.styling.ComponentColor
import com.appcues.domain.entity.styling.ComponentDistribution
import com.appcues.domain.entity.styling.ComponentHorizontalAlignment
import com.appcues.domain.entity.styling.ComponentShadow
import com.appcues.domain.entity.styling.ComponentSize
import com.appcues.domain.entity.styling.ComponentStyle
import com.appcues.domain.entity.styling.ComponentTextAlignment
import com.appcues.domain.entity.styling.ComponentVerticalAlignment
import kotlinx.parcelize.Parcelize
import java.util.UUID

internal sealed class ExperienceComponent(open val id: UUID) : Parcelable {

    @Parcelize
    data class TextComponent(
        override val id: UUID,
        val text: String,
        val textSize: Int,
        val textColor: ComponentColor,
        val style: ComponentStyle = ComponentStyle(),
        val textAlignment: ComponentTextAlignment = ComponentTextAlignment.CENTER,
        val fontName: String? = null,
        val lineSpacing: Int = 1,
    ) : ExperienceComponent(id)

    @Parcelize
    data class ButtonComponent(
        override val id: UUID,
        val content: ExperienceComponent,
        val style: ComponentStyle = ComponentStyle(),
        val colors: List<ComponentColor>? = null,
        val shadow: ComponentShadow? = null,
    ) : ExperienceComponent(id)

    @Parcelize
    data class ImageComponent(
        override val id: UUID,
        val url: String,
        val size: ComponentSize,
        val backgroundColor: ComponentColor,
        val style: ComponentStyle = ComponentStyle(),
    ) : ExperienceComponent(id)

    @Parcelize
    data class VerticalStackComponent(
        override val id: UUID,
        val items: List<ExperienceComponent>,
        val style: ComponentStyle = ComponentStyle(),
        val alignment: ComponentHorizontalAlignment = ComponentHorizontalAlignment.CENTER
    ) : ExperienceComponent(id)

    @Parcelize
    data class HorizontalStackComponent(
        override val id: UUID,
        val items: List<ExperienceComponent>,
        val style: ComponentStyle = ComponentStyle(),
        val alignment: ComponentVerticalAlignment = ComponentVerticalAlignment.TOP,
        val distribution: ComponentDistribution = ComponentDistribution.CENTER
    ) : ExperienceComponent(id)
}
