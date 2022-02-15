package com.appcues.ui

import androidx.compose.runtime.compositionLocalOf
import com.appcues.domain.entity.action.Action

internal val LocalAppcuesActions = compositionLocalOf { AppcuesActions() }

internal data class AppcuesActions(val on: ((List<Action>) -> Unit)? = null)
