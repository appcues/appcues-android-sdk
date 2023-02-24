package com.appcues.data.remote.imageupload

import com.appcues.data.remote.NetworkRequest
import com.appcues.data.remote.RemoteError
import com.appcues.debugger.screencapture.Capture
import com.appcues.util.ResultOf
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

internal class ImageUploadRemoteSource(
    private val service: ImageUploadService,
) {
    suspend fun upload(
        url: String,
        capture: Capture,
    ): ResultOf<Unit, RemoteError> {

        return NetworkRequest.execute {
            service.upload(
                url = url,
                // still need to figure out how to actually get the PNG content of the capture for the PUT body
                image = capture.displayName.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
            )
        }
    }
}
