package com.appcues.analytics

import com.appcues.data.remote.request.ActivityRequest

internal data class TrackingData(
    val type: AnalyticType,
    val isInternal: Boolean,
    val request: ActivityRequest,
)
