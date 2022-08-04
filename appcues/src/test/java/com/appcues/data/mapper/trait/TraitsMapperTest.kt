package com.appcues.data.mapper.trait

import com.appcues.data.remote.response.trait.TraitResponse
import com.appcues.trait.ExperienceTrait
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
        val from = arrayListOf(
            TraitResponse(
                type = type,
                config = config
            )
        )
        val experienceTrait: ExperienceTrait = mockk()
        val traitFactoryBlock = mockk<TraitFactoryBlock>()
        every { traitFactoryBlock.invoke(config) } returns experienceTrait
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
