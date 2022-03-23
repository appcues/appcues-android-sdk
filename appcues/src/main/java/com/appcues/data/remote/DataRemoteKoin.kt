package com.appcues.data.remote

import com.appcues.AppcuesConfig
import com.appcues.data.GsonConfiguration
import com.appcues.data.remote.retrofit.AppcuesService
import com.appcues.data.remote.retrofit.RetrofitAppcuesRemoteSource
import com.appcues.data.remote.retrofit.RetrofitWrapper
import com.appcues.di.KoinScopePlugin
import com.google.gson.Gson
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.koin.dsl.ScopeDSL

internal object DataRemoteKoin : KoinScopePlugin {

    private const val BASE_URL = "https://api.appcues.com/"

    override fun ScopeDSL.install(config: AppcuesConfig) {
        scoped { GsonConfiguration.getGson() }
        scoped<AppcuesRemoteSource> {
            RetrofitAppcuesRemoteSource(
                appcuesService = getAppcuesService(gson = get(), config.apiHostUrl ?: BASE_URL),
                accountId = config.accountId,
                storage = get(),
                gson = get()
            )
        }
    }

    private fun getAppcuesService(gson: Gson, apiHostUrl: String): AppcuesService {
        return RetrofitWrapper(gson, apiHostUrl.toHttpUrl()).create(AppcuesService::class)
    }
}
