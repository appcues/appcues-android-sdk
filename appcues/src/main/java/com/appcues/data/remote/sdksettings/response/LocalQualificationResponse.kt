package com.appcues.data.remote.sdksettings.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class LocalQualificationResponse(
    val qualifications: List<QualificationsResponse>
)
