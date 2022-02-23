package com.appcues.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.appcues.action.ExperienceAction

internal val LocalAppcuesActions = staticCompositionLocalOf { AppcuesActions {} }

internal data class AppcuesActions(val onAction: ((ExperienceAction) -> Unit))
