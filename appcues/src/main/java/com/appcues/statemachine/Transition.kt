package com.appcues.statemachine

import com.appcues.data.model.Experience

internal open class Transition(val state: State? = null, val continuation: Action? = null, val error: Error? = null) {
    data class ExperienceActiveError(val experience: Experience) :
        Transition(error = Error(experience, null, "experience already active"))
}
