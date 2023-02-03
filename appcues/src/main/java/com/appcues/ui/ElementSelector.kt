package com.appcues.ui

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class ElementSelector(
    val accessibilityIdentifier: String? = null,
    val description: String? = null,
    val tag: String? = null,
    val id: String? = null,
) {
    val isValid: Boolean
        get() = accessibilityIdentifier != null || description != null || tag != null || id != null
}
