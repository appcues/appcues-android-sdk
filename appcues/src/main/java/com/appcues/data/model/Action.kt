package com.appcues.data.model

import com.appcues.action.ExperienceAction

internal data class Action(
    val on: Trigger,
    val experienceAction: ExperienceAction,
) {

    enum class Trigger {
        TAP, LONG_PRESS, NAVIGATE
    }
}
