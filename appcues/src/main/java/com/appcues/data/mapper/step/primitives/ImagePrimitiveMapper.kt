package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.styling.mapComponentContentMode
import com.appcues.data.mapper.styling.mapComponentSize
import com.appcues.data.mapper.styling.mapComponentStyle
import com.appcues.data.model.ExperiencePrimitive.ImagePrimitive
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.ImagePrimitiveResponse

internal fun ImagePrimitiveResponse.mapImagePrimitive() = ImagePrimitive(
    id = id,
    url = imageUrl,
    accessibilityLabel = accessibilityLabel,
    style = style.mapComponentStyle(),
    intrinsicSize = intrinsicSize.mapComponentSize(),
    contentMode = mapComponentContentMode(contentMode),
    blurHash = blurHash,
)
