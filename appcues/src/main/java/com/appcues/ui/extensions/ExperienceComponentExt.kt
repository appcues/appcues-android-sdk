package com.appcues.ui.extensions

import androidx.compose.runtime.Composable
import com.appcues.domain.entity.ExperienceComponent
import com.appcues.domain.entity.ExperienceComponent.ButtonComponent
import com.appcues.domain.entity.ExperienceComponent.HorizontalStackComponent
import com.appcues.domain.entity.ExperienceComponent.ImageComponent
import com.appcues.domain.entity.ExperienceComponent.TextComponent
import com.appcues.domain.entity.ExperienceComponent.VerticalStackComponent
import com.appcues.ui.component.Compose
import java.util.UUID

@Composable
internal fun List<ExperienceComponent>.ComposeAll(onClick: (id: UUID) -> Unit) {
    forEach { it.ComposeExperience(onClick) }
}

@Composable
internal fun ExperienceComponent.ComposeExperience(onClick: (id: UUID) -> Unit) {
    when (this) {
        is ButtonComponent -> Compose(onClick = onClick)
        is ImageComponent -> Compose()
        is TextComponent -> Compose()
        is HorizontalStackComponent -> Compose(onClick = onClick)
        is VerticalStackComponent -> Compose(onClick = onClick)
    }
}
