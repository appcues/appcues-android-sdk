package com.appcues.data

import com.appcues.data.local.AppcuesLocalSource
import com.appcues.data.mapper.experience.ExperienceMapper
import com.appcues.data.model.Experience
import com.appcues.data.remote.AppcuesRemoteSource
import com.appcues.data.remote.response.experience.ExperienceResponse
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class AppcuesRepositoryTest {

    private val appcuesRemoteSource: AppcuesRemoteSource = mockk()
    private val appcuesLocalSource: AppcuesLocalSource = mockk()
    private val experienceMapper: ExperienceMapper = mockk()
    private val gson: Gson = mockk()

    private val defaultDataGateway = AppcuesRepository(
        appcuesRemoteSource = appcuesRemoteSource,
        appcuesLocalSource = appcuesLocalSource,
        experienceMapper = experienceMapper,
        gson = gson,
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
