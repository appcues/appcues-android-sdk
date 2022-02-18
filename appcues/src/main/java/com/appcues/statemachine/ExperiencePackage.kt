package com.appcues.statemachine

import android.content.Context
import com.appcues.data.model.Experience

// wrapper around the experience data that flows into the state machine,
// with at least the context to start an experience Activity (AppcuesActivity)
// and the scope it will need to get dependencies from Koin
internal data class ExperiencePackage(
    val context: Context,
    val scopeId: String,
    val experience: Experience
)
