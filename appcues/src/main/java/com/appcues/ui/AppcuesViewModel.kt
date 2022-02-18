package com.appcues.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.appcues.data.model.Experience
import com.appcues.di.AppcuesKoinComponent
import com.appcues.logging.Logcues
import com.appcues.statemachine.StateMachine
import org.koin.core.component.inject

internal class AppcuesViewModel(
    override val scopeId: String,
    experience: Experience,
) : ViewModel(), AppcuesKoinComponent {

    private val logcues by inject<Logcues>()

    private val stateMachine by inject<StateMachine>()

    private val _experienceState = mutableStateOf(experience)

    val experienceState: State<Experience>
        get() = _experienceState

    init {
        logcues.info("$this init with experience $experience")
    }

    fun finish() {
        stateMachine.endExperience()
    }
}
