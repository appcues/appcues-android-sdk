package com.appcues.ui.utils

import android.content.Context
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder

internal class ImageLoaderWrapper(private val context: Context) {

    fun build(): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                if (VERSION.SDK_INT >= VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
                add(SvgDecoder.Factory())
            }.build()
    }
}
