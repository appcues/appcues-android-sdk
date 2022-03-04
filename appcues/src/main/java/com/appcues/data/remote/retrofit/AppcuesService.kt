package com.appcues.data.remote.retrofit

import com.appcues.data.remote.request.ActivityRequest
import com.appcues.data.remote.response.ActivityResponse
import com.appcues.data.remote.response.experience.ExperienceResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

internal interface AppcuesService {

    @POST("v1/accounts/{account}/users/{user}/activity")
    suspend fun activity(
        @Path("account") account: String,
        @Path("user") user: String,
        @Query("sync") sync: Int?,
        @Body activity: ActivityRequest
    ): ActivityResponse

    @GET("v1/accounts/{account}/users/{user}/experience_content/{content}")
    suspend fun content(
        @Path("account") account: String,
        @Path("user") user: String,
        @Path("content") contentId: String,
    ): ExperienceResponse
}
