package com.appcues.data.mapper.step

import com.appcues.data.remote.response.step.StepResponse
import com.appcues.domain.entity.step.Step

internal fun List<StepResponse>.mapToStep(transform: (StepResponse) -> Step) = map { transform(it) }
