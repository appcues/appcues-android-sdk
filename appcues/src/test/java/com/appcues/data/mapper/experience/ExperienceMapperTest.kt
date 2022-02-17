package com.appcues.data.mapper.experience

import com.appcues.data.mapper.action.ActionMapper
import com.appcues.data.mapper.step.StepMapper
import com.appcues.data.mapper.trait.TraitMapper
import com.appcues.data.model.action.Action
import com.appcues.data.model.step.Step
import com.appcues.data.model.trait.Trait
import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.experience.ExperienceResponse
import com.appcues.data.remote.response.experience.ExperienceThemeResponse
import com.appcues.data.remote.response.step.StepResponse
import com.appcues.data.remote.response.trait.TraitResponse
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.util.UUID

class ExperienceMapperTest {

    private val stepMapper = mockk<StepMapper>()

    private val actionMapper = mockk<ActionMapper>()

    private val traitMapper = mockk<TraitMapper>()

    private val mapper = ExperienceMapper(
        actionMapper = actionMapper,
        traitMapper = traitMapper,
        stepMapper = stepMapper
    )

    @Test
    fun `map SHOULD transform ExperienceResponse into Experience`() {
        // GIVEN
        val randomId = UUID.randomUUID()

        val stepResponse = mockk<StepResponse>()
        val step = mockk<Step>()
        every { stepMapper.map(stepResponse) } returns step
        val actionResponse = mockk<ActionResponse>()
        val action = mockk<Action>()
        val actionRandomId = UUID.randomUUID()
        every { actionMapper.map(actionResponse) } returns action
        val traitResponse = mockk<TraitResponse>()
        val trait = mockk<Trait>()
        every { traitMapper.map(traitResponse) } returns trait
        val from = ExperienceResponse(
            id = randomId,
            name = "Test Experience",
            theme = ExperienceThemeResponse(),
            actions = hashMapOf(actionRandomId to arrayListOf(actionResponse)),
            traits = arrayListOf(traitResponse),
            steps = arrayListOf(stepResponse),
        )
        // WHEN
        val result = mapper.map(from)
        // THEN
        with(result) {
            assertThat(id).isEqualTo(randomId)
            assertThat(name).isEqualTo("Test Experience")
            assertThat(steps).hasSize(1)
            assertThat(steps[0]).isEqualTo(step)
            assertThat(actions).hasSize(1)
            assertThat(actions[actionRandomId]).hasSize(1)
            assertThat(actions[actionRandomId]?.get(0)).isEqualTo(action)
            assertThat(traits).hasSize(1)
            assertThat(traits[0]).isEqualTo(trait)
        }
    }
}
