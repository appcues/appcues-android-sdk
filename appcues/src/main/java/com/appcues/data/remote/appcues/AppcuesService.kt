package com.appcues.data.remote.appcues

import com.appcues.data.remote.appcues.response.ActivityResponse
import com.appcues.data.remote.appcues.response.HealthCheckResponse
import com.appcues.data.remote.appcues.response.QualifyResponse
import com.appcues.data.remote.appcues.response.experience.ExperienceResponse
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.UUID

internal interface AppcuesService {
    @POST("v1/accounts/{account}/users/{user}/activity")
    suspend fun activity(
        @Path("account") account: String,
        @Path("user") user: String,
        @Header("Authorization") authorization: String?,
        @Body activity: RequestBody
    ): ActivityResponse

    @POST("v1/accounts/{account}/users/{user}/qualify")
    suspend fun qualify(
        @Path("account") account: String,
        @Path("user") user: String,
        @Header("Authorization") authorization: String?,
        @Header("appcues-request-id") requestId: UUID,
        @Body activity: RequestBody
    ): QualifyResponse

    @GET("v1/accounts/{account}/users/{user}/experience_content/{experienceId}")
    suspend fun experienceContent(
        @Path("account") account: String,
        @Path("user") user: String,
        @Path("experienceId") experienceId: String,
        @Header("Authorization") authorization: String?,
    ): ExperienceResponse

    @GET("v1/accounts/{account}/users/{user}/experience_preview/{experienceId}")
    suspend fun experiencePreview(
        @Path("account") account: String,
        @Path("user") user: String,
        @Path("experienceId") experienceId: String,
        @Header("Authorization") authorization: String?,
    ): ExperienceResponse

    @GET("v1/accounts/{account}/experience_preview/{experienceId}")
    suspend fun experiencePreview(
        @Path("account") account: String,
        @Path("experienceId") experienceId: String,
    ): ExperienceResponse

    @GET("healthz")
    suspend fun healthCheck(): HealthCheckResponse
}
