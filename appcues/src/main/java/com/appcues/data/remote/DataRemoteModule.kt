package com.appcues.data.remote

import com.appcues.AppcuesConfig
import com.appcues.data.remote.retrofit.AppcuesService
import com.appcues.data.remote.retrofit.RetrofitAppcuesRemoteSource
import com.appcues.data.remote.retrofit.RetrofitWrapper
import com.appcues.di.KoinScopePlugin
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.koin.dsl.ScopeDSL

internal object DataRemoteModule : KoinScopePlugin {

    private const val BASE_URL = "https://api.appcues.com/"

    override fun installIn(koinScope: ScopeDSL, scopeId: String, config: AppcuesConfig) {
        with(koinScope) {
            scoped<AppcuesRemoteSource> {
                RetrofitAppcuesRemoteSource(
                    appcuesService = getAppcuesService(config.apiHostUrl ?: BASE_URL),
                    accountId = config.accountId,
                    session = get(),
                )
            }
        }
    }

    private fun getAppcuesService(apiHostUrl: String): AppcuesService {
        return RetrofitWrapper(apiHostUrl.toHttpUrl()).create(AppcuesService::class)
    }
}
