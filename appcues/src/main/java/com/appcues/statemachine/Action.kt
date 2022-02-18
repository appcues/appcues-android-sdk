package com.appcues.statemachine

internal sealed class Action {
    class StartExperience(val experience: ExperiencePackage) : Action()
    class StartStep(val step: Int) : Action()
    class RenderStep : Action()
    class EndStep : Action()
    class EndExperience : Action()
    class Reset : Action() // not sure yet if we really need Reset in addition to EndExperience, TBD
}
