package com.appcues.ui.primitive

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import coil.compose.AsyncImage
import com.appcues.data.model.ExperiencePrimitive.ImagePrimitive
import com.appcues.ui.composables.LocalImageLoader
import com.appcues.ui.composables.LocalLogcues
import com.appcues.ui.extensions.aspectRatio
import com.appcues.ui.extensions.blurHashPlaceholder
import com.appcues.ui.extensions.getBoxAlignment
import com.appcues.ui.extensions.getImageLoader
import com.appcues.ui.extensions.getImageRequest
import com.appcues.ui.extensions.styleSize
import com.appcues.ui.extensions.toImageAsyncContentScale
import com.appcues.ui.utils.rememberBlurHashDecoded

@Composable
internal fun ImagePrimitive.Compose(modifier: Modifier, matchParentBox: BoxScope? = null) {
    val context = LocalContext.current
    val logcues = LocalLogcues.current
    val decodedBlurHash = rememberBlurHashDecoded(blurHash = blurHash)

    AsyncImage(
        modifier = modifier
            .styleSize(style, matchParentBox, contentMode)
            .aspectRatio(contentMode, intrinsicSize, style, LocalDensity.current),
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
