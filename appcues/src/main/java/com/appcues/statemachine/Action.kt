package com.appcues.statemachine

import com.appcues.data.model.Experience

internal sealed class Action {
    data class StartExperience(val experience: Experience) : Action()
    data class StartStep(val stepReference: StepReference) : Action()
    object RenderStep : Action()
    // is EndStep action used anywhere? what should this do?
    // move to the next Step? (same as StartStep(index = next))?
    object EndStep : Action()
    object EndExperience : Action()
    object Reset : Action()
    data class ReportError(val error: Error) : Action()
}
