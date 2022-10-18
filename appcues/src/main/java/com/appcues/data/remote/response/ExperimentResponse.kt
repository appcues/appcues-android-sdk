package com.appcues.data.remote.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class ExperimentResponse(
    val group: String
)
