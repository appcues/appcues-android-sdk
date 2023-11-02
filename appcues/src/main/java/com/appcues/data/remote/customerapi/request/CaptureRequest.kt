package com.appcues.data.remote.customerapi.request

import com.appcues.ViewElement
import com.squareup.moshi.JsonClass
import java.util.Date
import java.util.UUID

@JsonClass(generateAdapter = true)
internal data class CaptureRequest(
    val id: UUID,
    val appId: String,
    val displayName: String,
    val screenshotImageUrl: String?,
    val layout: ViewElement,
    val metadata: CaptureMetadataRequest,
    val timestamp: Date,
)
