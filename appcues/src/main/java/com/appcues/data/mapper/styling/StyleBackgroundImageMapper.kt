package com.appcues.data.mapper.styling

import com.appcues.data.model.styling.ComponentBackgroundImage
import com.appcues.data.remote.appcues.response.styling.StyleBackgroundImageResponse

internal fun StyleBackgroundImageResponse.mapComponentBackgroundImage() = ComponentBackgroundImage(
    imageUrl = imageUrl,
    blurHash = blurHash,
    intrinsicSize = intrinsicSize?.mapComponentSize(),
    contentMode = mapComponentContentMode(contentMode),
    verticalAlignment = mapComponentVerticalAlignment(verticalAlignment),
    horizontalAlignment = mapComponentHorizontalAlignment(horizontalAlignment),
)
