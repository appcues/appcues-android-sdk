package com.appcues.monitor

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.appcues.di.DependencyProvider
import com.appcues.domain.entity.Experience
import com.appcues.domain.gateway.CustomerViewGateway
import com.appcues.ui.AppcuesActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ViewModelOwner

internal class CustomerActivityMonitor(
    private val dependencyProvider: DependencyProvider
) : CustomerViewGateway, ActivityMonitor, Application.ActivityLifecycleCallbacks {

    init {
        (dependencyProvider.get<Context>() as Application).registerActivityLifecycleCallbacks(this)
    }

    private var customerActivity: Activity? = null

    override suspend fun showExperiences(experience: List<Experience>) {
        withContext(Dispatchers.Main) {
            customerActivity?.let {
                it.startActivity(Intent(it, AppcuesActivity::class.java))
            }
        }
    }

    override fun getCustomerViewModel(): CustomerViewModel? {
        return customerActivity?.run {
            dependencyProvider.getViewModel {
                ViewModelOwner.from(
                    this as ViewModelStoreOwner,
                    this as? SavedStateRegistryOwner
                )
            }
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
        // do nothing
    }
}
