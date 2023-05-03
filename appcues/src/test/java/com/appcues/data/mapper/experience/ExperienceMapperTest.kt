package com.appcues.data.mapper.experience

internal class ExperienceMapperTest {

    //    private val mapper = ExperienceMapper(
    //        traitMapper = traitMapper,
    //        stepMapper = stepMapper
    //    )
    //
    //    @Test
    //    fun `map SHOULD transform ExperienceResponse into Experience`() {
    //        // GIVEN
    //        val randomId = UUID.randomUUID()
    //
    //        val stepResponse = mockk<StepResponse>()
    //        val step = mockk<Step>()
    //        val actions = hashMapOf<UUID, List<ActionResponse>>()
    //        every { stepMapper.map(stepResponse, actions) } returns step
    //        val traitResponse = mockk<TraitResponse>()
    //        val trait = mockk<Trait>()
    //        every { traitMapper.map(traitResponse) } returns trait
    //        val from = ExperienceResponse(
    //            id = randomId,
    //            name = "Test Experience",
    //            theme = ExperienceThemeResponse(),
    //            actions = actions,
    //            traits = arrayListOf(traitResponse),
    //            steps = arrayListOf(stepResponse),
    //        )
    //        // WHEN
    //        val result = mapper.map(from)
    //        // THEN
    //        with(result) {
    //            assertThat(id).isEqualTo(randomId)
    //            assertThat(name).isEqualTo("Test Experience")
    //            assertThat(steps).hasSize(1)
    //            assertThat(steps[0]).isEqualTo(step)
    //            assertThat(traits).hasSize(1)
    //            assertThat(traits[0]).isEqualTo(trait)
    //        }
    //    }
}
