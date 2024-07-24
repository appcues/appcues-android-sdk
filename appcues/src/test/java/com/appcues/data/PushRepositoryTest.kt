package com.appcues.data

import com.appcues.data.remote.DataLogcues
import com.appcues.data.remote.RemoteError.HttpError
import com.appcues.data.remote.appcues.AppcuesRemoteSource
import com.appcues.rules.MainDispatcherRule
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class PushRepositoryTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val appcuesRemoteSource: AppcuesRemoteSource = mockk(relaxed = true)
    private val dataLogcues: DataLogcues = mockk(relaxed = true)

    private lateinit var repository: PushRepository

    @Before
    fun setUp() {
        repository = PushRepository(
            appcuesRemoteSource = appcuesRemoteSource,
            dataLogcues = dataLogcues,
        )
    }

    @Test
    fun `preview SHOULD call appcuesRemoteSource`() = runTest {
        // GIVEN
        coEvery { appcuesRemoteSource.previewPush("1234", any()) } returns Success(Unit)
        // WHEN
        val result = repository.preview("1234", mapOf())
        // THEN
        assertThat(result).isInstanceOf(Success::class.java)
    }

    @Test
    fun `preview SHOULD return Error WHEN appcuesRemoteSource fails`() = runTest {
        // GIVEN
        coEvery { appcuesRemoteSource.previewPush("1234", any()) } returns Failure(HttpError())
        // WHEN
        val result = repository.preview("1234", mapOf())
        // THEN
        assertThat(result).isInstanceOf(Failure::class.java)
    }

    @Test
    fun `send SHOULD call appcuesRemoteSource`() = runTest {
        // GIVEN
        coEvery { appcuesRemoteSource.sendPush("1234") } returns Success(Unit)
        // WHEN
        val result = repository.send("1234")
        // THEN
        assertThat(result).isInstanceOf(Success::class.java)
    }

    @Test
    fun `send SHOULD return Error WHEN appcuesRemoteSource fails`() = runTest {
        // GIVEN
        coEvery { appcuesRemoteSource.sendPush("1234") } returns Failure(HttpError())
        // WHEN
        val result = repository.send("1234")
        // THEN
        assertThat(result).isInstanceOf(Failure::class.java)
    }
}
