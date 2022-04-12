package com.appcues.statemachine

import com.appcues.data.model.Experience

internal sealed class Action {
    data class StartExperience(val experience: Experience) : Action()
    data class StartStep(val stepReference: StepReference) : Action()
    object RenderStep : Action()
    data class EndExperience(val destroyed: Boolean, val markComplete: Boolean = false) : Action()
    object Reset : Action()
    data class ReportError(val error: Error) : Action()
}
