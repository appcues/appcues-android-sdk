package com.appcues.data.remote.customerapi

import com.appcues.data.remote.customerapi.response.PreUploadScreenshotResponse
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

internal interface CustomerApiService {
    @POST("v1/accounts/{account}/mobile/{application}/pre-upload-screenshot")
    suspend fun preUploadScreenshot(
        @Path("account") account: String,
        @Path("application") application: String,
        @Query("name") name: String,
        @Header("Authorization") authorization: String,
    ): PreUploadScreenshotResponse

    @POST("v1/accounts/{account}/mobile/{application}/screens")
    suspend fun screen(
        @Path("account") account: String,
        @Path("application") application: String,
        @Header("Authorization") authorization: String,
        @Body screen: RequestBody,
    )
}
