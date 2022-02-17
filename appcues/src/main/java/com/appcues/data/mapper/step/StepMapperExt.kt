package com.appcues.data.mapper.step

import com.appcues.data.model.step.Step
import com.appcues.data.remote.response.step.StepResponse

internal fun List<StepResponse>.mapToStep(transform: (StepResponse) -> Step) = map { transform(it) }
