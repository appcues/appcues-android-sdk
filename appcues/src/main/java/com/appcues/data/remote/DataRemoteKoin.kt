package com.appcues.data.remote

import com.appcues.AppcuesConfig
import com.appcues.data.remote.response.ActivityResponse
import com.appcues.data.remote.response.step.StepContainerResponse
import com.appcues.data.remote.retrofit.AppcuesService
import com.appcues.data.remote.retrofit.RetrofitAppcuesRemoteSource
import com.appcues.data.remote.retrofit.RetrofitWrapper
import com.appcues.data.remote.retrofit.deserializer.ActivityResponseDeserializer
import com.appcues.data.remote.retrofit.deserializer.StepContainerResponseDeserializer
import com.appcues.data.remote.retrofit.serializer.DateSerializer
import com.appcues.di.KoinScopePlugin
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.koin.dsl.ScopeDSL
import java.util.Date

internal object DataRemoteKoin : KoinScopePlugin {

    private const val BASE_URL = "https://api.appcues.com/"

    override fun ScopeDSL.install(config: AppcuesConfig) {
        scoped<Gson> {
            GsonBuilder()
                .registerTypeAdapter(Date::class.java, DateSerializer())
                .registerTypeAdapter(ActivityResponse::class.java, ActivityResponseDeserializer())
                .registerTypeAdapter(StepContainerResponse::class.java, StepContainerResponseDeserializer())
                .create()
        }
        scoped<AppcuesRemoteSource> {
            RetrofitAppcuesRemoteSource(
                appcuesService = getAppcuesService(gson = get(), config.apiHostUrl ?: BASE_URL),
                accountId = config.accountId,
                storage = get(),
            )
        }
    }

    private fun getAppcuesService(gson: Gson, apiHostUrl: String): AppcuesService {
        return RetrofitWrapper(gson, apiHostUrl.toHttpUrl()).create(AppcuesService::class)
    }
}
