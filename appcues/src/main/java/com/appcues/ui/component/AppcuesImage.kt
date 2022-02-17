package com.appcues.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberImagePainter
import coil.size.OriginalSize
import coil.size.Scale
import com.appcues.data.model.ExperiencePrimitive.ImagePrimitive
import com.appcues.data.model.styling.ComponentContentMode
import com.appcues.data.model.styling.ComponentContentMode.FILL
import com.appcues.data.model.styling.ComponentContentMode.FIT
import com.appcues.data.model.styling.ComponentSize
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.ui.extensions.componentStyle

@Composable
internal fun ImagePrimitive.Compose() {
    Box(
        modifier = Modifier
            .componentStyle(style, isSystemInDarkTheme(), noSizeFillMax = true)
            .imageAspectRatio(intrinsicSize, style)
            .animateContentSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberImagePainter(
                data = url,
                builder = {
                    crossfade(true)
                    size(OriginalSize)
                    scale(contentMode.toCoilScale())
                }
            ),
            contentScale = contentMode.toContentScale(),
            contentDescription = accessibilityLabel
        )
    }
}

private fun ComponentContentMode.toCoilScale() = when (this) {
    FILL -> Scale.FILL
    FIT -> Scale.FIT
}

private fun ComponentContentMode.toContentScale() = when (this) {
    FILL -> ContentScale.Crop
    FIT -> ContentScale.Fit
}

private fun Modifier.imageAspectRatio(intrinsicSize: ComponentSize?, style: ComponentStyle) = this.then(
    if (intrinsicSize != null && style.width == null && style.height == null) {
        Modifier.aspectRatio(ratio = intrinsicSize.width.toFloat() / intrinsicSize.height.toFloat())
    } else Modifier
)
