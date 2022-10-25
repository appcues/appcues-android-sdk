package com.appcues.ui.primitive

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import coil.compose.AsyncImage
import com.appcues.data.model.ExperiencePrimitive.ImagePrimitive
import com.appcues.ui.composables.LocalImageLoader
import com.appcues.ui.composables.LocalLogcues
import com.appcues.ui.composables.LocalStackScope
import com.appcues.ui.composables.StackScope
import com.appcues.ui.extensions.blurHashPlaceholder
import com.appcues.ui.extensions.getBoxAlignment
import com.appcues.ui.extensions.getImageLoader
import com.appcues.ui.extensions.getImageRequest
import com.appcues.ui.extensions.imageAspectRatio
import com.appcues.ui.extensions.styleSize
import com.appcues.ui.extensions.toImageAsyncContentScale
import com.appcues.ui.utils.rememberBlurHashDecoded

@Composable
internal fun ImagePrimitive.Compose(modifier: Modifier, matchParentBox: BoxScope? = null) {
    val context = LocalContext.current
    val logcues = LocalLogcues.current
    val stackScope = LocalStackScope.current
    val decodedBlurHash = rememberBlurHashDecoded(blurHash = blurHash)

    AsyncImage(
        modifier = modifier.applyImageModifier(this, matchParentBox, stackScope, LocalDensity.current),
        model = context.getImageRequest(url, contentMode),
        contentDescription = accessibilityLabel,
        imageLoader = LocalImageLoader.current ?: context.getImageLoader(),
        placeholder = context.blurHashPlaceholder(decodedBlurHash, intrinsicSize),
        contentScale = contentMode.toImageAsyncContentScale(),
        alignment = style.getBoxAlignment(),
        error = context.blurHashPlaceholder(decodedBlurHash, intrinsicSize),
        onError = {
            logcues.error(it.result.throwable)
        },
    )
}

private fun Modifier.applyImageModifier(image: ImagePrimitive, matchParentBox: BoxScope?, stackScope: StackScope, density: Density) = then(
    Modifier
        .styleSize(image.style, matchParentBox, image.contentMode)
        .imageAspectRatio(image.intrinsicSize, stackScope, image.style, density, image.contentMode)
)
