package com.appcues.data.remote.interceptor

import com.appcues.data.remote.DataLogcues
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response

internal class HttpLogcuesInterceptor(private val dataLogcues: DataLogcues) : Interceptor {

    @Suppress("TooGenericExceptionCaught")
    override fun intercept(chain: Chain): Response {
        val request = chain.request()

        dataLogcues.debug(request)

        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            dataLogcues.error("Failed to send request", e.message.toString())
            throw e
        }

        dataLogcues.debug(response)

        return response
    }
}
