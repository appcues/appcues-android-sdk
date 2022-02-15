package com.appcues.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcues.di.AppcuesKoinComponent
import com.appcues.domain.entity.Experience
import com.appcues.domain.entity.action.Action
import com.appcues.experience.ExperienceController
import com.appcues.ui.AppcuesViewModel.StepState.InFlight
import com.appcues.ui.AppcuesViewModel.StepState.ShowExperience
import com.appcues.ui.AppcuesViewModel.StepState.StepError
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import java.util.UUID

internal class AppcuesViewModel(
    override val scopeId: String,
    private val experienceId: UUID,
) : ViewModel(), AppcuesKoinComponent {

    private val experienceController by inject<ExperienceController>()

    private val _experienceState = mutableStateOf<StepState>(InFlight)

    val experienceStepState: State<StepState>
        get() = _experienceState

    sealed class StepState {
        object InFlight : StepState()
        data class ShowExperience(val experience: Experience) : StepState()
        object StepError : StepState()
    }

    init {
        _experienceState.value = experienceController.getExperience(experienceId)?.let {
            ShowExperience(it)
        } ?: StepError
    }

    fun onAction(actions: List<Action>) {
        viewModelScope.launch {
            experienceController.executeActions(experienceId, actions)
        }
    }
}
