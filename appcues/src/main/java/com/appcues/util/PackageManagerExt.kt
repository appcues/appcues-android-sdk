package com.appcues.util

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build

// extensions to handle deprecations in Android 33 of PackageManager functions
// https://developer.android.com/reference/android/content/pm/PackageManager

internal fun PackageManager.getPackageInfoCompat(packageName: String, flags: Int = 0): PackageInfo =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
    } else {
        @Suppress("DEPRECATION") getPackageInfo(packageName, flags)
    }

internal fun PackageManager.getActivityInfoCompat(componentName: ComponentName, flags: Int = 0): ActivityInfo =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getActivityInfo(componentName, PackageManager.ComponentInfoFlags.of(flags.toLong()))
    } else {
        @Suppress("DEPRECATION") getActivityInfo(componentName, flags)
    }

internal fun PackageManager.resolveActivityCompat(intent: Intent, flags: Int = 0): ResolveInfo? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        resolveActivity(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
    } else {
        @Suppress("DEPRECATION") resolveActivity(intent, flags)
    }
