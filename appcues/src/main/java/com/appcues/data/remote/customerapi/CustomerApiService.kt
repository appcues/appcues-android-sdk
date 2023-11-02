package com.appcues.data.remote.customerapi

import com.appcues.data.remote.customerapi.request.CaptureRequest
import com.appcues.data.remote.customerapi.response.PreUploadScreenshotResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

internal interface CustomerApiService {

    @POST
    suspend fun preUploadScreenshot(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Query("name") name: String,
    ): PreUploadScreenshotResponse

    @POST
    suspend fun saveCapture(
        @Url customerApiUrl: String,
        @Header("Authorization") authorization: String,
        @Body captureRequest: CaptureRequest,
    )
}
