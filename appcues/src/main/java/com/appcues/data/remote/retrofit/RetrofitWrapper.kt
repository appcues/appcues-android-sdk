package com.appcues.data.remote.retrofit

import com.appcues.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.reflect.KClass

internal class RetrofitWrapper(private val url: HttpUrl, private val isDebug: Boolean = BuildConfig.DEBUG) {

    fun <T : Any> create(service: KClass<T>): T {
        return getRetrofit().create(service.java)
    }

    private fun getRetrofit(): Retrofit {
        val gson = GsonBuilder().create()

        val okHttp = OkHttpClient.Builder().also {
            if (isDebug) {
                it.addInterceptor(getHttpLoggingInterceptor())
            }
        }.build()

        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun getHttpLoggingInterceptor(): Interceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
}
