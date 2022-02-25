package com.appcues.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import coil.compose.rememberImagePainter
import coil.size.OriginalSize
import coil.size.Scale
import com.appcues.data.model.ExperiencePrimitive.ImagePrimitive
import com.appcues.data.model.styling.ComponentContentMode
import com.appcues.data.model.styling.ComponentContentMode.FILL
import com.appcues.data.model.styling.ComponentContentMode.FIT
import com.appcues.data.model.styling.ComponentSize
import com.appcues.ui.LocalAppcuesActions
import com.appcues.ui.extensions.PrimitiveGestureProperties
import com.appcues.ui.extensions.componentStyle

@Composable
internal fun ImagePrimitive.Compose() {
    Box(
        modifier = Modifier
            .componentStyle(
                component = this,
                gestureProperties = PrimitiveGestureProperties(
                    onAction = LocalAppcuesActions.current.onAction,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(),
                    enabled = remember { true },
                    role = Role.Image,
                ),
                isDark = isSystemInDarkTheme(),
                noSizeFillMax = true
            )
            .imageAspectRatio(intrinsicSize)
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

private fun Modifier.imageAspectRatio(intrinsicSize: ComponentSize?) = this.then(
    // apply aspectRatio only when intrinsicSize is not null or any values is 0
    if (intrinsicSize != null && (intrinsicSize.width > 0 && intrinsicSize.height > 0)) {
        Modifier.aspectRatio(ratio = intrinsicSize.width.toFloat() / intrinsicSize.height.toFloat())
    } else Modifier
)
