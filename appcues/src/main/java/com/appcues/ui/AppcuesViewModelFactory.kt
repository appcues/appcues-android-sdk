package com.appcues.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.UUID

internal class AppcuesViewModelFactory(
    private val scopeID: String,
    private val experienceId: UUID,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AppcuesViewModel(scopeID, experienceId) as T
    }
}
