package com.appcues.util

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Test

internal class PackageManagerExtTest {

    private val packageManager = mockk<PackageManager>()

    @After
    fun tearDown() {
        unmockkStatic(
            PackageManager.PackageInfoFlags::class,
            PackageManager.ComponentInfoFlags::class,
            PackageManager.ResolveInfoFlags::class,
        )
    }

    @Test
    fun `getActivityInfoCompat WHEN ComponentInfoFlags of() is missing THEN falls back to the legacy overload`() {
        val componentName = mockk<ComponentName>()
        val expected = mockk<ActivityInfo>()
        mockkStatic(PackageManager.ComponentInfoFlags::class)
        every { PackageManager.ComponentInfoFlags.of(any()) } throws NoSuchMethodError("missing of(long) on this build")
        every { packageManager.getActivityInfo(componentName, any<Int>()) } returns expected

        val result = packageManager.getActivityInfoCompat(componentName, sdkInt = Build.VERSION_CODES.TIRAMISU)

        assertThat(result).isSameInstanceAs(expected)
    }

    @Test
    fun `getActivityInfoCompat WHEN ComponentInfoFlags of() is present THEN uses the API 33 overload`() {
        val componentName = mockk<ComponentName>()
        val expected = mockk<ActivityInfo>()
        val flags = mockk<PackageManager.ComponentInfoFlags>()
        mockkStatic(PackageManager.ComponentInfoFlags::class)
        every { PackageManager.ComponentInfoFlags.of(any()) } returns flags
        every { packageManager.getActivityInfo(componentName, flags) } returns expected

        val result = packageManager.getActivityInfoCompat(componentName, sdkInt = Build.VERSION_CODES.TIRAMISU)

        assertThat(result).isSameInstanceAs(expected)
    }

    @Test
    fun `getActivityInfoCompat WHEN below API 33 THEN uses the legacy overload`() {
        val componentName = mockk<ComponentName>()
        val expected = mockk<ActivityInfo>()
        every { packageManager.getActivityInfo(componentName, any<Int>()) } returns expected

        val result = packageManager.getActivityInfoCompat(componentName, sdkInt = Build.VERSION_CODES.S)

        assertThat(result).isSameInstanceAs(expected)
    }

    @Test
    fun `getPackageInfoCompat WHEN PackageInfoFlags of() is missing THEN falls back to the legacy overload`() {
        val expected = mockk<PackageInfo>()
        mockkStatic(PackageManager.PackageInfoFlags::class)
        every { PackageManager.PackageInfoFlags.of(any()) } throws NoSuchMethodError("missing of(long) on this build")
        every { packageManager.getPackageInfo("com.example", any<Int>()) } returns expected

        val result = packageManager.getPackageInfoCompat("com.example", sdkInt = Build.VERSION_CODES.TIRAMISU)

        assertThat(result).isSameInstanceAs(expected)
    }

    @Test
    fun `resolveActivityCompat WHEN ResolveInfoFlags of() is missing THEN falls back to the legacy overload`() {
        val intent = mockk<Intent>()
        val expected = mockk<ResolveInfo>()
        mockkStatic(PackageManager.ResolveInfoFlags::class)
        every { PackageManager.ResolveInfoFlags.of(any()) } throws NoSuchMethodError("missing of(long) on this build")
        every { packageManager.resolveActivity(intent, any<Int>()) } returns expected

        val result = packageManager.resolveActivityCompat(intent, sdkInt = Build.VERSION_CODES.TIRAMISU)

        assertThat(result).isSameInstanceAs(expected)
    }
}
