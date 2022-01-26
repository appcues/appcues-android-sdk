package com.appcues.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.appcues.domain.entity.ExperienceComponent.HorizontalStackComponent
import com.appcues.domain.entity.styling.ComponentDistribution
import com.appcues.domain.entity.styling.ComponentVerticalAlignment
import com.appcues.domain.entity.styling.ComponentVerticalAlignment.BOTTOM
import com.appcues.domain.entity.styling.ComponentVerticalAlignment.CENTER
import com.appcues.domain.entity.styling.ComponentVerticalAlignment.TOP
import com.appcues.ui.extensions.ComposeEach
import com.appcues.ui.extensions.padding

@Composable
internal fun HorizontalStackComponent.Compose() {
    Row(
        modifier = Modifier.padding(style.padding()),
        horizontalArrangement = distribution.toHorizontalArrangement(),
        verticalAlignment = alignment.toVerticalAlignment()
    ) {
        items.ComposeEach()
    }
}

private fun ComponentDistribution.toHorizontalArrangement(): Arrangement.Horizontal {
    return when (this) {
        ComponentDistribution.CENTER -> Arrangement.Center
        ComponentDistribution.EQUAL -> Arrangement.SpaceAround
    }
}

private fun ComponentVerticalAlignment.toVerticalAlignment(): Alignment.Vertical {
    return when (this) {
        TOP -> Alignment.Top
        CENTER -> Alignment.CenterVertically
        BOTTOM -> Alignment.Bottom
    }
}
