package com.appcues.data.mapper.step

import com.appcues.data.mapper.trait.TraitMapper
import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.step.StepContentResponse
import com.appcues.data.remote.response.step.StepResponse
import com.appcues.data.remote.response.trait.TraitResponse
import com.appcues.domain.entity.ExperienceComponent
import com.appcues.domain.entity.trait.Trait
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.util.UUID

class StepMapperTest {

    private val stepContentMapper = mockk<StepContentMapper>()

    private val traitMapper = mockk<TraitMapper>()

    private val mapper = StepMapper(
        traitMapper = traitMapper,
        stepContentMapper = stepContentMapper
    )

    @Test
    fun `map SHOULD transform StepResponse into Step`() {
        // Given
        val randomId = UUID.randomUUID()
        val stepContentResponse = mockk<StepContentResponse>(relaxed = true)
        val experienceComponent = mockk<ExperienceComponent>()
        every { stepContentMapper.map(stepContentResponse, any()) } returns experienceComponent
        val actionResponse = mockk<ActionResponse>()
        val actionRandomId = UUID.randomUUID()
        val traitResponse = mockk<TraitResponse>()
        val trait = mockk<Trait>()
        every { traitMapper.map(traitResponse) } returns trait
        val from = StepResponse(
            id = randomId,
            content = stepContentResponse,
            actions = hashMapOf(actionRandomId to arrayListOf(actionResponse)),
            traits = arrayListOf(traitResponse),
        )
        // When
        val result = mapper.map(from, hashMapOf())
        // Then
        with(result) {
            assertThat(id).isEqualTo(randomId)
            assertThat(content).isEqualTo(experienceComponent)
            assertThat(traits).hasSize(1)
            assertThat(traits[0]).isEqualTo(trait)
        }
    }
}
