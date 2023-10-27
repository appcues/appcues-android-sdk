package com.appcues.data.remote.interceptor

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

internal class CustomerApiBaseUrlInterceptor : Interceptor {

    companion object {

        // customer API host is looked up in settings, and must be set here
        // before any usage
        lateinit var baseUrl: HttpUrl
    }

    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request()

        val newUrl = request.url.newBuilder()
            .scheme(baseUrl.scheme)
            .host(baseUrl.host)
            .port(baseUrl.port)
            .build()

        request = request.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(request)
    }
}
