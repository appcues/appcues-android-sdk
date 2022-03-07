package com.appcues.data.remote.response.styling

import com.google.gson.Gson

internal data class StyleResponse(
    val width: Int? = null,
    val height: Int? = null,
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
    val foregroundColor: StyleColorResponse? = null,
    val backgroundColor: StyleColorResponse? = null,
    val backgroundGradient: StyleGradientColorResponse? = null,
    val borderColor: StyleColorResponse? = null,
    val borderWidth: Int? = null,
    val fontName: String? = null,
    val fontSize: Int? = null,
    val fontWeight: String? = null,
    val letterSpacing: Int? = null,
    val lineHeight: Int? = null,
    val textAlignment: String? = null,
    val verticalAlignment: String? = null,
    val horizontalAlignment: String? = null,
) {

    companion object {

        /**
         * Try to create a [StyleResponse] from [Any]
         */
        fun fromAny(any: Any?): StyleResponse? {
            return Gson().run {
                fromJson(toJsonTree(any).asJsonObject, StyleResponse::class.java)
            }
        }
    }
}
