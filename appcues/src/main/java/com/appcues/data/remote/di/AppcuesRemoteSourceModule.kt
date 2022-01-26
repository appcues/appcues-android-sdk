package com.appcues.data.remote.di

import com.appcues.AppcuesConfig
import com.appcues.data.remote.AppcuesRemoteSource
import com.appcues.data.remote.retrofit.AppcuesService
import com.appcues.data.remote.retrofit.RetrofitAppcuesRemoteSource
import com.appcues.data.remote.retrofit.RetrofitWrapper
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal object AppcuesRemoteSourceModule {

    private const val BASE_URL = "https://api.appcues.com/"

    fun install(scopeId: String, config: AppcuesConfig): Module = module {
        scope(named(scopeId)) {
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
