package com.appcues.ui

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcues.domain.GetExperienceUseCase
import com.appcues.domain.entity.Experience
import kotlinx.coroutines.launch
import java.util.UUID

internal class AppcuesViewModel(
    private val experienceId: UUID,
    private val getExperienceUseCase: GetExperienceUseCase
) : ViewModel() {

    val experience = mutableStateOf<Experience?>(null)

    init {
        viewModelScope.launch {
            experience.value = getExperienceUseCase(experienceId)
        }
    }
}
