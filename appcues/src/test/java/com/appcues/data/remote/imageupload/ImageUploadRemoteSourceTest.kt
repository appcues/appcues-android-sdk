package com.appcues.data.remote.imageupload

import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class ImageUploadRemoteSourceTest {

    @Test
    fun `upload SHOULD return Success`() = runTest {
        // GIVEN
        val service = mockk<ImageUploadService>(relaxed = true)
        val source = ImageUploadRemoteSource(service)
        coEvery { service.upload("fake-url", any()) } returns Unit
        // WHEN
        val result = source.upload("fake-url", mockk(relaxed = true))
        // THEN
        assertThat(result).isInstanceOf(Success::class.java)
    }

    @Test
    fun `upload SHOULD return Failure`() = runTest {
        // GIVEN
        val service = mockk<ImageUploadService>(relaxed = true)
        val source = ImageUploadRemoteSource(service)
        coEvery { service.upload("fake-url", any()) } throws Exception()
        // WHEN
        val result = source.upload("fake-url", mockk(relaxed = true))
        // THEN
        assertThat(result).isInstanceOf(Failure::class.java)
    }
}
