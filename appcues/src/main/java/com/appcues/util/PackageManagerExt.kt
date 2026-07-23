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
//
// The API 33 `*Flags.of(long)` factory methods are guarded by SDK_INT, but some OEM builds
// report SDK_INT >= 33 while shipping a framework.jar that is missing those methods. Calling
// them there throws NoSuchMethodError (an Error, not an Exception), so each helper falls back
// to the still-functional pre-33 overload instead of crashing the host app.
//
// `sdkInt` is injected (defaulting to the real value) only so the version branch is unit-testable.

@Suppress("SwallowedException")
internal fun PackageManager.getPackageInfoCompat(
    packageName: String,
    flags: Int = 0,
    sdkInt: Int = Build.VERSION.SDK_INT,
): PackageInfo =
    if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
        try {
            getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
        } catch (_: NoSuchMethodError) {
            @Suppress("DEPRECATION") getPackageInfo(packageName, flags)
        }
    } else {
        @Suppress("DEPRECATION") getPackageInfo(packageName, flags)
    }

@Suppress("SwallowedException")
internal fun PackageManager.getActivityInfoCompat(
    componentName: ComponentName,
    flags: Int = 0,
    sdkInt: Int = Build.VERSION.SDK_INT,
): ActivityInfo =
    if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
        try {
            getActivityInfo(componentName, PackageManager.ComponentInfoFlags.of(flags.toLong()))
        } catch (_: NoSuchMethodError) {
            @Suppress("DEPRECATION") getActivityInfo(componentName, flags)
        }
    } else {
        @Suppress("DEPRECATION") getActivityInfo(componentName, flags)
    }

@Suppress("SwallowedException")
internal fun PackageManager.resolveActivityCompat(
    intent: Intent,
    flags: Int = 0,
    sdkInt: Int = Build.VERSION.SDK_INT,
): ResolveInfo? =
    if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
        try {
            resolveActivity(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
        } catch (_: NoSuchMethodError) {
            @Suppress("DEPRECATION") resolveActivity(intent, flags)
        }
    } else {
        @Suppress("DEPRECATION") resolveActivity(intent, flags)
    }
