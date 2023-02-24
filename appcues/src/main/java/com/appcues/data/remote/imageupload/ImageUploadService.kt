package com.appcues.data.remote.imageupload

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Url

internal interface ImageUploadService {
    @PUT
    suspend fun upload(
        @Url url: String,
        @Body image: RequestBody,
    )
}
