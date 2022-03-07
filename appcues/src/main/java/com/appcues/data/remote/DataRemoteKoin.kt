package com.appcues.data.remote

import com.appcues.AppcuesConfig
import com.appcues.data.remote.retrofit.AppcuesService
import com.appcues.data.remote.retrofit.RetrofitAppcuesRemoteSource
import com.appcues.data.remote.retrofit.RetrofitWrapper
import com.appcues.di.KoinScopePlugin
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.koin.dsl.ScopeDSL

internal object DataRemoteKoin : KoinScopePlugin {

    private const val BASE_URL = "https://api.appcues.com/"

    override fun ScopeDSL.install(config: AppcuesConfig) {
        scoped<AppcuesRemoteSource> {
            RetrofitAppcuesRemoteSource(
                appcuesService = getAppcuesService(config.apiHostUrl ?: BASE_URL),
                accountId = config.accountId,
                storage = get(),
            )
        }
    }

    private fun getAppcuesService(apiHostUrl: String): AppcuesService {
        return RetrofitWrapper(apiHostUrl.toHttpUrl()).create(AppcuesService::class)
    }
}
