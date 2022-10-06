package com.appcues.data.mapper.step.primitives

import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.BlockPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.BoxPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.ButtonPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.EmbedPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.ImagePrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.OptionSelectPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.SpacerPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.StackPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.TextInputPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.TextPrimitiveResponse

internal fun PrimitiveResponse.mapPrimitive(): ExperiencePrimitive {
    return when (this) {
        is StackPrimitiveResponse -> mapStackPrimitive()
        is BoxPrimitiveResponse -> mapBoxPrimitive()
        is TextPrimitiveResponse -> mapTextPrimitive()
        is ButtonPrimitiveResponse -> mapButtonPrimitive()
        is ImagePrimitiveResponse -> mapImagePrimitive()
        is EmbedPrimitiveResponse -> mapEmbedPrimitive()
        is SpacerPrimitiveResponse -> mapSpacerPrimitive()
        is BlockPrimitiveResponse -> content.mapPrimitive()
        is OptionSelectPrimitiveResponse -> mapOptionSelectPrimitive()
        is TextInputPrimitiveResponse -> mapTextInputPrimitive()
    }
}
