package com.appcues

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import coil.ImageLoader
import com.appcues.data.MoshiConfiguration
import com.appcues.data.mapper.step.StepContentMapper
import com.appcues.data.mapper.step.primitives.BoxPrimitiveMapper
import com.appcues.data.mapper.step.primitives.ButtonPrimitiveMapper
import com.appcues.data.mapper.step.primitives.EmbedPrimitiveMapper
import com.appcues.data.mapper.step.primitives.ImagePrimitiveMapper
import com.appcues.data.mapper.step.primitives.StackPrimitiveMapper
import com.appcues.data.mapper.step.primitives.TextPrimitiveMapper
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.remote.response.step.StepContentResponse
import com.appcues.ui.LocalImageLoader
import com.appcues.ui.primitive.Compose

@Composable
fun ComposeContent(json: String, imageLoader: ImageLoader) {
    val response = MoshiConfiguration.moshi.adapter(StepContentResponse::class.java).fromJson(json)
    val mapper = StepContentMapper(
        stackMapper = StackPrimitiveMapper(),
        boxMapper = BoxPrimitiveMapper(),
        textMapper = TextPrimitiveMapper(),
        buttonMapper = ButtonPrimitiveMapper(),
        imageMapper = ImagePrimitiveMapper(),
        embedMapper = EmbedPrimitiveMapper()
    )
    val primitive = mapper.map(response!!)

    CompositionLocalProvider(LocalImageLoader provides imageLoader) {
        primitive.Compose()
    }
}
