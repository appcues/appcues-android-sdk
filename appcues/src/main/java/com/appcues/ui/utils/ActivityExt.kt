package com.appcues.ui.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.View
import android.view.ViewGroup
import android.view.inspector.WindowInspector
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import java.lang.reflect.Method

@Suppress("UNCHECKED_CAST")
@SuppressLint("PrivateApi")
internal fun Activity.getParentView(): ViewGroup {

    // try to find the most applicable decorView to inject Appcues content into. Typically there is just a single
    // decorView on the Activity window. However, if something like a dialog modal has been shown, this can add another
    // window with another decorView on top of the Activity. If we want to support showing content above that layer, we need
    // to find the top most decorView like below.

    val decorView = if (VERSION.SDK_INT >= VERSION_CODES.Q) {
        // this is the preferred method on API 29+ with the new WindowInspector function
        WindowInspector.getGlobalWindowViews().last()
    } else {
        @Suppress("SwallowedException", "TooGenericExceptionCaught")
        try {
            // this is the less desirable method for API 21-28, using reflection to try to get the root views
            val windowManagerClass = Class.forName("android.view.WindowManagerGlobal")
            val windowManager = windowManagerClass.getMethod("getInstance").invoke(null)
            val getViewRootNames: Method = windowManagerClass.getMethod("getViewRootNames")
            val getRootView: Method = windowManagerClass.getMethod("getRootView", String::class.java)
            val rootViewNames = getViewRootNames.invoke(windowManager) as Array<Any?>
            val rootViews = rootViewNames.map { getRootView(windowManager, it) as View }
            rootViews.last()
        } catch (_: Exception) {
            // if all else fails, use the decorView on the window, which is typically the only one
            window.decorView
        }
    }

    // This is the case of some other decorView showing on top - like a modal
    // dialog. In this case, we need to apply the fix-ups below to ensure that our
    // content can render correctly inside of this other view. In each case, we use
    // the applicable value from the Activity default window.
    if (decorView.findViewTreeLifecycleOwner() == null) {
        val lifecycleOwner = window.decorView.findViewTreeLifecycleOwner()
        decorView.setViewTreeLifecycleOwner(lifecycleOwner)
    }

    if (decorView.findViewTreeViewModelStoreOwner() == null) {
        val viewModelStoreOwner = window.decorView.findViewTreeViewModelStoreOwner()
        decorView.setViewTreeViewModelStoreOwner(viewModelStoreOwner)
    }

    if (decorView.findViewTreeSavedStateRegistryOwner() == null) {
        val savedStateRegistryOwner = window.decorView.findViewTreeSavedStateRegistryOwner()
        decorView.setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
    }

    return decorView.rootView as ViewGroup
}
