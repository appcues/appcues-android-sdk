package com.appcues.domain.entity.styling

internal data class ComponentStyle(
    // Margin is outside space
    val marginLeading: Int = 0,
    val marginTop: Int = 0,
    val marginTrailing: Int = 0,
    val marginBottom: Int = 0,
    // Padding is inside space
    val paddingLeading: Int = 0,
    val paddingTop: Int = 0,
    val paddingTrailing: Int = 0,
    val paddingBottom: Int = 0,

    val cornerRadius: Int = 0,
)
