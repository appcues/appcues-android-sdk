package com.appcues.statemachine

import com.appcues.data.model.Experience
import com.appcues.data.model.StepReference

internal sealed class Action {
    data class StartExperience(val experience: Experience) : Action()
    data class MoveToStep(val stepReference: StepReference) : Action()
    data class StartStep(val nextFlatStepIndex: Int, val nextStepContainerIndex: Int, val isDifferentContainer: Boolean) : Action()
    data class RenderStep(val metadata: Map<String, Any?>) : Action()
    data class EndExperience(
        val markComplete: Boolean,
        val destroyed: Boolean,
        val trackAnalytics: Boolean = true,
    ) : Action()

    object Reset : Action()
    data class ReportError(val error: Error, val fatal: Boolean) : Action()
}
