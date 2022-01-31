package com.appcues.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.ImagePainter.State.Success
import coil.compose.rememberImagePainter
import com.appcues.domain.entity.ExperienceComponent.ImageComponent
import com.appcues.domain.entity.styling.ComponentColor
import com.appcues.domain.entity.styling.ComponentSize

@Composable
internal fun ImageComponent.Compose() {
    val imagePainter = rememberImagePainter(data = url, builder = { crossfade(true) })

    Image(
        painter = imagePainter,
        contentDescription = null, // contentDescription for image is missing
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize()
            .background(backgroundColor, isSystemInDarkTheme())
            .aspectRatio(imagePainter, intrinsicSize),
        contentScale = ContentScale.FillWidth,
    )
}

private fun Modifier.background(colorComponent: ComponentColor?, isDark: Boolean) = background(
    when {
        colorComponent == null -> Color.Transparent
        isDark -> Color(colorComponent.dark)
        else -> Color(colorComponent.light)
    }
)

// This value is immense for aspect ratio on purpose, so that the Image compose
// will probably have 1 pixel height while image is loading.
// Still needed for ImagePainter to trigger the image loader
private const val FALLBACK_ASPECT_RATIO = 1000f

@OptIn(ExperimentalCoilApi::class)
private fun Modifier.aspectRatio(imagePainter: ImagePainter, intrinsicSize: ComponentSize?) = aspectRatio(
    when {
        intrinsicSize != null -> calculateAspectRatio(intrinsicSize.width, intrinsicSize.height)
        imagePainter.state is Success -> with((imagePainter.state as Success).result.drawable) {
            calculateAspectRatio(intrinsicWidth, intrinsicHeight)
        }
        else -> FALLBACK_ASPECT_RATIO
    }
)

private fun calculateAspectRatio(width: Int, height: Int): Float {
    return width.toFloat() / height.toFloat()
}
