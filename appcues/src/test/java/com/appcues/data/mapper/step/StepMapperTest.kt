package com.appcues.data.mapper.step

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.appcues.data.mapper.LeveledTraitResponse
import com.appcues.data.mapper.trait.TraitsMapper
import com.appcues.data.model.ExperiencePrimitive.HorizontalStackPrimitive
import com.appcues.data.model.ExperiencePrimitive.VerticalStackPrimitive
import com.appcues.data.remote.appcues.response.step.StepResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.BoxPrimitiveResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.ButtonPrimitiveResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.StackPrimitiveResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.TextPrimitiveResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.TextSpanResponse
import com.appcues.data.remote.appcues.response.trait.TraitResponse
import com.appcues.trait.ExperienceTraitLevel
import com.appcues.trait.PresentingTrait
import com.appcues.trait.StepDecoratingPadding
import com.appcues.trait.StepDecoratingTrait
import com.appcues.trait.StepDecoratingTrait.StepDecoratingType
import com.appcues.trait.StepDecoratingTrait.StepDecoratingType.OVERLAY
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.Test
import java.util.UUID

class StepMapperTest {
    @Test
    fun `map SHOULD extract top and bottom sticky content`() {
        // Given
        val mapper = StepMapper(mockk(relaxed = true), mockk(relaxed = true))
        val topItem1 = UUID.randomUUID()
        val topItem2 = UUID.randomUUID()
        val bottomItem1 = UUID.randomUUID()
        val stepResponse = mockStickyContentStepResponse(listOf(topItem1, topItem2), listOf(bottomItem1))

        // When
        val step = mapper.map(stepResponse, listOf())

        // Then
        with(step) {
            assertThat(topStickyContent).isNotNull()
            assertThat(bottomStickyContent).isNotNull()
            // this one is vstack since it had multiple items that get combined
            assertThat(topStickyContent).isInstanceOf(VerticalStackPrimitive::class.java)
            // this one is hstack since it was a single item pulled out directly
            assertThat(bottomStickyContent).isInstanceOf(HorizontalStackPrimitive::class.java)
            val topStack = topStickyContent as VerticalStackPrimitive
            val bottomStack = bottomStickyContent as HorizontalStackPrimitive
            assertThat(topStack.items[0].id).isEqualTo(topItem1)
            assertThat(topStack.items[1].id).isEqualTo(topItem2)
            assertThat(bottomStack.id).isEqualTo(bottomItem1)
        }
    }

    @Test
    fun `map SHOULD not create sticky content WHEN no response items are sticky`() {
        // Given
        val mapper = StepMapper(mockk(relaxed = true), mockk(relaxed = true))
        val bottomItem1 = UUID.randomUUID()
        val stepResponse = mockStickyContentStepResponse(listOf(), listOf(bottomItem1))

        // When
        val step = mapper.map(stepResponse, listOf())

        // Then
        with(step) {
            assertThat(topStickyContent).isNull()
        }
    }

    @Test
    fun `map SHOULD merge container StepDecoratingTraits`() {
        // Given
        val decoratingTrait = mockk<TraitResponse>(relaxed = true)
        val stepContainerTraits = listOf(decoratingTrait to ExperienceTraitLevel.GROUP)
        val mappedGroupTrait = mockk<StepDecoratingTrait>(relaxed = true)
        val traitMapper = mockk<TraitsMapper>(relaxed = true) {
            every { map(stepContainerTraits) } returns listOf(mappedGroupTrait)
        }
        val mapper = StepMapper(traitMapper, mockk(relaxed = true))
        val stepResponse = mockBasicStepResponse(listOf())

        // When
        val step = mapper.map(stepResponse, stepContainerTraits)

        // Then
        with(step) {
            assertThat(stepDecoratingTraits).isNotEmpty()
            assertThat(stepDecoratingTraits[0]).isEqualTo(mappedGroupTrait)
        }
    }

    @Test
    fun `map SHOULD NOT merge container trait when not StepDecoratingTrait`() {
        // Given
        val decoratingTrait = mockk<TraitResponse>(relaxed = true)
        val stepContainerTraits = listOf(decoratingTrait to ExperienceTraitLevel.GROUP)
        val mappedGroupTrait = mockk<PresentingTrait>(relaxed = true)
        val traitMapper = mockk<TraitsMapper>(relaxed = true) {
            every { map(stepContainerTraits) } returns listOf(mappedGroupTrait)
        }
        val mapper = StepMapper(traitMapper, mockk(relaxed = true))
        val stepResponse = mockBasicStepResponse(listOf())

        // When
        val step = mapper.map(stepResponse, stepContainerTraits)

        // Then
        with(step) {
            assertThat(stepDecoratingTraits).isEmpty()
        }
    }

    @Test
    fun `map SHOULD merge step and container StepDecoratingTraits if not same type`() {
        // Given
        val stepDecoratingTrait = mockk<TraitResponse>(relaxed = true) {
            every { this@mockk.type } returns "step-level-decorating"
        }
        val groupDecoratingTrait = mockk<TraitResponse>(relaxed = true) {
            every { this@mockk.type } returns "group-level-decorating"
        }

        val groupTraits = listOf(groupDecoratingTrait to ExperienceTraitLevel.GROUP)

        val slot = slot<List<LeveledTraitResponse>>()
        val traitMapper = mockk<TraitsMapper>(relaxed = true) {
            every { map(capture(slot)) } answers {
                slot.captured.map { leveledTrait ->
                    mockk<TestStepDecoratingTrait>(relaxed = true) {
                        every { this@mockk.type } answers { leveledTrait.first.type }
                    }
                }
            }
        }

        val mapper = StepMapper(traitMapper, mockk(relaxed = true))
        val stepResponse = mockBasicStepResponse(listOf(stepDecoratingTrait))

        // When
        val step = mapper.map(stepResponse, groupTraits)

        // Then
        with(step) {
            assertThat(stepDecoratingTraits.count()).isEqualTo(2)
            assertThat((stepDecoratingTraits[0] as TestStepDecoratingTrait).type).isEqualTo("step-level-decorating")
            assertThat((stepDecoratingTraits[1] as TestStepDecoratingTrait).type).isEqualTo("group-level-decorating")
        }
    }

    @Test
    fun `map SHOULD prefer step-level StepDecoratingTrait over container-level WHEN they are same type`() {
        // Given
        val stepDecoratingTrait = mockk<TraitResponse>(relaxed = true) {
            every { this@mockk.type } returns "step-decorating"
        }
        val groupDecoratingTrait = mockk<TraitResponse>(relaxed = true) {
            every { this@mockk.type } returns "step-decorating"
        }

        val groupTraits = listOf(groupDecoratingTrait to ExperienceTraitLevel.GROUP)

        val slot = slot<List<LeveledTraitResponse>>()
        val traitMapper = mockk<TraitsMapper>(relaxed = true) {
            every { map(capture(slot)) } answers {
                slot.captured.map { leveledTrait ->
                    mockk<TestStepDecoratingTrait>(relaxed = true) {
                        every { this@mockk.level } answers { leveledTrait.second }
                    }
                }
            }
        }

        val mapper = StepMapper(traitMapper, mockk(relaxed = true))
        val stepResponse = mockBasicStepResponse(listOf(stepDecoratingTrait))

        // When
        val step = mapper.map(stepResponse, groupTraits)

        // Then
        with(step) {
            assertThat(stepDecoratingTraits.count()).isEqualTo(1)
            assertThat((stepDecoratingTraits[0] as TestStepDecoratingTrait).level).isEqualTo(ExperienceTraitLevel.STEP)
        }
    }
}

private fun mockBasicStepResponse(stepTraits: List<TraitResponse>) =
    StepResponse(
        id = UUID.randomUUID(),
        content = BoxPrimitiveResponse(
            id = UUID.randomUUID(),
            items = listOf(
                ButtonPrimitiveResponse(
                    id = UUID.randomUUID(),
                    content = TextPrimitiveResponse(UUID.randomUUID(), spans = listOf(TextSpanResponse("hello world")))
                )
            )
        ),
        traits = stepTraits,
        actions = mapOf(),
        type = "step"
    )

private fun mockStickyContentStepResponse(top: List<UUID>, bottom: List<UUID>): StepResponse {
    val topItems = top.map {
        StackPrimitiveResponse(
            id = it,
            orientation = "horizontal",
            items = listOf(),
            sticky = "top"
        )
    }

    val bottomItems = bottom.map {
        StackPrimitiveResponse(
            id = it,
            orientation = "horizontal",
            items = listOf(),
            sticky = "bottom"
        )
    }

    val nonSticky =
        StackPrimitiveResponse(
            id = UUID.randomUUID(),
            orientation = "horizontal",
            items = listOf(
                BoxPrimitiveResponse(
                    id = UUID.randomUUID(),
                    items = listOf(
                        BoxPrimitiveResponse(UUID.randomUUID(), items = listOf())
                    )
                )
            ),
        )

    val items = topItems + nonSticky + bottomItems

    return StepResponse(
        id = UUID.randomUUID(),
        content = StackPrimitiveResponse(
            id = UUID.randomUUID(),
            orientation = "vertical",
            items = items
        ),
        traits = listOf(),
        actions = mapOf(),
        type = "stickyStep"
    )
}

private class TestStepDecoratingTrait : StepDecoratingTrait {

    override val stepComposeOrder: StepDecoratingType
        get() = OVERLAY

    @Composable
    override fun BoxScope.DecorateStep(stepDecoratingPadding: StepDecoratingPadding) {
        return
    }

    override val config: Map<String, Any>?
        get() = null

    var type: String = ""

    var level: ExperienceTraitLevel = ExperienceTraitLevel.EXPERIENCE
}
