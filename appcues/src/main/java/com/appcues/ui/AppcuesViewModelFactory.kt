package com.appcues.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.appcues.data.model.Experience

internal class AppcuesViewModelFactory(
    private val scopeID: String,
    private val experience: Experience,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AppcuesViewModel(scopeID, experience) as T
    }
}
