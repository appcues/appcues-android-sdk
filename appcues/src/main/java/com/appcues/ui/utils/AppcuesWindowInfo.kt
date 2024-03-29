package com.appcues.ui.utils

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appcues.ui.utils.AppcuesWindowInfo.DeviceType.MOBILE
import com.appcues.ui.utils.AppcuesWindowInfo.DeviceType.TABLET
import com.appcues.ui.utils.AppcuesWindowInfo.Orientation.LANDSCAPE
import com.appcues.ui.utils.AppcuesWindowInfo.Orientation.PORTRAIT
import com.appcues.ui.utils.AppcuesWindowInfo.ScreenType.COMPACT
import com.appcues.ui.utils.AppcuesWindowInfo.ScreenType.EXPANDED
import com.appcues.ui.utils.AppcuesWindowInfo.ScreenType.MEDIUM

private const val WIDTH_COMPACT = 600
private const val WIDTH_MEDIUM = 840
private const val HEIGHT_COMPACT = 480
private const val HEIGHT_MEDIUM = 900

@Composable
internal fun rememberAppcuesWindowInfo(): AppcuesWindowInfo {
    val configuration = LocalConfiguration.current

    val screenWidthType = when {
        configuration.screenWidthDp < WIDTH_COMPACT -> COMPACT
        configuration.screenWidthDp < WIDTH_MEDIUM -> MEDIUM
        else -> EXPANDED
    }

    val screenHeightType = when {
        configuration.screenHeightDp < HEIGHT_COMPACT -> COMPACT
        configuration.screenHeightDp < HEIGHT_MEDIUM -> MEDIUM
        else -> EXPANDED
    }

    val orientation = when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> PORTRAIT
        else -> LANDSCAPE
    }

    val deviceType = when (orientation) {
        PORTRAIT -> when (screenWidthType) {
            COMPACT -> MOBILE
            MEDIUM -> TABLET
            EXPANDED -> TABLET
        }
        LANDSCAPE -> when (screenHeightType) {
            COMPACT -> MOBILE
            MEDIUM -> TABLET
            EXPANDED -> TABLET
        }
    }
    return remember(configuration) {
        AppcuesWindowInfo(
            screenWidthType = screenWidthType,
            screenHeightType = screenHeightType,
            widthDp = configuration.screenWidthDp.dp,
            heightDp = configuration.screenHeightDp.dp,
            orientation = orientation,
            deviceType = deviceType,
        )
    }
}

internal data class AppcuesWindowInfo(
    val screenWidthType: ScreenType,
    val screenHeightType: ScreenType,
    val widthDp: Dp,
    val heightDp: Dp,
    val orientation: Orientation,
    val deviceType: DeviceType,
) {

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
