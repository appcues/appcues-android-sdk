package com.appcues.ui.primitive

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.appcues.data.model.ExperiencePrimitive.ImagePrimitive
import com.appcues.ui.extensions.blurHashPlaceholder
import com.appcues.ui.extensions.getBoxAlignment
import com.appcues.ui.extensions.getImageLoader
import com.appcues.ui.extensions.getImageRequest
import com.appcues.ui.extensions.imageAspectRatio
import com.appcues.ui.extensions.styleSize
import com.appcues.ui.extensions.toImageAsyncContentScale
import com.appcues.ui.utils.rememberBlurHashDecoded

@Composable
internal fun ImagePrimitive.Compose(modifier: Modifier) {
    Box(
        modifier = modifier.then(
            Modifier
                .animateContentSize()
                .imageAspectRatio(intrinsicSize)
        )
    ) {
        val context = LocalContext.current
        val decodedBlurHash = rememberBlurHashDecoded(blurHash = blurHash)

        AsyncImage(
            modifier = Modifier.styleSize(style, true),
            model = context.getImageRequest(url, contentMode),
            contentDescription = accessibilityLabel,
            imageLoader = context.getImageLoader(),
            placeholder = context.blurHashPlaceholder(decodedBlurHash, intrinsicSize),
            contentScale = contentMode.toImageAsyncContentScale(),
            alignment = style.getBoxAlignment(),
        )
    }
}
