package com.appcues.data.mapper.experience

import com.appcues.data.model.ExperienceTrigger
import com.appcues.data.remote.appcues.response.experience.ContextResponse
import com.appcues.data.remote.appcues.response.experience.ExperienceResponse
import com.appcues.data.remote.appcues.response.experience.FailedExperienceResponse
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Test
import java.util.UUID

internal class ExperienceMapperTest {

    @Test
    fun `mapDecoded SHOULD set locale properties from ExperienceResponse context`() {
        // GIVEN
        val mapper = ExperienceMapper(
            stepMapper = mockk(relaxed = true),
            actionsMapper = mockk(relaxed = true),
            traitsMapper = mockk(relaxed = true),
            scope = mockk(relaxed = true)
        )
        val response = ExperienceResponse(
            id = UUID.randomUUID(),
            name = "name",
            theme = null,
            traits = listOf(),
            steps = listOf(),
            state = null,
            type = null,
            publishedAt = null,
            nextContentId = null,
            redirectUrl = null,
            context = ContextResponse("locale-id", "locale-name")
        )

        // WHEN
        val experience = mapper.mapDecoded(response, ExperienceTrigger.ShowCall)

        // THEN
        assertThat(experience.localeId).isEqualTo("locale-id")
        assertThat(experience.localeName).isEqualTo("locale-name")
    }

    @Test
    fun `mapDecoded SHOULD NOT set locale properties from ExperienceResponse WHEN context is null`() {
        // GIVEN
        val mapper = ExperienceMapper(
            stepMapper = mockk(relaxed = true),
            actionsMapper = mockk(relaxed = true),
            traitsMapper = mockk(relaxed = true),
            scope = mockk(relaxed = true)
        )
        val response = ExperienceResponse(
            id = UUID.randomUUID(),
            name = "name",
            theme = null,
            traits = listOf(),
            steps = listOf(),
            state = null,
            type = null,
            publishedAt = null,
            nextContentId = null,
            redirectUrl = null,
            context = null
        )

        // WHEN
        val experience = mapper.mapDecoded(response, ExperienceTrigger.ShowCall)

        // THEN
        assertThat(experience.localeId).isNull()
        assertThat(experience.localeName).isNull()
    }

    @Test
    fun `mapDecoded SHOULD set locale properties from FailedExperienceResponse context`() {
        // GIVEN
        val mapper = ExperienceMapper(
            stepMapper = mockk(relaxed = true),
            actionsMapper = mockk(relaxed = true),
            traitsMapper = mockk(relaxed = true),
            scope = mockk(relaxed = true)
        )
        val response = FailedExperienceResponse(
            id = UUID.randomUUID(),
            name = "name",
            type = null,
            publishedAt = null,
            context = ContextResponse("locale-id", "locale-name")
        )

        // WHEN
        val experience = mapper.mapDecoded(response, ExperienceTrigger.ShowCall)

        // THEN
        assertThat(experience.localeId).isEqualTo("locale-id")
        assertThat(experience.localeName).isEqualTo("locale-name")
    }
}
