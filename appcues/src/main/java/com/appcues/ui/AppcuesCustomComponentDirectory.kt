package com.appcues.ui

import com.appcues.AppcuesCustomComponentView
import kotlin.collections.set

internal object AppcuesCustomComponentDirectory {

    private val _customComponents = mutableMapOf<String, AppcuesCustomComponentView>()
    val customComponents: Map<String, AppcuesCustomComponentView> = _customComponents

    fun set(identifier: String, view: AppcuesCustomComponentView) {
        _customComponents[identifier] = view
    }

    fun get(identifier: String): AppcuesCustomComponentView? {
        return _customComponents[identifier]
    }
}
