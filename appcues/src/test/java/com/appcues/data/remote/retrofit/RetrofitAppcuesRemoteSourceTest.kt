package com.appcues.data.remote.retrofit

import com.appcues.AppcuesConfig
import com.appcues.SessionMonitor
import com.appcues.Storage
import com.appcues.data.remote.appcues.AppcuesRemoteSource
import com.appcues.data.remote.appcues.AppcuesService
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
internal class RetrofitAppcuesRemoteSourceTest {

    private lateinit var appcuesService: AppcuesService
    private lateinit var sessionMonitor: SessionMonitor
    private lateinit var retrofitAppcuesRemoteSource: AppcuesRemoteSource

    @Before
    fun setUp() {
        val storage: Storage = mockk() {
            every { this@mockk.userId } returns "test-user"
        }
        val config: AppcuesConfig = mockk() {
            every { this@mockk.accountId } returns "123"
        }
        appcuesService = mockk()
        sessionMonitor = mockk()
        retrofitAppcuesRemoteSource = AppcuesRemoteSource(
            service = appcuesService,
            config = config,
            storage = storage,
        )
    }

    @Test
    fun `getExperienceContent SHOULD add Bearer token auth WHEN userSignature is not null`() = runTest {
        // When
        retrofitAppcuesRemoteSource.getExperienceContent("test-experience", "abc", mapOf())

        // Then
        coVerify { appcuesService.experienceContent("123", "test-user", "test-experience", mapOf(), "Bearer abc") }
    }

    @Test
    fun `getExperienceContent SHOULD NOT add Bearer token auth WHEN userSignature is null`() = runTest {
        // When
        retrofitAppcuesRemoteSource.getExperienceContent("test-experience", null, mapOf())

        // Then
        coVerify { appcuesService.experienceContent("123", "test-user", "test-experience", mapOf(), null) }
    }

    @Test
    fun `getExperiencePreview SHOULD add Bearer token auth WHEN userSignature is not null AND userId exists`() = runTest {
        // Given
        // userId set in storage in setUp()

        // When
        retrofitAppcuesRemoteSource.getExperiencePreview("test-experience", "abc", mapOf())

        // Then
        coVerify { appcuesService.experiencePreview("123", "test-user", "test-experience", mapOf(), "Bearer abc") }
    }

    @Test
    fun `getExperiencePreview SHOULD NOT add Bearer token auth WHEN userSignature is null AND userId exists`() = runTest {
        // Given
        // userId set in storage in setUp()

        // When
        retrofitAppcuesRemoteSource.getExperiencePreview("test-experience", null, mapOf())

        // Then
        coVerify { appcuesService.experiencePreview("123", "test-user", "test-experience", mapOf(), null) }
    }

    @Test
    fun `postActivity SHOULD add Bearer token auth WHEN userSignature is not null`() = runTest {
        // When
        retrofitAppcuesRemoteSource.postActivity("test-user", "abc", "")

        // Then
        coVerify { appcuesService.activity("123", "test-user", "Bearer abc", any()) }
    }

    @Test
    fun `postActivity SHOULD NOT add Bearer token auth WHEN userSignature is null`() = runTest {
        // When
        retrofitAppcuesRemoteSource.postActivity("test-user", null, "")

        // Then
        coVerify { appcuesService.activity("123", "test-user", null, any()) }
    }

    @Test
    fun `qualify SHOULD add Bearer token auth WHEN userSignature is not null`() = runTest {
        // When
        retrofitAppcuesRemoteSource.qualify("test-user", "abc", UUID.randomUUID(), "")

        // Then
        coVerify { appcuesService.qualify("123", "test-user", "Bearer abc", any(), any()) }
    }

    @Test
    fun `qualify SHOULD NOT add Bearer token auth WHEN userSignature is null`() = runTest {
        // When
        retrofitAppcuesRemoteSource.qualify("test-user", null, UUID.randomUUID(), "")

        // Then
        coVerify { appcuesService.qualify("123", "test-user", null, any(), any()) }
    }
}
