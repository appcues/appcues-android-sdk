package com.appcues.statemachine

import com.appcues.data.model.Experience

internal sealed class SideEffect {
    data class ContinuationEffect(val action: Action) : SideEffect()
    data class PresentContainerEffect(val experience: Experience, val step: Int) : SideEffect()
    data class ReportErrorEffect(val error: Error) : SideEffect()
}
