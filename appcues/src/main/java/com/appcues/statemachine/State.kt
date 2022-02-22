package com.appcues.statemachine

import com.appcues.data.model.Experience
import com.appcues.di.AppcuesKoinComponent

internal interface State : AppcuesKoinComponent {
    val experience: Experience?
    fun handleAction(action: Action): State
}
