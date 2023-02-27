package com.appcues.data.remote.imageupload

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.PNG
import com.appcues.data.remote.NetworkRequest
import com.appcues.data.remote.RemoteError
import com.appcues.util.ResultOf
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

internal class ImageUploadRemoteSource(
    private val service: ImageUploadService,
) {
    companion object {
        private const val IMAGE_QUALITY = 100
    }

    suspend fun upload(
        url: String,
        bitmap: Bitmap,
    ): ResultOf<Unit, RemoteError> {
        return NetworkRequest.execute {
            service.upload(
                url = url,
                image = bitmap.toData().toRequestBody("image/png".toMediaTypeOrNull()),
            )
        }
    }

    private fun Bitmap.toData(): ByteArray {
        val stream = ByteArrayOutputStream()
        this.compress(PNG, IMAGE_QUALITY, stream)
        return stream.toByteArray()
    }
}
