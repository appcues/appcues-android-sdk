package com.appcues.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

internal class AppcuesViewModelFactory(
    private val scopeID: String,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AppcuesViewModel(scopeID) as T
    }
}
