package com.appcues.data.remote.retrofit

import com.appcues.data.remote.response.TacoResponse
import retrofit2.http.GET
import retrofit2.http.Path

internal interface AppcuesService {

    @GET("v1/accounts/{account}/users/{user}/activity")
    suspend fun getTaco(
        @Path("account") account: String,
        @Path("user") user: String
    ): TacoResponse
}
