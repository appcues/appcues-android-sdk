package com.appcues.data.remote

import com.appcues.data.remote.response.TacoResponse

internal interface AppcuesRemoteSource {

    suspend fun getTaco(account: String, user: String): TacoResponse
}
