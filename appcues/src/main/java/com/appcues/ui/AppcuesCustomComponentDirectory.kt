package com.appcues.ui

import com.appcues.AppcuesCustomComponentView
import kotlin.collections.set

internal object AppcuesCustomComponentDirectory {

    private var views = mutableMapOf<String, AppcuesCustomComponentView>()

    fun set(identifier: String, view: AppcuesCustomComponentView) {
        views[identifier] = view
    }

    fun get(identifier: String): AppcuesCustomComponentView? {
        return views[identifier]
    }
}
