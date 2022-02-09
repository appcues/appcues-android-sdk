package com.appcues.ui.extensions

import androidx.compose.runtime.Composable
import com.appcues.domain.entity.ExperienceComponent
import com.appcues.domain.entity.ExperienceComponent.ButtonComponent
import com.appcues.domain.entity.ExperienceComponent.HorizontalStackComponent
import com.appcues.domain.entity.ExperienceComponent.ImageComponent
import com.appcues.domain.entity.ExperienceComponent.TextComponent
import com.appcues.domain.entity.ExperienceComponent.VerticalStackComponent
import com.appcues.ui.component.Compose

@Composable
internal fun ExperienceComponent.Compose() = when (this) {
    is ButtonComponent -> Compose()
    is ImageComponent -> Compose()
    is TextComponent -> Compose()
    is HorizontalStackComponent -> Compose()
    is VerticalStackComponent -> Compose()
}
