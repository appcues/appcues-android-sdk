package com.appcues.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.koin.core.scope.Scope

internal class AppcuesViewModelFactory(
    private val scope: Scope
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AppcuesViewModel(scope) as T
    }
}
