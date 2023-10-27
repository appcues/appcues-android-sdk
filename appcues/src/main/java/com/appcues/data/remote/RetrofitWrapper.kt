package com.appcues.data.remote

import com.appcues.data.MoshiConfiguration
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.reflect.KClass

internal class RetrofitWrapper(
    private val baseUrl: HttpUrl?,
    private val interceptors: List<Interceptor> = listOf(),
    private val okhttpConfig: (OkHttpClient.Builder) -> OkHttpClient.Builder = { it }
) {

    private val retrofit: Retrofit by lazy {
        val okHttp = OkHttpClient.Builder()
            .also {
                okhttpConfig(it)

                interceptors.forEach { interceptor ->
                    it.addInterceptor(interceptor)
                }
            }.build()

        Retrofit.Builder()
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(MoshiConfiguration.moshi))
            .baseUrl(baseUrl ?: "http://localhost/".toHttpUrl())
            .build()
    }

    fun <T : Any> create(service: KClass<T>): T {
        return retrofit.create(service.java)
    }
}
