package com.appcues.ui.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.geometry.Rect

internal data class AppcuesWindowInfo(
    val screenWidthType: ScreenType,
    val screenHeightType: ScreenType,
    val safeRect: Rect,
    val safeInsets: WindowInsets,
    val orientation: Orientation,
    val deviceType: DeviceType,
) {
    companion object {
        const val WIDTH_COMPACT = 600
        const val WIDTH_MEDIUM = 840
        const val HEIGHT_COMPACT = 480
        const val HEIGHT_MEDIUM = 900
    }

    enum class ScreenType {
        COMPACT, MEDIUM, EXPANDED
    }

    enum class Orientation {
        PORTRAIT, LANDSCAPE
    }

    enum class DeviceType {
        MOBILE, TABLET
    }
}
