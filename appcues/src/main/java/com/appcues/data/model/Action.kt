package com.appcues.data.model

import com.appcues.action.ExperienceAction

internal data class Action(
    val on: Motion,
    val experienceAction: ExperienceAction,
) {

    enum class Motion {
        TAP, LONG_PRESS
    }
}
