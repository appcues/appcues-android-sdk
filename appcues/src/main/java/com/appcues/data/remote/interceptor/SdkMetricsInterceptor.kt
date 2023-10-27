package com.appcues.data.remote.interceptor

import com.appcues.analytics.SdkMetrics
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response

// an interceptor used on Appcues requests to track the timing of SDK requests
// related to experience rendering, for SDK metrics
internal class SdkMetricsInterceptor : Interceptor {

    override fun intercept(chain: Chain): Response {
        val request = chain.request()
        val requestId = request.header("appcues-request-id")
        val requestBuilder = request
            .newBuilder()
            .method(request.method, request.body)
            .removeHeader("appcues-request-id")
        SdkMetrics.requested(requestId)
        val response = chain.proceed(requestBuilder.build())
        SdkMetrics.responded(requestId)
        return response
    }
}
