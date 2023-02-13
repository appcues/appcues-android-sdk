package com.appcues.debugger.screencapture

import android.os.Build.VERSION
import android.util.Log
import com.appcues.BuildConfig
import com.appcues.R
import com.appcues.data.MoshiConfiguration
import com.appcues.ui.ElementSelector
import com.appcues.util.ContextResources
import com.squareup.moshi.JsonClass
import java.util.Date
import java.util.UUID

@JsonClass(generateAdapter = true)
internal data class Capture(
    val id: UUID = UUID.randomUUID(),
    val appId: String,
    var displayName: String,
    val screenshotImageUrl: String?,
    val layout: View,
    val metadata: Metadata,
    val timestamp: Date,
) {
    @JsonClass(generateAdapter = true)
    internal data class View(
        val id: UUID = UUID.randomUUID(),
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val type: String,
        val selector: ElementSelector?,
        val children: List<View>?,
    )

    @JsonClass(generateAdapter = true)
    internal data class Metadata(
        val appName: String,
        val appBuild: String,
        val appVersion: String,
        val deviceModel: String,
        val deviceWidth: String,
        val deviceHeight: String,
        val deviceOrientation: String,
        val deviceType: String,
        val bundlePackageId: String,
        val sdkVersion: String,
        val sdkName: String,
        val osName: String,
        val osVersion: String,
    )
}

internal fun ContextResources.generateCaptureMetadata(): Capture.Metadata {
    val width = displayMetrics.widthPixels / displayMetrics.density
    val height = displayMetrics.heightPixels / displayMetrics.density

    return Capture.Metadata(
        appName = getAppName(),
        appBuild = getAppBuild().toString(),
        appVersion = getAppVersion(),
        deviceModel = getDeviceName(),
        deviceWidth = width.toString(),
        deviceHeight = height.toString(),
        deviceOrientation = orientation,
        deviceType = getString(R.string.appcues_device_type),
        bundlePackageId = getPackageName(),
        sdkVersion = BuildConfig.SDK_VERSION,
        sdkName = "appcues-android",
        osName = "android",
        osVersion = "${VERSION.SDK_INT}",
    )
}

// TESTING - will be removed but output layout info to log for now
internal fun Capture.prettyPrint() {
    val json = MoshiConfiguration.moshi.adapter(Capture::class.java).indent("    ").toJson(this)
    Log.i("Appcues", json)
}
