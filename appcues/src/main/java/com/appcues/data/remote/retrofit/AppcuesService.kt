package com.appcues.data.remote.retrofit

import com.appcues.data.remote.response.ActivityResponse
import com.appcues.data.remote.response.HealthCheckResponse
import com.appcues.data.remote.response.QualifyResponse
import com.appcues.data.remote.response.experience.ExperienceResponse
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

internal interface AppcuesService {

    @POST("v1/accounts/{account}/users/{user}/activity")
    suspend fun activity(
        @Path("account") account: String,
        @Path("user") user: String,
        @Body activity: RequestBody
    ): ActivityResponse

    @POST("v1/accounts/{account}/users/{user}/qualify")
    suspend fun qualify(
        @Path("account") account: String,
        @Path("user") user: String,
        @Body activity: RequestBody
    ): QualifyResponse

    @GET("v1/accounts/{account}/users/{user}/experience_content/{experienceId}")
    suspend fun experienceContent(
        @Path("account") account: String,
        @Path("user") user: String,
        @Path("experienceId") experienceId: String,
    ): ExperienceResponse

    @GET("v1/accounts/{account}/users/{user}/experience_preview/{experienceId}")
    suspend fun experiencePreview(
        @Path("account") account: String,
        @Path("user") user: String,
        @Path("experienceId") experienceId: String,
    ): ExperienceResponse

    @GET("v1/accounts/{account}/experience_preview/{experienceId}")
    suspend fun experiencePreview(
        @Path("account") account: String,
        @Path("experienceId") experienceId: String,
    ): ExperienceResponse

    @GET("healthz")
    suspend fun healthCheck(): HealthCheckResponse
}
