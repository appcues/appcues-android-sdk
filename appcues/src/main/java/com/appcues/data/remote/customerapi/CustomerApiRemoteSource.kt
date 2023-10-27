package com.appcues.data.remote.customerapi

import com.appcues.AppcuesConfig
import com.appcues.data.MoshiConfiguration
import com.appcues.data.remote.NetworkRequest
import com.appcues.data.remote.RemoteError
import com.appcues.data.remote.customerapi.response.PreUploadScreenshotResponse
import com.appcues.debugger.screencapture.Capture
import com.appcues.util.ResultOf
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

internal class CustomerApiRemoteSource(
    private val service: CustomerApiService,
    private val config: AppcuesConfig,
) {

    suspend fun preUploadScreenshot(
        capture: Capture,
        token: String,
    ): ResultOf<PreUploadScreenshotResponse, RemoteError> =
        NetworkRequest.execute {
            service.preUploadScreenshot(
                account = config.accountId,
                application = config.applicationId,
                name = "${capture.id}.png",
                authorization = "Bearer $token"
            )
        }

    suspend fun screen(
        capture: Capture,
        token: String,
    ): ResultOf<Unit, RemoteError> {
        val captureJson = MoshiConfiguration.moshi.adapter(Capture::class.java).toJson(capture)
        return NetworkRequest.execute {
            service.screen(
                account = config.accountId,
                application = config.applicationId,
                authorization = "Bearer $token",
                screen = captureJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            )
        }
    }
}
