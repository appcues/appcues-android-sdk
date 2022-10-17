package com.appcues.data

import com.appcues.AppcuesConfig
import com.appcues.data.local.AppcuesLocalSource
import com.appcues.data.local.model.ActivityStorage
import com.appcues.data.mapper.experience.ExperienceMapper
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperiencePriority.LOW
import com.appcues.data.model.ExperiencePriority.NORMAL
import com.appcues.data.remote.AppcuesRemoteSource
import com.appcues.data.remote.RemoteError.HttpError
import com.appcues.data.remote.RemoteError.NetworkError
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.data.remote.response.QualifyResponse
import com.appcues.data.remote.response.experience.ExperienceResponse
import com.appcues.logging.Logcues
import com.appcues.rules.MainDispatcherRule
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class AppcuesRepositoryTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val appcuesRemoteSource: AppcuesRemoteSource = mockk(relaxed = true)
    private val appcuesLocalSource: AppcuesLocalSource = mockk(relaxed = true)
    private val experienceMapper: ExperienceMapper = mockk()
    private val logcues: Logcues = mockk(relaxed = true)

    private lateinit var config: AppcuesConfig
    private lateinit var repository: AppcuesRepository

    @Before
    fun setUp() {
        config = AppcuesConfig("123", "abc")
        repository = AppcuesRepository(
            appcuesRemoteSource = appcuesRemoteSource,
            appcuesLocalSource = appcuesLocalSource,
            experienceMapper = experienceMapper,
            config = config,
            logcues = logcues,
        )
    }

    @Test
    fun `getExperienceContent SHOULD get from appcuesRemoteSource AND map from experienceMapper`() = runTest {
        // GIVEN
        val experienceResponse = mockk<ExperienceResponse>()
        coEvery { appcuesRemoteSource.getExperienceContent("1234") } returns Success(experienceResponse)
        val mappedExperience = mockk<Experience>()
        coEvery { experienceMapper.map(experienceResponse) } returns mappedExperience
        // WHEN
        val result = repository.getExperienceContent("1234")
        // THEN
        assertThat(result).isEqualTo(mappedExperience)
    }

    @Test
    fun `getExperienceContent SHOULD return null WHEN appcuesRemoteSource fails`() = runTest {
        // GIVEN
        coEvery { appcuesRemoteSource.getExperienceContent("1234") } returns Failure(HttpError())
        // WHEN
        val result = repository.getExperienceContent("1234")
        // THEN
        assertThat(result).isNull()
    }

    @Test
    fun `getExperiencePreview SHOULD get from appcuesRemoteSource AND map from experienceMapper`() = runTest {
        // GIVEN
        val experienceResponse = mockk<ExperienceResponse>()
        coEvery { appcuesRemoteSource.getExperiencePreview("1234") } returns Success(experienceResponse)
        val mappedExperience = mockk<Experience>()
        coEvery { experienceMapper.map(experienceResponse) } returns mappedExperience
        // WHEN
        val result = repository.getExperiencePreview("1234")
        // THEN
        assertThat(result).isEqualTo(mappedExperience)
    }

    @Test
    fun `getExperiencePreview SHOULD return null WHEN appcuesRemoteSource fails`() = runTest {
        // GIVEN
        coEvery { appcuesRemoteSource.getExperiencePreview("1234") } returns Failure(HttpError())
        // WHEN
        val result = repository.getExperiencePreview("1234")
        // THEN
        assertThat(result).isNull()
    }

    @Test
    fun `trackActivity SHOULD save to local storage AND send qualify request`() = runTest {
        // GIVEN
        val request = ActivityRequest(accountId = "123", userId = "userId")
        val qualifyResponse = QualifyResponse(
            experiences = listOf(mockk(), mockk()),
            performedQualification = true,
            qualificationReason = "screen_view",
            experiments = emptyMap(),
        )
        coEvery { appcuesRemoteSource.qualify(any(), any()) } returns Success(qualifyResponse)
        val mappedExperience = mockk<Experience>()
        coEvery { experienceMapper.map(any(), any()) } returns mappedExperience

        // WHEN
        val result = repository.trackActivity(request)

        // THEN
        coVerify { appcuesLocalSource.saveActivity(any()) }
        coVerify { appcuesRemoteSource.qualify("userId", any()) }
        assertThat(result.count()).isEqualTo(2)
        assertThat(result.first()).isEqualTo(mappedExperience)
        coVerify { appcuesLocalSource.removeActivity(any()) }
    }

    @Test
    fun `trackActivity SHOULD call trackActivity on cache item WHEN the next request is sent`() = runTest {
        // GIVEN
        val request = ActivityRequest(accountId = "123", userId = "userId")
        val activityStorage = ActivityStorage(UUID.randomUUID(), "123", "userId", "data")
        coEvery { appcuesLocalSource.getAllActivity() } returns listOf(activityStorage)

        // WHEN
        repository.trackActivity(request)

        // THEN
        coVerify { appcuesRemoteSource.postActivity("userId", "data") }
    }

    @Test
    fun `trackActivity SHOULD retain cache item WHEN the qualify request fails with a NetworkError`() = runTest {
        // GIVEN
        val request = ActivityRequest(accountId = "123", userId = "userId")
        coEvery { appcuesRemoteSource.qualify(any(), any()) } returns Failure(NetworkError())

        // WHEN
        repository.trackActivity(request)

        // THEN
        coVerify(exactly = 1) { appcuesLocalSource.saveActivity(any()) }
        coVerify(exactly = 0) { appcuesLocalSource.removeActivity(any()) }
    }

    @Test
    fun `trackActivity SHOULD NOT retain cache item WHEN the qualify request fails with an HTTPError`() = runTest {
        // GIVEN
        val request = ActivityRequest(accountId = "123", userId = "userId")
        coEvery { appcuesRemoteSource.qualify(any(), any()) } returns Failure(HttpError())

        // WHEN
        repository.trackActivity(request)

        // THEN
        coVerify(exactly = 1) { appcuesLocalSource.saveActivity(any()) }
        coVerify(exactly = 1) { appcuesLocalSource.removeActivity(any()) }
    }

    @Test
    fun `trackActivity SHOULD retain a cache item WHEN the retry fails with a NetworkError`() = runTest {
        // GIVEN
        val request = ActivityRequest(accountId = "123", userId = "userId")
        val activityStorage = ActivityStorage(UUID.randomUUID(), "123", "userId", "data")
        coEvery { appcuesLocalSource.getAllActivity() } returns listOf(activityStorage)
        coEvery { appcuesRemoteSource.postActivity("userId", "data") } returns Failure(NetworkError())

        // WHEN
        repository.trackActivity(request)

        // THEN
        coVerify(exactly = 0) { appcuesLocalSource.removeActivity(activityStorage) }
    }

    @Test
    fun `trackActivity SHOULD NOT retain a cache item WHEN the retry fails with an HTTPError`() = runTest {
        // GIVEN
        val request = ActivityRequest(accountId = "123", userId = "userId")
        val activityStorage = ActivityStorage(UUID.randomUUID(), "123", "userId", "data")
        coEvery { appcuesLocalSource.getAllActivity() } returns listOf(activityStorage)
        coEvery { appcuesRemoteSource.postActivity("userId", "data") } returns Failure(HttpError())

        // WHEN
        repository.trackActivity(request)

        // THEN
        coVerify(exactly = 1) { appcuesLocalSource.removeActivity(activityStorage) }
    }

    @Test
    fun `trackActivity SHOULD remove cache item WHEN the total storage size is at capacity`() = runTest {
        // GIVEN
        val request: ActivityRequest = mockk(relaxed = true)
        val activityStorage1 = ActivityStorage(UUID.randomUUID(), "123", "userId", "data1")
        val activityStorage2 = ActivityStorage(UUID.randomUUID(), "123", "userId", "data2")
        val activityStorage3 = ActivityStorage(UUID.randomUUID(), "123", "userId", "data3")
        coEvery { appcuesLocalSource.getAllActivity() } returns listOf(activityStorage1, activityStorage2, activityStorage3)
        config.activityStorageMaxSize = 2

        // WHEN
        repository.trackActivity(request)

        // THEN
        coVerify(exactly = 0) { appcuesRemoteSource.postActivity("userId", "data1") } // expired item
        coVerify(exactly = 1) { appcuesRemoteSource.postActivity("userId", "data2") }
        coVerify(exactly = 1) { appcuesRemoteSource.postActivity("userId", "data3") }
        coVerify(exactly = 4) { appcuesLocalSource.removeActivity(any()) } // all 3 in cache, plus current item removed
    }

    @Test
    fun `trackActivity SHOULD remove cache item WHEN it is older than the max age requirement`() = runTest {
        // GIVEN
        val request: ActivityRequest = mockk(relaxed = true)
        val fiveSecAgo = Date().apply { time -= 5000 }
        val activityStorage1 = ActivityStorage(UUID.randomUUID(), "123", "userId", "data1", created = fiveSecAgo)
        val activityStorage2 = ActivityStorage(UUID.randomUUID(), "123", "userId", "data2")
        val activityStorage3 = ActivityStorage(UUID.randomUUID(), "123", "userId", "data3")
        coEvery { appcuesLocalSource.getAllActivity() } returns listOf(activityStorage1, activityStorage2, activityStorage3)
        config.activityStorageMaxAge = 3

        // WHEN
        repository.trackActivity(request)

        // THEN
        coVerify(exactly = 0) { appcuesRemoteSource.postActivity("userId", "data1") } // expired item
        coVerify(exactly = 1) { appcuesRemoteSource.postActivity("userId", "data2") }
        coVerify(exactly = 1) { appcuesRemoteSource.postActivity("userId", "data3") }
        coVerify(exactly = 4) { appcuesLocalSource.removeActivity(any()) } // all 3 in cache, plus current item removed
    }

    @Test
    fun `trackActivity SHOULD mark the experience LOW priority WHEN the qualification reason is screen_view`() = runTest {
        // GIVEN
        val request = ActivityRequest(accountId = "123", userId = "userId")
        val qualifyResponse = QualifyResponse(
            experiences = listOf(mockk(), mockk()),
            performedQualification = true,
            qualificationReason = "screen_view",
            experiments = emptyMap(),
        )
        coEvery { appcuesRemoteSource.qualify(any(), any()) } returns Success(qualifyResponse)
        val mappedExperience = mockk<Experience>()
        coEvery { experienceMapper.map(any(), any()) } returns mappedExperience

        // WHEN
        repository.trackActivity(request)

        // THEN
        coVerify { experienceMapper.map(any(), LOW) }
    }

    @Test
    fun `trackActivity SHOULD mark the experience NORMAL priority WHEN the qualification reason is not screen_view`() = runTest {
        // GIVEN
        val request = ActivityRequest(accountId = "123", userId = "userId")
        val qualifyResponse = QualifyResponse(
            experiences = listOf(mockk(), mockk()),
            performedQualification = true,
            qualificationReason = "event_trigger",
            experiments = emptyMap(),
        )
        coEvery { appcuesRemoteSource.qualify(any(), any()) } returns Success(qualifyResponse)
        val mappedExperience = mockk<Experience>()
        coEvery { experienceMapper.map(any(), any()) } returns mappedExperience

        // WHEN
        repository.trackActivity(request)

        // THEN
        coVerify { experienceMapper.map(any(), NORMAL) }
    }

    // test items already in processing don't get included again
}
