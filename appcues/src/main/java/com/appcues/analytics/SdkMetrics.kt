package com.appcues.analytics

import java.util.Date
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal data class SdkMetrics(
    private var trackedAt: Date? = null,
    private var requestedAt: Date? = null,
    private var respondedAt: Date? = null,
    private var renderStartAt: Date? = null,
) {
    companion object {

        const val METRICS_PROPERTY = "_sdkMetrics"

        private val metrics = ConcurrentHashMap<UUID, SdkMetrics>()

        fun clear() {
            metrics.clear()
        }

        fun tracked(id: UUID, time: Date?) {
            time?.let { metrics.getOrPut(id) { SdkMetrics() }.trackedAt = time }
        }

        fun requested(id: String?, time: Date = Date()) {
            id?.let { metrics[UUID.fromString(it)]?.requestedAt = time }
        }

        fun responded(id: String?, time: Date = Date()) {
            id?.let { metrics[UUID.fromString(it)]?.respondedAt = time }
        }

        fun renderStart(id: UUID?, time: Date = Date()) {
            id?.let { metrics[it]?.renderStartAt = time }
        }

        fun remove(id: UUID) {
            metrics.remove(id)
        }

        fun trackRender(id: UUID?): Map<String, Any> {
            val properties = hashMapOf<String, Any>()
            id?.let {
                val item = metrics[it]
                val trackedAt = item?.trackedAt
                val requestedAt = item?.requestedAt
                val respondedAt = item?.respondedAt
                val renderStartAt = item?.renderStartAt

                @Suppress("ComplexCondition")
                if (trackedAt != null && requestedAt != null && respondedAt != null && renderStartAt != null) {
                    val renderedAt = Date()

                    val timeBeforeRequest = (requestedAt.time - trackedAt.time).toInt()
                    val timeNetwork = (respondedAt.time - requestedAt.time).toInt()
                    val timeProcessingResponse = (renderStartAt.time - respondedAt.time).toInt()
                    val timePresenting = (renderedAt.time - renderStartAt.time).toInt()
                    val timeTotal = (renderedAt.time - trackedAt.time).toInt()

                    remove(it)

                    properties[METRICS_PROPERTY] = mapOf(
                        "timeBeforeRequest" to timeBeforeRequest,
                        "timeNetwork" to timeNetwork,
                        "timeProcessingResponse" to timeProcessingResponse,
                        "timePresenting" to timePresenting,
                        "timeTotal" to timeTotal,
                    )
                }
            }

            return properties
        }
    }
}
