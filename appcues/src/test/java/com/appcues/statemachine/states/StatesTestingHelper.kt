package com.appcues.statemachine.states

import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.MoveToStep
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.Action.ReportError
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.Error
import com.appcues.statemachine.SideEffect
import com.appcues.statemachine.State
import com.appcues.statemachine.Transition
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk

internal fun State.assertIgnoredActions(list: List<Action>) {
    list.forEach { assertThat(take(it)).isNull() }
}

internal fun Transition?.assertState(state: State) {
    if (this != null) {
        assertThat(newState).isEqualTo(state)
    } else {
        assert(false) { "transition should not be null" }
    }
}

internal fun Transition?.assertEffect(effect: SideEffect?) {
    if (this != null) {
        assertThat(error).isNull()
        assertThat(sideEffect).isEqualTo(effect)
    } else {
        assert(false) { "transition should not be null" }
    }
}

internal fun Transition?.assertError(error: Error) {
    if (this != null) {
        assertThat(sideEffect).isNull()
        assertThat(this.error).isEqualTo(error)
    } else {
        assert(false) { "transition should not be null" }
    }
}

internal object MockActions {

    val StartExperience = mockk<StartExperience>(relaxed = true)
    val StartStep = mockk<StartStep>(relaxed = true)
    val MoveToStep = mockk<MoveToStep>(relaxed = true)
    val RenderStep = mockk<RenderStep>(relaxed = true)
    val EndExperience = mockk<EndExperience>(relaxed = true)
    val Reset = mockk<Reset>(relaxed = true)
    val ReportError = mockk<ReportError>(relaxed = true)
}
