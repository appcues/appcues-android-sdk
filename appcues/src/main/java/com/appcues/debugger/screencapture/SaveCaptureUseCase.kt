package com.appcues.debugger.screencapture

import android.graphics.Bitmap
import com.appcues.data.remote.customerapi.CustomerApiRemoteSource
import com.appcues.data.remote.imageupload.ImageUploadRemoteSource
import com.appcues.data.remote.sdksettings.SdkSettingsRemoteSource
import com.appcues.debugger.screencapture.ScreenCaptureSaveException.CaptureStep.GET_CUSTOMER_API_URL
import com.appcues.debugger.screencapture.ScreenCaptureSaveException.CaptureStep.GET_IMAGE_PRE_UPLOAD_URL
import com.appcues.debugger.screencapture.ScreenCaptureSaveException.CaptureStep.SAVE_SCREEN_CAPTURE
import com.appcues.debugger.screencapture.ScreenCaptureSaveException.CaptureStep.UPLOAD_IMAGE
import com.appcues.debugger.screencapture.model.Capture
import com.appcues.util.ResultOf
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success

internal class SaveCaptureUseCase(
    private val sdkSettings: SdkSettingsRemoteSource,
    private val customer: CustomerApiRemoteSource,
    private val imageUpload: ImageUploadRemoteSource,
) {

    suspend operator fun invoke(token: String, capture: Capture): ResultOf<Unit, ScreenCaptureSaveException> {
        return try {
            // step 1 - get customer API url
            val customerApiUrl = getCustomerApiUrl()
            // step 2 - use the customer API path to get the link to upload the screenshot
            val imageUrls = getImageUrls(customerApiUrl, token, capture)
            // step 3 - upload the screenshot
            uploadScreenshot(imageUrls.uploadUrl, capture.bitmapToUpload)
            // step 4 - update the screenshot link and save the screen
            saveScreen(customerApiUrl, token, capture, imageUrls.finalUrl)
        } catch (e: ScreenCaptureSaveException) {
            Failure(e)
        }
    }

    // step 1 - get the settings, with the path to customer API
    private suspend fun getCustomerApiUrl(): String {
        return sdkSettings.getCustomerApiUrl().let { result ->
            when (result) {
                is Failure -> throw ScreenCaptureSaveException(GET_CUSTOMER_API_URL, result.reason)
                is Success -> result.value
            }
        }
    }

    data class ImageUrls(
        val finalUrl: String,
        val uploadUrl: String,
    )

    // step 2 - use the customer API path to get the link to upload the screenshot
    private suspend fun getImageUrls(url: String, token: String, capture: Capture): ImageUrls {
        val name = "${capture.id}.png"

        return customer.getUploadUrls(url, token, name).let { result ->
            when (result) {
                is Failure -> throw ScreenCaptureSaveException(GET_IMAGE_PRE_UPLOAD_URL, result.reason)
                is Success -> result.value
            }
        }
    }

    // step 3 - upload the screenshot
    private suspend fun uploadScreenshot(uploadUrl: String, bitmap: Bitmap) {
        return imageUpload.upload(uploadUrl, bitmap).let { result ->
            when (result) {
                is Failure -> throw ScreenCaptureSaveException(UPLOAD_IMAGE, result.reason)
                is Success -> Unit
            }
        }
    }

    // step 4 - update the screenshot link and save the screen
    private suspend fun saveScreen(customerApiUrl: String, token: String, capture: Capture, imageUrl: String): Success<Unit> {
        return customer.saveCapture(customerApiUrl, token, capture, imageUrl).let { result ->
            when (result) {
                is Failure -> throw ScreenCaptureSaveException(SAVE_SCREEN_CAPTURE, result.reason)
                is Success -> result
            }
        }
    }
}
