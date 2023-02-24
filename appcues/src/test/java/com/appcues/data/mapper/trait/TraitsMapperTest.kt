package com.appcues.data.mapper.trait

import com.appcues.data.remote.appcues.response.trait.TraitResponse
import com.appcues.trait.ExperienceTrait
import com.appcues.trait.ExperienceTraitLevel
import com.appcues.trait.TraitFactoryBlock
import com.appcues.trait.TraitRegistry
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class TraitsMapperTest {

    private val traitRegistry = mockk<TraitRegistry>()

    private val mapper = TraitsMapper(traitRegistry)

    @Test
    fun `map SHOULD map TraitResponse into ExperienceTrait`() {
        // Given
        val type = "trait-type"
        val config = hashMapOf<String, Any>()
        val level = ExperienceTraitLevel.STEP
        val from = arrayListOf(
            TraitResponse(
                type = type,
                config = config
            ) to level
        )
        val experienceTrait: ExperienceTrait = mockk()
        val traitFactoryBlock = mockk<TraitFactoryBlock>()
        every { traitFactoryBlock.invoke(config, level) } returns experienceTrait
        every { traitRegistry[type] } returns traitFactoryBlock
        // When
        val result = mapper.map(from)
        // Then
        with(result) {
            assertThat(this).hasSize(1)
            assertThat(this[0]).isEqualTo(experienceTrait)
        }
    }
}
