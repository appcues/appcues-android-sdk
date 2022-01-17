package com.appcues.builder.validator

import com.appcues.builder.ApiHostBuilderValidator
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ApiHostBuilderValidatorTest {

    private val validator = ApiHostBuilderValidator()

    @Test
    fun `validate SHOULD not throw an Exception`() {
        // When
        val result = validator.validate("https://api.appcues.com/")
        // Then
        with(result) {
            assertThat(this).isEqualTo("https://api.appcues.com/")
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `validate SHOULD throw an AppcuesBuilderException WHEN apiHost does not start with http`() {
        // WHEN
        validator.validate("api.appcues.com/")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `validate SHOULD throw an AppcuesBuilderException WHEN apiHost does not end with slash`() {
        // WHEN
        validator.validate("https://api.appcues.com")
    }
}
