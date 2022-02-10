package com.appcues.monitor

internal interface CustomerViewModelHolder {

    fun withViewModel(block: CustomerViewModel.() -> Unit)
}
