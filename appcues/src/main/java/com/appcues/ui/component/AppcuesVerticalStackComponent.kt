package com.appcues.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.appcues.domain.entity.ExperienceComponent.VerticalStackComponent
import com.appcues.domain.entity.styling.ComponentHorizontalAlignment
import com.appcues.domain.entity.styling.ComponentHorizontalAlignment.CENTER
import com.appcues.domain.entity.styling.ComponentHorizontalAlignment.LEADING
import com.appcues.domain.entity.styling.ComponentHorizontalAlignment.TRAILING
import com.appcues.ui.extensions.ComposeAll
import java.util.UUID

@Composable
internal fun VerticalStackComponent.Compose(onClick: (id: UUID) -> Unit) {
    Column(
        horizontalAlignment = alignment.toHorizontalAlignment()
    ) {
        items.ComposeAll(onClick = onClick)
    }
}

private fun ComponentHorizontalAlignment.toHorizontalAlignment(): Alignment.Horizontal {
    return when (this) {
        LEADING -> Alignment.Start
        CENTER -> Alignment.CenterHorizontally
        TRAILING -> Alignment.End
    }
}
