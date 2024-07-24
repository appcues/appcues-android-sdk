package com.appcues.data.remote.customerapi

import android.os.Build.VERSION
import androidx.core.graphics.Insets
import com.appcues.AppcuesConfig
import com.appcues.BuildConfig
import com.appcues.R
import com.appcues.Screenshot
import com.appcues.data.remote.NetworkRequest
import com.appcues.data.remote.RemoteError
import com.appcues.data.remote.customerapi.request.CaptureMetadataRequest
import com.appcues.data.remote.customerapi.request.CaptureRequest
import com.appcues.data.remote.customerapi.request.InsetsRequest
import com.appcues.debugger.screencapture.SaveCaptureUseCase.ImageUrls
import com.appcues.debugger.screencapture.model.Capture
import com.appcues.util.ContextWrapper
import com.appcues.util.ResultOf

internal class CustomerApiRemoteSource(
    private val service: CustomerApiService,
    private val config: AppcuesConfig,
    private val contextWrapper: ContextWrapper,
) {

    suspend fun getUploadUrls(
        url: String,
        token: String,
        name: String,
    ): ResultOf<ImageUrls, RemoteError> =
        NetworkRequest.execute {
            val path = "/v1/accounts/${config.accountId}/mobile/${config.applicationId}/pre-upload-screenshot"

            val result = service.preUploadScreenshot(
                url = url + path,
                authorization = "Bearer $token",
                name = name
            )

            ImageUrls(
                finalUrl = result.url,
                uploadUrl = result.upload.presignedUrl,
            )
        }

    suspend fun saveCapture(
        url: String,
        token: String,
        capture: Capture,
        imageUrl: String,
    ): ResultOf<Unit, RemoteError> {
        return NetworkRequest.execute {
            val path = "/v1/accounts/${config.accountId}/mobile/${config.applicationId}/screens"

            service.saveCapture(
                customerApiUrl = url + path,
                authorization = "Bearer $token",
                captureRequest = capture.toRequest(imageUrl)
            )
        }
    }

    private fun Capture.toRequest(imageUrl: String): CaptureRequest {
        return CaptureRequest(
            id = id,
            appId = config.applicationId,
            displayName = displayName,
            screenshotImageUrl = imageUrl,
            layout = layout,
            metadata = screenshot.generateCaptureMetadata(),
            timestamp = timestamp
        )
    }

    private fun Screenshot.generateCaptureMetadata(): CaptureMetadataRequest {
        return CaptureMetadataRequest(
            appName = contextWrapper.getAppName(),
            appBuild = contextWrapper.getAppBuild().toString(),
            appVersion = contextWrapper.getAppVersion(),
            deviceModel = contextWrapper.getDeviceName(),
            deviceWidth = size.width,
            deviceHeight = size.height,
            deviceOrientation = contextWrapper.orientation,
            deviceType = contextWrapper.getString(R.string.appcues_device_type),
            bundlePackageId = contextWrapper.packageName,
            sdkVersion = BuildConfig.SDK_VERSION,
            sdkName = "appcues-android",
            osName = "android",
            osVersion = "${VERSION.SDK_INT}",
            insets = insets.toRequest()
        )
    }

    private fun Insets.toRequest(): InsetsRequest {
        return InsetsRequest(
            left = left,
            right = right,
            top = top,
            bottom = bottom,
        )
    }
}
