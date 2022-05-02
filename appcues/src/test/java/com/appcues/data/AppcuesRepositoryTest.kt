package com.appcues.data

import com.appcues.AppcuesConfig
import com.appcues.data.local.AppcuesLocalSource
import com.appcues.data.mapper.experience.ExperienceMapper
import com.appcues.data.model.Experience
import com.appcues.data.remote.AppcuesRemoteSource
import com.appcues.data.remote.response.experience.ExperienceResponse
import com.appcues.logging.Logcues
import com.appcues.util.ResultOf.Success
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class AppcuesRepositoryTest {

    private val appcuesRemoteSource: AppcuesRemoteSource = mockk()
    private val appcuesLocalSource: AppcuesLocalSource = mockk()
    private val experienceMapper: ExperienceMapper = mockk()
    private val config: AppcuesConfig = mockk()
    private val logcues: Logcues = mockk()

    private val defaultDataGateway = AppcuesRepository(
        appcuesRemoteSource = appcuesRemoteSource,
        appcuesLocalSource = appcuesLocalSource,
        experienceMapper = experienceMapper,
        config = config,
        logcues = logcues,
    )

    @Test
    fun `getContent SHOULD get from appcuesRemoteSource AND map from experienceMapper`() {
        // Given
        val experienceResponse = mockk<ExperienceResponse>()
        coEvery { appcuesRemoteSource.getExperienceContent("1234") } returns Success(experienceResponse)
        val mappedExperience = mockk<Experience>()
        coEvery { experienceMapper.map(experienceResponse) } returns mappedExperience
        // When
        val result = runBlocking {
            defaultDataGateway.getExperienceContent("1234")
        }
        // Then
        assertThat(result).isEqualTo(mappedExperience)
    }
}
