package com.appcues.data.remote.response.settings

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class SdkSettingsResponse(
    val services: Services
) {
    @JsonClass(generateAdapter = true)
    data class Services(
        val customerApi: String
    )
}
