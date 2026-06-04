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
    fun `mapDecoded SHOULD set context properties from ExperienceResponse context`() {
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
            context = ContextResponse(
                localeId = "locale-id",
                localeName = "locale-name",
                workflowId = UUID.randomUUID(),
                workflowTaskId = UUID.randomUUID()
            )
        )

        // WHEN
        val experience = mapper.mapDecoded(response, ExperienceTrigger.ShowCall)

        // THEN
        assertThat(experience.localeId).isEqualTo("locale-id")
        assertThat(experience.localeName).isEqualTo("locale-name")
        assertThat(experience.workflowId).isEqualTo(response.context?.workflowId)
        assertThat(experience.workflowTaskId).isEqualTo(response.context?.workflowTaskId)
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
        assertThat(experience.workflowId).isNull()
        assertThat(experience.workflowTaskId).isNull()
    }

    @Test
    fun `mapDecoded SHOULD set campaign properties from ExperienceResponse`() {
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
            context = null,
            campaignId = "campaign-123",
            tacticId = "tactic-456",
        )

        // WHEN
        val experience = mapper.mapDecoded(response, ExperienceTrigger.ShowCall)

        // THEN
        assertThat(experience.campaignId).isEqualTo("campaign-123")
        assertThat(experience.tacticId).isEqualTo("tactic-456")
    }

    @Test
    fun `mapDecoded SHOULD set campaign properties to null WHEN not present in ExperienceResponse`() {
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
            context = null,
        )

        // WHEN
        val experience = mapper.mapDecoded(response, ExperienceTrigger.ShowCall)

        // THEN
        assertThat(experience.campaignId).isNull()
        assertThat(experience.tacticId).isNull()
    }

    @Test
    fun `mapDecoded SHOULD set campaign properties from FailedExperienceResponse`() {
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
            context = null,
            campaignId = "campaign-789",
            tacticId = "tactic-012",
        )

        // WHEN
        val experience = mapper.mapDecoded(response, ExperienceTrigger.ShowCall)

        // THEN
        assertThat(experience.campaignId).isEqualTo("campaign-789")
        assertThat(experience.tacticId).isEqualTo("tactic-012")
    }

    @Test
    fun `mapDecoded SHOULD use parent campaign properties from LaunchExperienceAction trigger`() {
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
            context = null,
            campaignId = "child-api-campaign",
            tacticId = "child-api-tactic",
        )
        val trigger = ExperienceTrigger.LaunchExperienceAction(
            fromExperienceId = UUID.randomUUID(),
            campaignId = "parent-campaign",
            tacticId = "parent-tactic",
        )

        // WHEN
        val experience = mapper.mapDecoded(response, trigger)

        // THEN — parent's IDs override the API response values
        assertThat(experience.campaignId).isEqualTo("parent-campaign")
        assertThat(experience.tacticId).isEqualTo("parent-tactic")
    }

    @Test
    fun `mapDecoded SHOULD use parent campaign properties from ExperienceCompletionAction trigger`() {
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
            context = null,
            campaignId = "child-api-campaign",
            tacticId = "child-api-tactic",
        )
        val trigger = ExperienceTrigger.ExperienceCompletionAction(
            fromExperienceId = UUID.randomUUID(),
            campaignId = "parent-campaign",
            tacticId = "parent-tactic",
        )

        // WHEN
        val experience = mapper.mapDecoded(response, trigger)

        // THEN — parent's IDs override the API response values
        assertThat(experience.campaignId).isEqualTo("parent-campaign")
        assertThat(experience.tacticId).isEqualTo("parent-tactic")
    }

    @Test
    fun `mapDecoded SHOULD set campaign to null WHEN parent trigger has no campaign`() {
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
            context = null,
            campaignId = "child-api-campaign",
            tacticId = "child-api-tactic",
        )
        val trigger = ExperienceTrigger.LaunchExperienceAction(
            fromExperienceId = UUID.randomUUID(),
            campaignId = null,
            tacticId = null,
        )

        // WHEN
        val experience = mapper.mapDecoded(response, trigger)

        // THEN — parent had no campaign, so child should not use its own API values
        assertThat(experience.campaignId).isNull()
        assertThat(experience.tacticId).isNull()
    }

    @Test
    fun `mapDecoded SHOULD set context properties from FailedExperienceResponse context`() {
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
            context = ContextResponse(
                localeId = "locale-id",
                localeName = "locale-name",
                workflowId = UUID.randomUUID(),
                workflowTaskId = UUID.randomUUID()
            )
        )

        // WHEN
        val experience = mapper.mapDecoded(response, ExperienceTrigger.ShowCall)

        // THEN
        assertThat(experience.localeId).isEqualTo("locale-id")
        assertThat(experience.localeName).isEqualTo("locale-name")
        assertThat(experience.workflowId).isEqualTo(response.context?.workflowId)
        assertThat(experience.workflowTaskId).isEqualTo(response.context?.workflowTaskId)
    }
}
