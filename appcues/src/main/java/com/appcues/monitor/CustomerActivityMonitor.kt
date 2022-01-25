package com.appcues.monitor

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.appcues.R
import com.appcues.di.AppcuesKoinComponent
import com.appcues.di.getApplicationContext
import com.appcues.di.getOwnedViewModel
import com.appcues.domain.entity.Experience
import com.appcues.domain.gateway.CustomerViewGateway
import com.appcues.ui.AppcuesActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ViewModelOwner

internal class CustomerActivityMonitor(
    override val scopeId: String,
) : CustomerViewGateway, ActivityMonitor, Application.ActivityLifecycleCallbacks, AppcuesKoinComponent {

    init {
        getApplicationContext().registerActivityLifecycleCallbacks(this)
    }

    private var customerActivity: Activity? = null

    override suspend fun showExperience(experience: Experience) {
        withContext(Dispatchers.Main) {
            customerActivity?.let {
                it.startActivity(AppcuesActivity.getIntent(it, scopeId, experience))
                it.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }
    }

    override fun getCustomerViewModel(): CustomerViewModel? {
        return customerActivity?.let {
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

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity !is AppcuesActivity) {
            this.customerActivity = activity
        }
    }

    override fun onActivityStarted(activity: Activity) {
        // do nothing
    }

    override fun onActivityResumed(activity: Activity) {
        // do nothing
    }

    override fun onActivityPaused(activity: Activity) {
        // do nothing
    }

    override fun onActivityStopped(activity: Activity) {
        // do nothing
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // do nothing
    }

    override fun onActivityDestroyed(activity: Activity) {
        activity.sendLocalBroadcast(Intent(activity.intentActionFinish()))
    }
}
