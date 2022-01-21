package com.appcues.ui

import androidx.compose.runtime.staticCompositionLocalOf
import java.util.UUID

internal val LocalAppcuesActions = staticCompositionLocalOf { AppcuesActions() }

internal data class AppcuesActions(val onClick: ((id: UUID) -> Unit)? = null)
