package com.appcues.data.remote.appcues.response.styling

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class StyleResponse(
    val width: Double? = null,
    val height: Double? = null,
    val marginLeading: Double = 0.0,
    val marginTop: Double = 0.0,
    val marginTrailing: Double = 0.0,
    val marginBottom: Double = 0.0,
    val paddingLeading: Double = 0.0,
    val paddingTop: Double = 0.0,
    val paddingBottom: Double = 0.0,
    val paddingTrailing: Double = 0.0,
    val cornerRadius: Double = 0.0,
    val shadow: StyleShadowResponse? = null,
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
