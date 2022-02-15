package com.appcues.monitor

import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.appcues.di.AppcuesKoinComponent
import com.appcues.di.getOwnedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ViewModelOwner

internal class CustomerViewModelAdapter(
    override val scopeId: String,
) : CustomerViewModelHolder, AppcuesKoinComponent {

    override fun withViewModel(block: CustomerViewModel.() -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            AppcuesActivityMonitor.activity?.let {
                getOwnedViewModel<CustomerViewModel>(
                    owner = {
                        ViewModelOwner.from(
                            it as ViewModelStoreOwner,
                            it as? SavedStateRegistryOwner
                        )
                    }
                ).run(block)
            }
        }
    }
}
