package com.appcues.data.remote.response.styling

internal data class StyleResponse(
    val horizontalAlignment: String? = null,
    val marginLeading: Int = 0,
    val marginTop: Int = 0,
    val marginTrailing: Int = 0,
    val marginBottom: Int = 0,
    val paddingLeading: Int = 0,
    val paddingTop: Int = 0,
    val paddingBottom: Int = 0,
    val paddingTrailing: Int = 0,
    val cornerRadius: Int = 0,
    val shadow: StyleShadowResponse? = null,
    val backgroundColor: StyleColorResponse? = null,
    val foregroundColor: StyleColorResponse? = null,
    val backgroundGradient: StyleGradientColorResponse? = null,
    val fontName: String? = null,
    val fontSize: Int? = null,
    val textAlignment: String? = null,
    val lineSpacing: Int? = null
)
