package com.appcues.data.remote.customerapi.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class PreUploadScreenshotResponse(
    val upload: Upload,
    val url: String,
) {
    @JsonClass(generateAdapter = true)
    data class Upload(
        val presignedUrl: String
    )
}
