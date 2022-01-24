package com.appcues.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.appcues.domain.entity.Experience

internal class AppcuesViewModel(
    experience: Experience
) : ViewModel() {

    private val _experienceState = mutableStateOf(experience)

    val experienceState: State<Experience>
        get() = _experienceState
}
