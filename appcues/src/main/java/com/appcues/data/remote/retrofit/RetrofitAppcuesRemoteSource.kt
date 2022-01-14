package com.appcues.data.remote.retrofit

import com.appcues.data.remote.AppcuesRemoteSource
import com.appcues.data.remote.response.TacoResponse

internal class RetrofitAppcuesRemoteSource(private val appcuesService: AppcuesService) : AppcuesRemoteSource {

    override suspend fun getTaco(account: String, user: String): TacoResponse {
        return appcuesService.getTaco(account, user)
    }
}
