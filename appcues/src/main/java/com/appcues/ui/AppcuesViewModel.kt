package com.appcues.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.appcues.di.AppcuesKoinComponent
import com.appcues.domain.entity.Experience
import com.appcues.logging.Logcues
import org.koin.core.component.inject

internal class AppcuesViewModel(
    override val scopeId: String,
    experience: Experience,
) : ViewModel(), AppcuesKoinComponent {

    private val logcues by inject<Logcues>()

    private val _experienceState = mutableStateOf(experience)

    val experienceState: State<Experience>
        get() = _experienceState

    init {
        logcues.i("$this init with experience $experience")
    }
}
