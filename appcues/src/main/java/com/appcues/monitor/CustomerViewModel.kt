package com.appcues.monitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcues.domain.ShowUseCase
import com.appcues.logging.Logcues
import kotlinx.coroutines.launch

internal class CustomerViewModel(
    private val logcues: Logcues,
    private val showUseCase: ShowUseCase,
) : ViewModel() {

    fun show(contentId: String) {
        logcues.i("show(contentId: $contentId)")
        viewModelScope.launch {
            showUseCase(contentId = contentId)
        }
    }
}
