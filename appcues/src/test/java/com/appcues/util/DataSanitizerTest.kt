package com.appcues.util

import com.appcues.data.model.ExperienceStepFormState
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.util.Date
import java.util.UUID

internal class DataSanitizerTest {

    private val sanitizer = DataSanitizer()

    @Test
    fun `map sanitize SHOULD remove null value`() = with(sanitizer) {
        // Given
        val map = hashMapOf<String, Any?>("prop1" to null)
        // When
        val sanitizedMap = map.sanitize()
        // Then
        assertThat(sanitizedMap).hasSize(0)
    }

    @Test
    fun `map sanitize SHOULD keep primitive value`() = with(sanitizer) {
        // Given
        val map = hashMapOf<String, Any?>("prop1" to 1, "prop2" to "value")
        // When
        val sanitizedMap = map.sanitize()
        // Then
        assertThat(sanitizedMap).hasSize(2)
        assertThat(sanitizedMap).containsEntry("prop1", 1)
        assertThat(sanitizedMap).containsEntry("prop2", "value")
    }

    @Test
    fun `map sanitize SHOULD handle ExperienceStepFormState`() = with(sanitizer) {
        // Given
        val experienceStepFormState = mockk<ExperienceStepFormState>().apply {
            every { toHashMap() } returns hashMapOf("form_prop" to 1)
        }
        val map = hashMapOf<String, Any?>("prop1" to experienceStepFormState)
        // When
        val sanitizedMap = map.sanitize()
        // Then
        assertThat(sanitizedMap).hasSize(1)
        assertThat(sanitizedMap).containsEntry("prop1", hashMapOf("form_prop" to 1))
    }

    @Test
    fun `map sanitize SHOULD map Date to Double`() = with(sanitizer) {
        // Given
        val map = hashMapOf<String, Any?>("prop1" to Date(1666102372942))
        // When
        val sanitizedMap = map.sanitize()
        // Then
        assertThat(sanitizedMap).hasSize(1)
        assertThat(sanitizedMap).containsEntry("prop1", 1666102372942.0)
    }

    @Test
    fun `map sanitize SHOULD map UUID to String`() = with(sanitizer) {
        // Given
        val uuid = UUID.randomUUID()
        val map = hashMapOf<String, Any?>("prop1" to uuid)
        // When
        val sanitizedMap = map.sanitize()
        // Then
        assertThat(sanitizedMap).hasSize(1)
        assertThat(sanitizedMap).containsEntry("prop1", uuid.toString())
    }

    @Test
    fun `map sanitize SHOULD flat inner maps`() = with(sanitizer) {
        // Given
        val map = hashMapOf<String, Any?>("prop1" to hashMapOf("prop2" to 1))
        // When
        val sanitizedMap = map.sanitize()
        // Then
        assertThat(sanitizedMap).hasSize(1)
        assertThat(sanitizedMap).containsEntry("prop1", hashMapOf("prop2" to 1))
    }

    @Test
    fun `map sanitize SHOULD items from a list`() = with(sanitizer) {
        // Given
        val map = hashMapOf<String, Any?>("prop1" to listOf(Date(1666102372942), 2, 3, 4, 5))
        // When
        val sanitizedMap = map.sanitize()
        // Then
        assertThat(sanitizedMap).hasSize(1)
        assertThat(sanitizedMap).containsEntry("prop1", listOf(1666102372942.0, 2, 3, 4, 5))
    }

    @Test
    fun `list sanitize SHOULD filter null items`() = with(sanitizer) {
        // Given
        val list = listOf(1, null, 2)
        // When
        val sanitizedList = list.sanitize()
        // Then
        assertThat(sanitizedList).hasSize(2)
    }

    @Test
    fun `list sanitize SHOULD handle ExperienceStepFormState`() = with(sanitizer) {
        // Given
        val experienceStepFormState = mockk<ExperienceStepFormState>().apply {
            every { toHashMap() } returns hashMapOf("form_prop" to 1)
        }
        // When
        val sanitizedList = listOf(experienceStepFormState).sanitize()
        // Then
        assertThat(sanitizedList).hasSize(1)
        assertThat(sanitizedList).containsExactly(hashMapOf("form_prop" to 1))
        return@with
    }

    @Test
    fun `list sanitize SHOULD map Date to Double`() = with(sanitizer) {
        // Given
        val list = listOf(Date(1666102372942))
        // When
        val sanitizedList = list.sanitize()
        // Then
        assertThat(sanitizedList).hasSize(1)
        assertThat(sanitizedList).containsExactly(1666102372942.0)
        return@with
    }

    @Test
    fun `list sanitize SHOULD map UUID to String`() = with(sanitizer) {
        // Given
        val uuid = UUID.randomUUID()
        val list = listOf(uuid)
        // When
        val sanitizedList = list.sanitize()
        // Then
        assertThat(sanitizedList).hasSize(1)
        assertThat(sanitizedList).containsExactly(uuid.toString())
        return@with
    }
    
    @Test
    fun `list sanitize SHOULD handle maps`() = with(sanitizer) {
        // Given
        val list = listOf(hashMapOf("prop2" to Date(1666102372942)))
        // When
        val sanitizedList = list.sanitize()
        // Then
        assertThat(sanitizedList).hasSize(1)
        assertThat(sanitizedList).containsExactly(hashMapOf("prop2" to 1666102372942.0))
        return@with
    }

    @Test
    fun `list sanitize SHOULD handle lists`() = with(sanitizer) {
        // Given
        val list = listOf(listOf(Date(1666102372942)))
        // When
        val sanitizedList = list.sanitize()
        // Then
        assertThat(sanitizedList).hasSize(1)
        assertThat(sanitizedList).containsExactly(listOf(1666102372942.0))
        return@with
    }
}
