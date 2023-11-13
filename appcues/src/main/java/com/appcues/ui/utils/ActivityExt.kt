package com.appcues.ui.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inspector.WindowInspector
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
        // in case of multiple views, get the one that is hosting android.R.id.content
        // we get the last one because sometimes stacking activities might be listed in this method,
        // and we always want the one that is on top
        WindowInspector.getGlobalWindowViews().findTopMost() ?: window.decorView
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
            rootViews.findTopMost() ?: window.decorView
        } catch (ex: Exception) {
            Log.e("Appcues", "error getting decorView, ${ex.message}")
            // if all else fails, use the decorView on the window, which is typically the only one
            window.decorView
        }
    }

    return decorView.rootView as ViewGroup
}

private fun List<View>.findTopMost() = lastOrNull { it.findViewById<View?>(android.R.id.content) != null }
