package com.appcues.data.mapper.step

import com.appcues.data.mapper.trait.TraitMapper
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.Trait
import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.step.StepContentResponse
import com.appcues.data.remote.response.step.StepResponse
import com.appcues.data.remote.response.trait.TraitResponse
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
        val experienceComponent = mockk<ExperiencePrimitive>()
        val actions = hashMapOf<UUID, List<ActionResponse>>()
        every { stepContentMapper.map(stepContentResponse, actions) } returns experienceComponent
        val traitResponse = mockk<TraitResponse>()
        val trait = mockk<Trait>()
        every { traitMapper.map(traitResponse) } returns trait
        val from = StepResponse(
            id = randomId,
            content = stepContentResponse,
            actions = actions,
            traits = arrayListOf(traitResponse),
        )
        // When
        val result = mapper.map(from, actions)
        // Then
        with(result) {
            assertThat(id).isEqualTo(randomId)
            assertThat(content).isEqualTo(experienceComponent)
            assertThat(traits).hasSize(1)
            assertThat(traits[0]).isEqualTo(trait)
        }
    }
}
