package com.appcues.data.model.rules

import com.appcues.data.model.rules.Operator.Contains
import com.appcues.data.model.rules.Operator.DoesntContains
import com.appcues.data.model.rules.Operator.DoesntEndsWith
import com.appcues.data.model.rules.Operator.DoesntEquals
import com.appcues.data.model.rules.Operator.DoesntExists
import com.appcues.data.model.rules.Operator.DoesntStartsWith
import com.appcues.data.model.rules.Operator.EndsWith
import com.appcues.data.model.rules.Operator.Equals
import com.appcues.data.model.rules.Operator.Exists
import com.appcues.data.model.rules.Operator.Invalid
import com.appcues.data.model.rules.Operator.IsGreaterOrEqual
import com.appcues.data.model.rules.Operator.IsGreaterThan
import com.appcues.data.model.rules.Operator.IsLessOrEqual
import com.appcues.data.model.rules.Operator.IsLessThan
import com.appcues.data.model.rules.Operator.IsOneOf
import com.appcues.data.model.rules.Operator.IsntOneOf
import com.appcues.data.model.rules.Operator.MatchesRegex
import com.appcues.data.model.rules.Operator.StartsWith
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class OperatorTest {

    @Test
    fun `Invalid Operator tests`() = with(Invalid) {
        assertThat(evaluate("a", "b")).isFalse()
    }

    @Test
    fun `Equals Operator tests`() = with(Equals) {
        assertThat(evaluate("a", "a")).isTrue()
        assertThat(evaluate("abc", "abc")).isTrue()
        assertThat(evaluate(null, null)).isTrue()
        assertThat(evaluate("a", "b")).isFalse()
        assertThat(evaluate(null, "b")).isFalse()
        assertThat(evaluate("abc", null)).isFalse()
    }

    @Test
    fun `DoesntEquals Operator tests`() = with(DoesntEquals) {
        assertThat(evaluate("a", "a")).isFalse()
        assertThat(evaluate("abc", "abc")).isFalse()
        assertThat(evaluate(null, null)).isFalse()
        assertThat(evaluate("a", "b")).isTrue()
        assertThat(evaluate(null, "b")).isTrue()
        assertThat(evaluate("abc", null)).isTrue()
    }

    @Test
    fun `Contains Operator tests`() = with(Contains) {
        assertThat(evaluate("test-123", "123")).isTrue()
        assertThat(evaluate("test-123", "t-1")).isTrue()
        assertThat(evaluate("test-123", "test")).isTrue()
        assertThat(evaluate("test-123", "")).isTrue()
        assertThat(evaluate("test-123", null)).isTrue()
        assertThat(evaluate("test-123", "4")).isFalse()
        assertThat(evaluate(null, "123")).isFalse()
        assertThat(evaluate(null, null)).isFalse()
    }

    @Test
    fun `DoesntContains Operator tests`() = with(DoesntContains) {
        assertThat(evaluate("test-123", "123")).isFalse()
        assertThat(evaluate("test-123", "t-1")).isFalse()
        assertThat(evaluate("test-123", "test")).isFalse()
        assertThat(evaluate("test-123", "")).isFalse()
        assertThat(evaluate("test-123", null)).isFalse()
        assertThat(evaluate("test-123", "4")).isTrue()
        assertThat(evaluate(null, "123")).isTrue()
        assertThat(evaluate(null, null)).isTrue()
    }

    @Test
    fun `StartsWith Operator tests`() = with(StartsWith) {
        assertThat(evaluate("test-123", "test")).isTrue()
        assertThat(evaluate("test-123", "")).isTrue()
        assertThat(evaluate("test-123", null)).isTrue()
        assertThat(evaluate("test-123", "123")).isFalse()
        assertThat(evaluate("test-123", "t-1")).isFalse()
        assertThat(evaluate("test-123", "4")).isFalse()
        assertThat(evaluate(null, "123")).isFalse()
        assertThat(evaluate(null, null)).isFalse()
    }

    @Test
    fun `DoesntStartsWith Operator tests`() = with(DoesntStartsWith) {
        assertThat(evaluate("test-123", "test")).isFalse()
        assertThat(evaluate("test-123", "")).isFalse()
        assertThat(evaluate("test-123", null)).isFalse()
        assertThat(evaluate("test-123", "123")).isTrue()
        assertThat(evaluate("test-123", "t-1")).isTrue()
        assertThat(evaluate("test-123", "4")).isTrue()
        assertThat(evaluate(null, "123")).isTrue()
        assertThat(evaluate(null, null)).isTrue()
    }

    @Test
    fun `EndsWith Operator tests`() = with(EndsWith) {
        assertThat(evaluate("test-123", "123")).isTrue()
        assertThat(evaluate("test-123", "")).isTrue()
        assertThat(evaluate("test-123", null)).isTrue()
        assertThat(evaluate("test-123", "test")).isFalse()
        assertThat(evaluate("test-123", "t-1")).isFalse()
        assertThat(evaluate("test-123", "4")).isFalse()
        assertThat(evaluate(null, "123")).isFalse()
        assertThat(evaluate(null, null)).isFalse()
    }

    @Test
    fun `DoesntEndsWith Operator tests`() = with(DoesntEndsWith) {
        assertThat(evaluate("test-123", "123")).isFalse()
        assertThat(evaluate("test-123", "")).isFalse()
        assertThat(evaluate("test-123", null)).isFalse()
        assertThat(evaluate("test-123", "test")).isTrue()
        assertThat(evaluate("test-123", "t-1")).isTrue()
        assertThat(evaluate("test-123", "4")).isTrue()
        assertThat(evaluate(null, "123")).isTrue()
        assertThat(evaluate(null, null)).isTrue()
    }

    @Test
    fun `MatchesRegex Operator tests`() = with(MatchesRegex) {
        assertThat(evaluate("test-123", "^test")).isTrue()
        assertThat(evaluate("test-123", "-123$")).isTrue()
        assertThat(evaluate("test-123", "123$")).isTrue()
        assertThat(evaluate("test-123", "test-123")).isTrue()
        assertThat(evaluate("test-123", "")).isTrue()
        assertThat(evaluate("test-123", null)).isTrue()
        assertThat(evaluate("test-123", "4")).isFalse()
        assertThat(evaluate(null, "")).isFalse()
        assertThat(evaluate(null, null)).isFalse()
    }

    @Test
    fun `IsOneOf Operator tests`() = with(IsOneOf) {
        assertThat(evaluate("apple", "apple,banana,orange")).isTrue()
        assertThat(evaluate("apple", "banana, apple, orange")).isTrue()
        assertThat(evaluate("apple", "banana/napple/norange")).isTrue()
        assertThat(evaluate("apple", "banana,orange")).isFalse()
        assertThat(evaluate(null, "")).isFalse()
        assertThat(evaluate(null, null)).isFalse()
    }

    @Test
    fun `IsntOneOf Operator tests`() = with(IsntOneOf) {
        assertThat(evaluate("apple", "apple,banana,orange")).isFalse()
        assertThat(evaluate("apple", "banana, apple, orange")).isFalse()
        assertThat(evaluate("apple", "banana/napple/norange")).isFalse()
        assertThat(evaluate("apple", "banana,orange")).isTrue()
        assertThat(evaluate(null, "")).isTrue()
        assertThat(evaluate(null, null)).isTrue()
    }

    @Test
    fun `Exists Operator tests`() = with(Exists) {
        assertThat(evaluate("apple", null)).isTrue()
        assertThat(evaluate(null, null)).isFalse()
    }

    @Test
    fun `DoesntExists Operator tests`() = with(DoesntExists) {
        assertThat(evaluate("apple", null)).isFalse()
        assertThat(evaluate(null, null)).isTrue()
    }

    @Test
    fun `IsGreaterThan Operator tests`() = with(IsGreaterThan) {
        assertThat(evaluate("1", "0")).isTrue()
        assertThat(evaluate("5", "2")).isTrue()
        assertThat(evaluate("0", "-10")).isTrue()
        assertThat(evaluate("0", "0")).isFalse()
        assertThat(evaluate("2.0.0", "1.0.0")).isTrue()
        assertThat(evaluate("1.1.0", "2.0.0")).isFalse()
        assertThat(evaluate("1.0.1", "1.0.1")).isFalse()
        assertThat(evaluate("0", null)).isTrue()
        assertThat(evaluate(null, "0")).isFalse()
        assertThat(evaluate(null, null)).isFalse()
    }

    @Test
    fun `IsGreaterOrEqual Operator tests`() = with(IsGreaterOrEqual) {
        assertThat(evaluate("1", "0")).isTrue()
        assertThat(evaluate("5", "2")).isTrue()
        assertThat(evaluate("0", "-10")).isTrue()
        assertThat(evaluate("0", "0")).isTrue()
        assertThat(evaluate("2.0.0", "1.0.0")).isTrue()
        assertThat(evaluate("1.1.1", "1.1.1")).isTrue()
        assertThat(evaluate("1.0.5", "2.0.0")).isFalse()
        assertThat(evaluate("0", null)).isTrue()
        assertThat(evaluate(null, null)).isTrue()
        assertThat(evaluate(null, "0")).isFalse()
    }

    @Test
    fun `IsLessThan Operator tests`() = with(IsLessThan) {
        assertThat(evaluate("1", "0")).isFalse()
        assertThat(evaluate("5", "2")).isFalse()
        assertThat(evaluate("0", "-10")).isFalse()
        assertThat(evaluate("0", "0")).isFalse()
        assertThat(evaluate("2.0.0", "1.0.0")).isFalse()
        assertThat(evaluate("1.1.0", "2.0.0")).isTrue()
        assertThat(evaluate("1.0.1", "1.0.1")).isFalse()
        assertThat(evaluate("0", null)).isFalse()
        assertThat(evaluate(null, "0")).isTrue()
        assertThat(evaluate(null, null)).isFalse()
    }

    @Test
    fun `IsLessOrEqual Operator tests`() = with(IsLessOrEqual) {
        assertThat(evaluate("1", "0")).isFalse()
        assertThat(evaluate("5", "2")).isFalse()
        assertThat(evaluate("0", "-10")).isFalse()
        assertThat(evaluate("0", "0")).isTrue()
        assertThat(evaluate("2.0.0", "1.0.0")).isFalse()
        assertThat(evaluate("1.1.0", "2.0.0")).isTrue()
        assertThat(evaluate("1.0.1", "1.0.1")).isTrue()
        assertThat(evaluate("0", null)).isFalse()
        assertThat(evaluate(null, "0")).isTrue()
        assertThat(evaluate(null, null)).isTrue()
    }
}
