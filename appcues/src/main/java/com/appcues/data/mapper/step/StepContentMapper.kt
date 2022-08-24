package com.appcues.data.mapper.step

import com.appcues.data.mapper.step.primitives.BoxPrimitiveMapper
import com.appcues.data.mapper.step.primitives.ButtonPrimitiveMapper
import com.appcues.data.mapper.step.primitives.EmbedPrimitiveMapper
import com.appcues.data.mapper.step.primitives.ImagePrimitiveMapper
import com.appcues.data.mapper.step.primitives.StackPrimitiveMapper
import com.appcues.data.mapper.step.primitives.TextPrimitiveMapper
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.BlockPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.BoxPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.ButtonPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.EmbedPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.ImagePrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.OptionSelectPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.StackPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.TextInputPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.TextPrimitiveResponse

internal class StepContentMapper(
    private val stackMapper: StackPrimitiveMapper,
    private val boxMapper: BoxPrimitiveMapper,
    private val textMapper: TextPrimitiveMapper,
    private val buttonMapper: ButtonPrimitiveMapper,
    private val imageMapper: ImagePrimitiveMapper,
    private val embedMapper: EmbedPrimitiveMapper,
) {

    fun map(from: PrimitiveResponse): ExperiencePrimitive = when (from) {
        is StackPrimitiveResponse -> stackMapper.map(from) { map(it) }
        is BoxPrimitiveResponse -> boxMapper.map(from) { map(it) }
        is TextPrimitiveResponse -> textMapper.map(from)
        is ButtonPrimitiveResponse -> buttonMapper.map(from) { map(it) }
        is ImagePrimitiveResponse -> imageMapper.map(from)
        is EmbedPrimitiveResponse -> embedMapper.map(from)
        is BlockPrimitiveResponse -> map(from.content)
        is OptionSelectPrimitiveResponse -> throw NotImplementedError()
        is TextInputPrimitiveResponse -> throw NotImplementedError()
    }
}
