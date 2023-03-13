package com.appcues.analytics

import com.appcues.AnalyticType
import com.appcues.data.remote.appcues.request.ActivityRequest

internal data class TrackingData(
    val type: AnalyticType,
    val isInternal: Boolean,
    val request: ActivityRequest,
)
