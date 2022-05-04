package com.appcues.data.remote

import com.appcues.AppcuesConfig
import com.appcues.data.remote.retrofit.AppcuesService
import com.appcues.data.remote.retrofit.RetrofitAppcuesRemoteSource
import com.appcues.data.remote.retrofit.RetrofitWrapper
import com.appcues.di.KoinScopePlugin
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.koin.dsl.ScopeDSL

internal object DataRemoteKoin : KoinScopePlugin {

    private const val BASE_URL = "https://api.appcues.net/"

    override fun ScopeDSL.install() {
        scoped<AppcuesRemoteSource> {
            val config: AppcuesConfig = get()
            RetrofitAppcuesRemoteSource(
                appcuesService = getAppcuesService(config.apiBasePath ?: BASE_URL),
                accountId = config.accountId,
                storage = get(),
                sessionMonitor = get(),
            )
        }
    }

    private fun getAppcuesService(apiHostUrl: String): AppcuesService {
        return RetrofitWrapper(apiHostUrl.toHttpUrl()).create(AppcuesService::class)
    }
}
