package com.appcues.debugger.screencapture

import android.graphics.Bitmap
import android.os.Build.VERSION
import com.appcues.BuildConfig
import com.appcues.R
import com.appcues.Screenshot
import com.appcues.ViewElement
import com.appcues.util.ContextWrapper
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date
import java.util.UUID

@JsonClass(generateAdapter = true)
internal data class Capture(
    val id: UUID = UUID.randomUUID(),
    val appId: String,
    var displayName: String,
    val screenshotImageUrl: String?,
    val layout: ViewElement,
    val metadata: Metadata,
    val timestamp: Date,
) {

    val targetableElementCount: Int = getTargetableElementCount(layout)

    private fun getTargetableElementCount(element: ViewElement): Int {
        val selectableElement = if (element.selector != null) 1 else 0

        return selectableElement + (element.children?.sumOf { getTargetableElementCount(it) } ?: 0)
    }

    // this is a lateinit var instead of a constructor arg so we can ignore it in JSON
    // serialization and a default value is not needed for the Moshi generated adapter
    @Json(ignore = true)
    lateinit var screenshot: Bitmap

    @JsonClass(generateAdapter = true)
    internal data class Metadata(
        val appName: String,
        val appBuild: String,
        val appVersion: String,
        val deviceModel: String,
        val deviceWidth: Int,
        val deviceHeight: Int,
        val deviceOrientation: String,
        val deviceType: String,
        val bundlePackageId: String,
        val sdkVersion: String,
        val sdkName: String,
        val osName: String,
        val osVersion: String,
        val insets: Insets,
    )

    @JsonClass(generateAdapter = true)
    internal data class Insets(
        val left: Int,
        val right: Int,
        val top: Int,
        val bottom: Int,
    )
}

internal fun ContextWrapper.generateCaptureMetadata(screenshot: Screenshot): Capture.Metadata {
    return Capture.Metadata(
        appName = getAppName(),
        appBuild = getAppBuild().toString(),
        appVersion = getAppVersion(),
        deviceModel = getDeviceName(),
        deviceWidth = screenshot.size.width,
        deviceHeight = screenshot.size.height,
        deviceOrientation = orientation,
        deviceType = getString(R.string.appcues_device_type),
        bundlePackageId = getPackageName(),
        sdkVersion = BuildConfig.SDK_VERSION,
        sdkName = "appcues-android",
        osName = "android",
        osVersion = "${VERSION.SDK_INT}",
        insets = Capture.Insets(
            left = screenshot.insets.left,
            right = screenshot.insets.right,
            top = screenshot.insets.top,
            bottom = screenshot.insets.bottom,
        )
    )
}

internal fun Int.toDp(density: Float) =
    (this / density).toInt()
