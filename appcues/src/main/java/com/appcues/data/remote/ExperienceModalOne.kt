package com.appcues.data.remote

import com.appcues.domain.entity.Experience
import com.appcues.domain.entity.ExperienceComponent.ButtonComponent
import com.appcues.domain.entity.ExperienceComponent.HorizontalStackComponent
import com.appcues.domain.entity.ExperienceComponent.ImageComponent
import com.appcues.domain.entity.ExperienceComponent.TextComponent
import com.appcues.domain.entity.ExperienceComponent.VerticalStackComponent
import com.appcues.domain.entity.step.Step
import com.appcues.domain.entity.styling.ComponentColor
import com.appcues.domain.entity.styling.ComponentDistribution
import com.appcues.domain.entity.styling.ComponentHorizontalAlignment
import com.appcues.domain.entity.styling.ComponentSize
import com.appcues.domain.entity.styling.ComponentStyle
import com.appcues.domain.entity.styling.ComponentTextAlignment
import java.util.UUID

internal val experienceModalOne = Experience(
    id = UUID.randomUUID(),
    name = "Experience Modal One",
    actions = hashMapOf(),
    traits = listOf(),
    steps = arrayListOf(
        Step(
            id = UUID.randomUUID(),
            content = experienceModalOneContent(),
            traits = listOf(),
            actions = hashMapOf()
        )
    )
)

@Suppress("LongMethod")
internal fun experienceModalOneContent() = VerticalStackComponent(
    id = UUID.randomUUID(),
    style = ComponentStyle(
        marginBottom = 25,
    ),
    alignment = ComponentHorizontalAlignment.CENTER,
    items = arrayListOf(
        HorizontalStackComponent(
            id = UUID.randomUUID(),
            style = ComponentStyle(),
            distribution = ComponentDistribution.EQUAL,
            items = arrayListOf(
                ImageComponent(
                    id = UUID.randomUUID(),
                    url = "https://res.cloudinary.com/dnjrorsut/image/upload/v1635971825/98227/oh5drlvojb1spaetc1ol.jpg",
                    intrinsicSize = ComponentSize(width = 1920, height = 1280),
                    backgroundColor = ComponentColor(light = 0xFF8F8F8F, dark = 0xFF8F8F8F),
                    style = ComponentStyle()
                )
            )
        ),
        HorizontalStackComponent(
            id = UUID.randomUUID(),
            style = ComponentStyle(),
            distribution = ComponentDistribution.EQUAL,
            items = arrayListOf(
                TextComponent(
                    id = UUID.randomUUID(),
                    text = "Ready to make your\nworkflow simpler?",
                    style = ComponentStyle(
                        marginTop = 20,
                        marginBottom = 5,
                    ),
                    textColor = ComponentColor(light = 0xFF394455, dark = 0xFF394455),
                    textSize = 20,
                    textAlignment = ComponentTextAlignment.CENTER,
                )
            )
        ),
        HorizontalStackComponent(
            id = UUID.randomUUID(),
            style = ComponentStyle(),
            distribution = ComponentDistribution.EQUAL,
            items = arrayListOf(
                TextComponent(
                    id = UUID.randomUUID(),
                    style = ComponentStyle(
                        marginLeading = 30,
                        marginTop = 10,
                        marginTrailing = 30,
                        marginBottom = 15,
                    ),
                    text = "Take a few moments to learn how to best use our features.",
                    textSize = 17,
                    textColor = ComponentColor(light = 0xFF394455, dark = 0xFF394455),
                    textAlignment = ComponentTextAlignment.CENTER,
                )
            )
        ),
        HorizontalStackComponent(
            id = UUID.randomUUID(),
            style = ComponentStyle(),
            distribution = ComponentDistribution.EQUAL,
            items = arrayListOf(
                ButtonComponent(
                    id = UUID.randomUUID(),
                    style = ComponentStyle(
                        marginLeading = 18,
                        marginTop = 8,
                        marginTrailing = 18,
                        marginBottom = 8,
                        cornerRadius = 6,
                    ),
                    backgroundColors = arrayListOf(
                        ComponentColor(light = 0xFF5C5CFF, dark = 0xFF394455),
                        ComponentColor(light = 0xFF8960FF, dark = 0xFF394455),
                        ComponentColor(light = 0xFFAA90FF, dark = 0xFF394455)
                    ),
                    content = TextComponent(
                        id = UUID.randomUUID(),
                        text = "Button 1",
                        textSize = 17,
                        textColor = ComponentColor(light = 0xFFFFFFFF, dark = 0xFF394455)
                    )
                )
            )
        ),
    )
)
