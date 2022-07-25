package com.appcues.ui.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import com.appcues.data.model.styling.ComponentContentMode
import com.appcues.data.model.styling.ComponentContentMode.FILL
import com.appcues.data.model.styling.ComponentContentMode.FIT
import com.appcues.data.model.styling.ComponentSize

internal fun Context.getImageLoader(): ImageLoader {
    return ImageLoader.Builder(this)
        .components {
            if (VERSION.SDK_INT >= VERSION_CODES.P) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
            add(SvgDecoder.Factory())
        }.build()
}

internal fun Context.getImageRequest(url: String, contentMode: ComponentContentMode): ImageRequest {
    return ImageRequest.Builder(this)
        .data(url)
        .crossfade(true)
        .size(Size.ORIGINAL)
        .scale(contentMode.toCoilScale())
        .build()
}

internal fun Context.blurHashPlaceholder(decodedBlurHash: Bitmap?, intrinsicSize: ComponentSize?): Painter? {
    return if (decodedBlurHash != null) BitmapPainter(
        BitmapDrawable(resources, decodedBlurHash).bitmap
            .let { bitmap ->
                if (intrinsicSize != null) {
                    // if we know the intrinsicSize we scale the place holder accordingly
                    Bitmap.createScaledBitmap(bitmap, intrinsicSize.width.toInt(), intrinsicSize.height.toInt(), false)
                } else bitmap
            }
            .asImageBitmap()
    )
    else
        null
}

internal fun ComponentContentMode.toImageAsyncContentScale() = when (this) {
    FILL -> ContentScale.Crop
    FIT -> ContentScale.Fit
}

internal fun ComponentContentMode.toCoilScale() = when (this) {
    FILL -> Scale.FILL
    FIT -> Scale.FIT
}
