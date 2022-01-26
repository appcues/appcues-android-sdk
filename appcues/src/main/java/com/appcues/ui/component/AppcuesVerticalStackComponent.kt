package com.appcues.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.appcues.domain.entity.ExperienceComponent.VerticalStackComponent
import com.appcues.domain.entity.styling.ComponentHorizontalAlignment
import com.appcues.domain.entity.styling.ComponentHorizontalAlignment.CENTER
import com.appcues.domain.entity.styling.ComponentHorizontalAlignment.LEADING
import com.appcues.domain.entity.styling.ComponentHorizontalAlignment.TRAILING
import com.appcues.ui.extensions.ComposeEach
import com.appcues.ui.extensions.padding

@Composable
internal fun VerticalStackComponent.Compose() {
    Column(
        modifier = Modifier.padding(style.padding()),
        horizontalAlignment = alignment.toHorizontalAlignment()
    ) {
        items.ComposeEach()
    }
}

private fun ComponentHorizontalAlignment.toHorizontalAlignment(): Alignment.Horizontal {
    return when (this) {
        LEADING -> Alignment.Start
        CENTER -> Alignment.CenterHorizontally
        TRAILING -> Alignment.End
    }
}
