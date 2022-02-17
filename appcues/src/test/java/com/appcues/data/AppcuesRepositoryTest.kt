package com.appcues.data

import com.appcues.data.mapper.experience.ExperienceMapper
import com.appcues.data.model.Experience
import com.appcues.data.remote.AppcuesRemoteSource
import com.appcues.data.remote.response.experience.ExperienceResponse
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class AppcuesRepositoryTest {

    private val appcuesRemoteSource: AppcuesRemoteSource = mockk()
    private val experienceMapper: ExperienceMapper = mockk()

    private val defaultDataGateway = AppcuesRepository(
        appcuesRemoteSource = appcuesRemoteSource,
        experienceMapper = experienceMapper,
    )

    @Test
    fun `getContent SHOULD get from appcuesRemoteSource AND map from experienceMapper`() {
        // Given
        val experienceResponse = mockk<ExperienceResponse>()
        coEvery { appcuesRemoteSource.getContent("1234") } returns experienceResponse
        val mappedExperience = mockk<Experience>()
        coEvery { experienceMapper.map(experienceResponse) } returns mappedExperience
        // When
        val result = runBlocking {
            defaultDataGateway.getContent("1234")
        }
        // Then
        assertThat(result).isEqualTo(mappedExperience)
    }
}
