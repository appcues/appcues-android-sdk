package com.appcues.data.model.styling

import com.appcues.data.model.styling.ComponentContentMode.FIT
import com.appcues.data.model.styling.ComponentStyle.ComponentHorizontalAlignment
import com.appcues.data.model.styling.ComponentStyle.ComponentVerticalAlignment

internal data class ComponentBackgroundImage(
    val imageUrl: String,
    val blurHash: String? = null,
    val intrinsicSize: ComponentSize? = null,
    val contentMode: ComponentContentMode = FIT,
    val verticalAlignment: ComponentVerticalAlignment? = null,
    val horizontalAlignment: ComponentHorizontalAlignment? = null,
)
