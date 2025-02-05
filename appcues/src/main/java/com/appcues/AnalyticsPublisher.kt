package com.appcues

import com.appcues.AnalyticType.EVENT
import com.appcues.AnalyticType.GROUP
import com.appcues.AnalyticType.IDENTIFY
import com.appcues.AnalyticType.SCREEN
import com.appcues.analytics.ActivityRequestBuilder
import com.appcues.analytics.TrackingData
import com.appcues.data.remote.appcues.request.EventRequest
import com.appcues.util.DataSanitizer

internal class AnalyticsPublisher(
    private val storage: Storage,
    private val dataSanitizer: DataSanitizer,
) {

    fun publish(listener: AnalyticsListener?, data: TrackingData) = with(dataSanitizer) {
        if (listener == null) return@with

        when (data.type) {
            EVENT -> data.request.events?.forEach {
                listener.trackedAnalytic(EVENT, it.name, it.attributes.sanitize(), data.isInternal)
            }
            IDENTIFY -> listener.trackedAnalytic(IDENTIFY, storage.userId, data.request.profileUpdate?.sanitize(), data.isInternal)
            GROUP -> listener.trackedAnalytic(GROUP, storage.groupId, data.request.groupUpdate?.sanitize(), data.isInternal)
            SCREEN -> data.request.events?.forEach {
                listener.trackedAnalytic(SCREEN, it.screenTitle(), it.attributes.sanitize(), data.isInternal)
            }
        }
    }

    private fun EventRequest.screenTitle(): String? =
        attributes[ActivityRequestBuilder.SCREEN_TITLE_ATTRIBUTE] as? String
}
