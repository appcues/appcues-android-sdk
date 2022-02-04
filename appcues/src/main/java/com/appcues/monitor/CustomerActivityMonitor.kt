package com.appcues.monitor

import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.appcues.R
import com.appcues.di.AppcuesKoinComponent
import com.appcues.di.getOwnedViewModel
import com.appcues.domain.entity.Experience
import com.appcues.domain.gateway.CustomerViewGateway
import com.appcues.ui.AppcuesActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ViewModelOwner

internal class CustomerActivityMonitor(
    override val scopeId: String,
) : CustomerViewGateway, ActivityMonitor, AppcuesKoinComponent {

    override suspend fun showExperience(experience: Experience) {
        withContext(Dispatchers.Main) {
            AppcuesActivityMonitor.activity?.let {
                it.startActivity(AppcuesActivity.getIntent(it, scopeId, experience))
                it.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }
    }

    override fun getCustomerViewModel(): CustomerViewModel? {
        return AppcuesActivityMonitor.activity?.let {
            getOwnedViewModel(
                owner = {
                    ViewModelOwner.from(
                        it as ViewModelStoreOwner,
                        it as? SavedStateRegistryOwner
                    )
                }
            )
        }
    }
}
