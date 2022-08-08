package com.appcues.ui.primitive

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.appcues.data.model.ExperiencePrimitive.ImagePrimitive
import com.appcues.ui.LocalImageLoader
import com.appcues.ui.extensions.blurHashPlaceholder
import com.appcues.ui.extensions.getImageLoader
import com.appcues.ui.extensions.getImageRequest
import com.appcues.ui.extensions.imageAspectRatio
import com.appcues.ui.extensions.styleSize
import com.appcues.ui.extensions.toImageAsyncContentScale
import com.appcues.ui.utils.rememberBlurHashDecoded

@Composable
internal fun ImagePrimitive.Compose(modifier: Modifier, matchParentBox: BoxScope? = null) {
    val context = LocalContext.current
    val decodedBlurHash = rememberBlurHashDecoded(blurHash = blurHash)

    AsyncImage(
        modifier = modifier.applyImageModifier(this, matchParentBox),
        model = context.getImageRequest(url, contentMode),
        contentDescription = accessibilityLabel,
        imageLoader = LocalImageLoader.current ?: context.getImageLoader(),
        placeholder = context.blurHashPlaceholder(decodedBlurHash, intrinsicSize),
        contentScale = contentMode.toImageAsyncContentScale(),
        error = context.blurHashPlaceholder(decodedBlurHash, intrinsicSize),
    )
}

private fun Modifier.applyImageModifier(image: ImagePrimitive, matchParentBox: BoxScope?) = then(
    Modifier
        .styleSize(image.style, matchParentBox, image.contentMode)
        .animateContentSize()
        .imageAspectRatio(image.intrinsicSize, image.contentMode)
)
