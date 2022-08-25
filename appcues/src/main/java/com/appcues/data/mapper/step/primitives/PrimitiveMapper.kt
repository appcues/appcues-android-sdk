package com.appcues.data.mapper.step

import com.appcues.data.mapper.step.primitives.mapBoxPrimitive
import com.appcues.data.mapper.step.primitives.mapButtonPrimitive
import com.appcues.data.mapper.step.primitives.mapEmbedPrimitive
import com.appcues.data.mapper.step.primitives.mapImagePrimitive
import com.appcues.data.mapper.step.primitives.mapOptionSelectPrimitive
import com.appcues.data.mapper.step.primitives.mapStackPrimitive
import com.appcues.data.mapper.step.primitives.mapTextInputPrimitive
import com.appcues.data.mapper.step.primitives.mapTextPrimitive
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

internal fun PrimitiveResponse.mapPrimitive(): ExperiencePrimitive {
    return when (this) {
        is StackPrimitiveResponse -> mapStackPrimitive()
        is BoxPrimitiveResponse -> mapBoxPrimitive()
        is TextPrimitiveResponse -> mapTextPrimitive()
        is ButtonPrimitiveResponse -> mapButtonPrimitive()
        is ImagePrimitiveResponse -> mapImagePrimitive()
        is EmbedPrimitiveResponse -> mapEmbedPrimitive()
        is BlockPrimitiveResponse -> content.mapPrimitive()
        is OptionSelectPrimitiveResponse -> mapOptionSelectPrimitive()
        is TextInputPrimitiveResponse -> mapTextInputPrimitive()
    }
}
