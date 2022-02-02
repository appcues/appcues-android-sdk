package com.appcues.ui.component

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberImagePainter
import coil.size.OriginalSize
import com.appcues.domain.entity.ExperienceComponent.ImageComponent
import com.appcues.domain.entity.styling.ComponentSize
import com.appcues.ui.extensions.componentStyle

@Composable
internal fun ImageComponent.Compose() {
    val imagePainter = rememberImagePainter(
        data = url,
        builder = {
            crossfade(true)
            size(OriginalSize)
        }
    )

    Log.i("Appcues", "imageUrl -> $url")
    Image(
        painter = imagePainter,
        contentDescription = accessibilityLabel,
        modifier = Modifier
            .animateContentSize()
            .imageAspectRatio(intrinsicSize)
            .componentStyle(style, isSystemInDarkTheme()),
        contentScale = ContentScale.Inside,
    )
}

private fun Modifier.imageAspectRatio(intrinsicSize: ComponentSize?) = this.then(
    if (intrinsicSize != null) {
        Modifier
            .aspectRatio(calculateAspectRatio(intrinsicSize.width, intrinsicSize.height))
            .fillMaxSize()
    } else Modifier
)

private fun calculateAspectRatio(width: Int, height: Int): Float {
    return width.toFloat() / height.toFloat()
}
