package com.appcues.debugger.screencapture

import com.appcues.data.remote.RemoteError
import com.appcues.data.remote.RemoteError.HttpError
import com.appcues.debugger.screencapture.ScreenCaptureSaveException.CaptureStep.GET_IMAGE_PRE_UPLOAD_URL
import com.appcues.debugger.screencapture.ScreenCaptureSaveException.CaptureStep.SAVE_SCREEN_CAPTURE

internal class ScreenCaptureSaveException(val step: CaptureStep, val error: RemoteError) : Exception("ScreenCaptureException: $error") {

    companion object {

        private const val INVALID_TOKEN_CODE = 400
    }

    enum class CaptureStep {
        GET_CUSTOMER_API_URL,
        GET_IMAGE_PRE_UPLOAD_URL,
        UPLOAD_IMAGE,
        SAVE_SCREEN_CAPTURE,
    }

    fun isTokenInvalid(): Boolean {
        return error is HttpError && error.code == INVALID_TOKEN_CODE && (step == GET_IMAGE_PRE_UPLOAD_URL || step == SAVE_SCREEN_CAPTURE)
    }
}
