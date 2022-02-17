package com.appcues.data.mapper.step

import com.appcues.data.mapper.action.ActionMapper
import com.appcues.data.mapper.trait.TraitMapper
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.action.Action
import com.appcues.data.model.trait.Trait
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

    private val actionMapper = mockk<ActionMapper>()

    private val traitMapper = mockk<TraitMapper>()

    private val mapper = StepMapper(
        actionMapper = actionMapper,
        traitMapper = traitMapper,
        stepContentMapper = stepContentMapper
    )

    @Test
    fun `map SHOULD transform StepResponse into Step`() {
        // Given
        val randomId = UUID.randomUUID()
        val stepContentResponse = mockk<StepContentResponse>(relaxed = true)
        val experienceComponent = mockk<ExperiencePrimitive>()
        every { stepContentMapper.map(stepContentResponse) } returns experienceComponent
        val actionResponse = mockk<ActionResponse>()
        val action = mockk<Action>()
        val actionRandomId = UUID.randomUUID()
        every { actionMapper.map(actionResponse) } returns action
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
        val result = mapper.map(from)
        // Then
        with(result) {
            assertThat(id).isEqualTo(randomId)
            assertThat(content).isEqualTo(experienceComponent)
            assertThat(actions).hasSize(1)
            assertThat(actions[actionRandomId]).hasSize(1)
            assertThat(actions[actionRandomId]?.get(0)).isEqualTo(action)
            assertThat(traits).hasSize(1)
            assertThat(traits[0]).isEqualTo(trait)
        }
    }
}
