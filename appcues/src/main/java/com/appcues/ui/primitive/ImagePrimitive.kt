package com.appcues.ui.primitive

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.appcues.data.model.ExperiencePrimitive.ImagePrimitive
import com.appcues.ui.LocalImageLoader
import com.appcues.ui.extensions.blurHashPlaceholder
import com.appcues.ui.extensions.getImageLoader
import com.appcues.ui.extensions.getImageRequest
import com.appcues.ui.extensions.styleImageAspect
import com.appcues.ui.extensions.styleSize
import com.appcues.ui.extensions.toImageAsyncContentScale
import com.appcues.ui.utils.rememberBlurHashDecoded

@Composable
internal fun ImagePrimitive.Compose(modifier: Modifier) {
    val context = LocalContext.current
    val decodedBlurHash = rememberBlurHashDecoded(blurHash = blurHash)

    AsyncImage(
        modifier = modifier.then(Modifier.styleSize(style, contentMode).styleImageAspect(this)),
        model = context.getImageRequest(url, contentMode),
        contentDescription = accessibilityLabel,
        imageLoader = LocalImageLoader.current ?: context.getImageLoader(),
        placeholder = context.blurHashPlaceholder(decodedBlurHash, intrinsicSize),
        contentScale = contentMode.toImageAsyncContentScale(),
        error = context.blurHashPlaceholder(decodedBlurHash, intrinsicSize),
    )
}
