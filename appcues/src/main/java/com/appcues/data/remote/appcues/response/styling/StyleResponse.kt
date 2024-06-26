package com.appcues.data.remote.appcues.response.styling

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class StyleResponse(
    val width: Double? = null,
    val height: Double? = null,
    val marginLeading: Double? = null,
    val marginTop: Double? = null,
    val marginTrailing: Double? = null,
    val marginBottom: Double? = null,
    val paddingLeading: Double? = null,
    val paddingTop: Double? = null,
    val paddingBottom: Double? = null,
    val paddingTrailing: Double? = null,
    val cornerRadius: Double? = null,
    val shadow: StyleShadowResponse? = null,
    val colors: List<String>? = null,
    val foregroundColor: StyleColorResponse? = null,
    val backgroundColor: StyleColorResponse? = null,
    val backgroundGradient: StyleGradientColorResponse? = null,
    val borderColor: StyleColorResponse? = null,
    val borderWidth: Double? = null,
    val fontName: String? = null,
    val fontSize: Double? = null,
    val letterSpacing: Double? = null,
    val lineHeight: Double? = null,
    val textAlignment: String? = null,
    val verticalAlignment: String? = null,
    val horizontalAlignment: String? = null,
    val backgroundImage: StyleBackgroundImageResponse? = null,
)
