package com.appcues.trait.appcues

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfig
import com.appcues.trait.AppcuesTraitAnimatedVisibility
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.PresentingTrait
import com.appcues.ui.AppcuesOverlayViewManager
import com.appcues.ui.modal.dialogEnterTransition
import com.appcues.ui.modal.dialogExitTransition
import com.appcues.ui.utils.AppcuesWindowInfo
import com.appcues.ui.utils.AppcuesWindowInfo.ScreenType.COMPACT
import com.appcues.ui.utils.AppcuesWindowInfo.ScreenType.EXPANDED
import com.appcues.ui.utils.AppcuesWindowInfo.ScreenType.MEDIUM
import com.appcues.ui.utils.rememberAppcuesWindowInfo
import org.koin.core.scope.Scope

private val MAX_WIDTH_COMPACT_DP = 400.dp
private val MAX_WIDTH_MEDIUM_DP = 480.dp
private val MAX_WIDTH_EXPANDED_DP = 560.dp
private val MAX_HEIGHT_COMPACT_DP = Dp.Unspecified
private val MAX_HEIGHT_MEDIUM_DP = 800.dp
private val MAX_HEIGHT_EXPANDED_DP = 900.dp
private const val SCREEN_PADDING = 0.05

internal class TooltipTrait(
    override val config: AppcuesConfigMap,
    val scope: Scope,
) : PresentingTrait, ContentWrappingTrait {

    companion object {

        const val TYPE = "@appcues/tooltip"
    }

    private val xPath: String? = config.getConfig("path")

    @Composable
    override fun WrapContent(content: @Composable (hasFixedHeight: Boolean, contentPadding: PaddingValues?) -> Unit) {
        val windowInfo = rememberAppcuesWindowInfo()
        val configuration = LocalConfiguration.current
        val dialogHorizontalMargin = (configuration.screenWidthDp * SCREEN_PADDING).dp
        val dialogVerticalMargin = (configuration.screenHeightDp * SCREEN_PADDING).dp
        val isDark = isSystemInDarkTheme()

        val maxWidth = maxWidthDerivedOf(windowInfo)
        val maxHeight = maxHeightDerivedOf(windowInfo)

        AppcuesTraitAnimatedVisibility(
            enter = dialogEnterTransition(),
            exit = dialogExitTransition(),
        ) {
            Surface(
                modifier = Modifier
                    .sizeIn(maxWidth = maxWidth.value, maxHeight = maxHeight.value)
                    // container padding based on screen size
                    .padding(horizontal = dialogHorizontalMargin, vertical = dialogVerticalMargin),
                content = { content(false, PaddingValues(0.dp)) },
            )
        }
    }

    override fun present() {
        // get targetable view info from trait properties
        // check to see if view is visible
        // if yes then we present and return true so statemachine knows to wait for the viewModel
        // if not then we return false and check this on statemachine to return Failure("Experience not presented")


        // this is probably not the best
        if (xPath == null) return

        AppcuesOverlayViewManager(scope = scope).addView()
    }
}

private fun maxWidthDerivedOf(windowInfo: AppcuesWindowInfo): State<Dp> {
    return derivedStateOf {
        when (windowInfo.screenWidthType) {
            COMPACT -> MAX_WIDTH_COMPACT_DP
            MEDIUM -> MAX_WIDTH_MEDIUM_DP
            EXPANDED -> MAX_WIDTH_EXPANDED_DP
        }
    }
}

private fun maxHeightDerivedOf(windowInfo: AppcuesWindowInfo): State<Dp> {
    return derivedStateOf {
        when (windowInfo.screenHeightType) {
            COMPACT -> MAX_HEIGHT_COMPACT_DP
            MEDIUM -> MAX_HEIGHT_MEDIUM_DP
            EXPANDED -> MAX_HEIGHT_EXPANDED_DP
        }
    }
}
