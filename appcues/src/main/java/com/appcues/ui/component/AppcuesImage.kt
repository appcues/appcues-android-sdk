package com.appcues.ui.component

import android.graphics.drawable.BitmapDrawable
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import coil.ImageLoader
import coil.compose.rememberImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.size.OriginalSize
import coil.size.Scale
import com.appcues.data.model.ExperiencePrimitive.ImagePrimitive
import com.appcues.data.model.styling.ComponentContentMode
import com.appcues.data.model.styling.ComponentContentMode.FILL
import com.appcues.data.model.styling.ComponentContentMode.FIT
import com.appcues.ui.LocalAppcuesActionDelegate
import com.appcues.ui.LocalAppcuesActions
import com.appcues.ui.blurhash.BlurHashDecoder
import com.appcues.ui.extensions.PrimitiveGestureProperties
import com.appcues.ui.extensions.imageAspectRatio
import com.appcues.ui.extensions.primitiveStyle

private const val PLACEHOLDER_SIZE_PX = 32

@Composable
internal fun ImagePrimitive.Compose() {
    val blurPlaceholder = BlurHashDecoder.decode(blurHash, PLACEHOLDER_SIZE_PX, PLACEHOLDER_SIZE_PX)
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .componentRegistry {
            if (SDK_INT >= VERSION_CODES.P) {
                add(ImageDecoderDecoder(context))
            } else {
                add(GifDecoder())
            }
            add(SvgDecoder(context))
        }
        .build()

    Box(
        modifier = Modifier
            .primitiveStyle(
                component = this,
                gestureProperties = PrimitiveGestureProperties(
                    onAction = LocalAppcuesActionDelegate.current.onAction,
                    actions = LocalAppcuesActions.current,
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
            modifier = Modifier.matchParentSize(),
            painter = rememberImagePainter(
                imageLoader = imageLoader,
                data = url,
                builder = {
                    if (blurPlaceholder != null) {
                        placeholder(BitmapDrawable(LocalContext.current.resources, blurPlaceholder))
                    }

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
