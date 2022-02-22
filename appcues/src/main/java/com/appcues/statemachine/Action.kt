package com.appcues.statemachine

import com.appcues.data.model.Experience

internal sealed class Action {
    class StartExperience(val experience: Experience) : Action()
    class StartStep(val step: Int) : Action()
    class RenderStep : Action()
    class EndStep : Action()
    class EndExperience : Action()
    class Reset : Action() // not sure yet if we really need Reset in addition to EndExperience, TBD
}
