package com.appcues.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import com.appcues.domain.entity.ExperienceComponent.ImageComponent
import com.skydoves.landscapist.glide.GlideImage

@Composable
internal fun ImageComponent.Compose() {
    if (LocalInspectionMode.current) {
        // if true, it means we are visualizing this composition from the Compose preview panel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(ratio = size.width.toFloat() / size.height.toFloat())
                .background(color = getBackgroundColor(isSystemInDarkTheme())),
        )
    } else {
        GlideImage(
            imageModel = url,
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(ratio = size.width.toFloat() / size.height.toFloat())
                .background(color = getBackgroundColor(isSystemInDarkTheme())),
            contentScale = ContentScale.FillWidth,
        )
    }
}

private fun ImageComponent.getBackgroundColor(isDark: Boolean): Color {
    return when {
        backgroundColor == null -> Color.Transparent
        isDark -> Color(backgroundColor.dark)
        else -> Color(backgroundColor.light)
    }
}
