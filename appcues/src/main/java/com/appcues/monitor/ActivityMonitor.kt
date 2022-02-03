package com.appcues.monitor

import android.app.Activity

internal interface ActivityMonitor {
    var customerActivity: Activity?
    fun getCustomerViewModel(): CustomerViewModel?
}
