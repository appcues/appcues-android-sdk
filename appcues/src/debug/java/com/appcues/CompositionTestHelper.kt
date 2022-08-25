package com.appcues

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import coil.ImageLoader
import com.appcues.data.MoshiConfiguration
import com.appcues.data.mapper.step.mapPrimitive
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse
import com.appcues.logging.Logcues
import com.appcues.ui.LocalImageLoader
import com.appcues.ui.LocalLogcues
import com.appcues.ui.primitive.Compose

@Composable
fun ComposeContent(json: String, imageLoader: ImageLoader) {
    val response = MoshiConfiguration.moshi.adapter(PrimitiveResponse::class.java).fromJson(json)
    val primitive = response!!.mapPrimitive()

    CompositionLocalProvider(
        LocalImageLoader provides imageLoader,
        LocalLogcues provides Logcues(LoggingLevel.DEBUG)
    ) {
        primitive.Compose()
    }
}
