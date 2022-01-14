package com.appcues.data.remote.response.trait

import com.appcues.data.remote.response.styling.StyleColorResponse
import com.appcues.data.remote.response.styling.StyleResponse

internal data class TraitConfigResponse(
    val presentationStyle: String,
    val skippable: Boolean,
    val backdropColor: StyleColorResponse,
    val style: StyleResponse,
)
