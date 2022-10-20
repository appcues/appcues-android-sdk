package com.appcues.data.remote.retrofit

import com.appcues.BuildConfig
import com.appcues.analytics.SdkMetrics
import com.appcues.data.MoshiConfiguration
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.reflect.KClass

internal class RetrofitWrapper(
    private val url: HttpUrl,
    private val isDebug: Boolean = BuildConfig.DEBUG
) {
    companion object {
        // we should not show an experience response if it takes > 5 seconds to return
        // as it could be out of date with the content the user is now viewing
        const val READ_TIMEOUT_SECONDS: Long = 5
    }

    fun <T : Any> create(service: KClass<T>): T {
        return getRetrofit().create(service.java)
    }

    private fun getRetrofit(): Retrofit {

        val okHttp = OkHttpClient.Builder().also {
            if (isDebug) {
                it.addInterceptor(getHttpLoggingInterceptor())
            }
        }
            .addInterceptor(MetricsInterceptor())
            .readTimeout(READ_TIMEOUT_SECONDS, SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(MoshiConfiguration.moshi))
            .build()
    }

    private fun getHttpLoggingInterceptor(): Interceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
}

internal class MetricsInterceptor : Interceptor {

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
