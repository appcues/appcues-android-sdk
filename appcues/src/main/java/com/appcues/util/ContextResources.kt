package com.appcues.util

import android.content.Context
import androidx.annotation.StringRes

// class used to access resources from classes that are not
// supposed to be tied to android context, this is helpful to keep
// any class clean of Android references, which makes it easier to unit test later
class ContextResources(private val context: Context) {

    fun getString(@StringRes id: Int): String {
        return context.getString(id)
    }

    fun getString(@StringRes id: Int, vararg args: Any): String {
        return context.getString(id, *args)
    }
}
