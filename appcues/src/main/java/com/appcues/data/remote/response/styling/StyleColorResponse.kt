package com.appcues.data.remote.response.styling

import com.google.gson.Gson

internal data class StyleColorResponse(
    val light: String,
    val dark: String? = null,
) {

    companion object {

        /**
         * Try to create a [StyleColorResponse] from [Any]
         */
        fun fromAny(any: Any?): StyleColorResponse? {
            return Gson().run {
                fromJson(toJsonTree(any).asJsonObject, StyleColorResponse::class.java)
            }
        }
    }
}
