package com.appcues.ui.component

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
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
import com.appcues.ui.extensions.styleSize

private const val PLACEHOLDER_SIZE_PX = 32

@Composable
internal fun ImagePrimitive.Compose() {
    val blurPlaceholder = remember(blurHash) { BlurHashDecoder.decode(blurHash, PLACEHOLDER_SIZE_PX, PLACEHOLDER_SIZE_PX) }
    val context = LocalContext.current

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
            )
            .animateContentSize()
            .imageAspectRatio(intrinsicSize),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = getImageRequest(context, url, contentMode),
            contentDescription = accessibilityLabel,
            imageLoader = getImageLoader(context),
            placeholder = blurPlaceholder?.let { BitmapPainter(BitmapDrawable(context.resources, it).bitmap.asImageBitmap()) },
            contentScale = contentMode.toContentScale(),
            modifier = Modifier.styleSize(style, true)
        )
    }
}

private fun ComponentContentMode.toContentScale() = when (this) {
    FILL -> ContentScale.Crop
    FIT -> ContentScale.Fit
}

private fun getImageLoader(context: Context): ImageLoader {
    return ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= VERSION_CODES.P) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
            add(SvgDecoder.Factory())
        }.build()
}

private fun getImageRequest(context: Context, url: String, contentMode: ComponentContentMode): ImageRequest {
    return ImageRequest.Builder(context)
        .data(url)
        .crossfade(true)
        .size(Size.ORIGINAL)
        .scale(contentMode.toCoilScale())
        .build()
}

private fun ComponentContentMode.toCoilScale() = when (this) {
    FILL -> Scale.FILL
    FIT -> Scale.FIT
}
