package com.appcues.ui.utils

import android.app.Activity
import android.view.ViewGroup

internal fun Activity.getRootView(): ViewGroup {
    return window.decorView.rootView as ViewGroup
}
