package com.appcues.data.remote.customerapi.request

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class CaptureMetadataRequest(
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
    val insets: InsetsRequest,
)
