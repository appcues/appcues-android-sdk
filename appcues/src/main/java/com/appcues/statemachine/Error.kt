package com.appcues.statemachine

import com.appcues.data.model.Experience

internal data class Error(
    val experience: Experience,
    val step: Int?,
    val message: String,
)
