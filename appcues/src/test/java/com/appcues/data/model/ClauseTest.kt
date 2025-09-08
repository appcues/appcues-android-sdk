package com.appcues.data.model

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert
import org.junit.Test
import java.util.UUID

@Suppress("LargeClass")
internal class ClauseTest {

    private val moshi = Moshi.Builder()
        .add(ClauseAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // MARK: - And Clause Tests

    @Test
    fun testAndClause() {
        val andClause = Clause.And(listOf(
            Clause.Token(Clause.TokenClause("premium", Clause.Operator.EQUALS, "premium")),
            Clause.Token(Clause.TokenClause("active", Clause.Operator.EQUALS, "active"))
        ))

        assertThat(andClause.evaluate(emptyMap())).isTrue()
        assertThat(andClause.toString()).isEqualTo("premium equals premium AND active equals active")
    }

    @Test
    fun testAndClauseWithFalseCondition() {
        val andClause = Clause.And(listOf(
            Clause.Token(Clause.TokenClause("premium", Clause.Operator.EQUALS, "premium")),
            Clause.Token(Clause.TokenClause("active", Clause.Operator.EQUALS, "wrong_value"))
        ))
        
        assertThat(andClause.evaluate(emptyMap())).isFalse()
    }
    
    @Test
    fun testSingleAnd() {
        val andClause = Clause.And(listOf(
            Clause.Token(Clause.TokenClause("premium", Clause.Operator.EQUALS, "premium"))
        ))

        assertThat(andClause.evaluate(emptyMap())).isTrue()
        assertThat(andClause.toString()).isEqualTo("premium equals premium")
    }

    @Test
    fun testEmptyAndClause() {
        val andClause = Clause.And(emptyList())
        val state = emptyMap<UUID, String>()

        // Empty AND clause should evaluate to true (all conditions are satisfied)
        assertThat(andClause.evaluate(state)).isTrue()
        assertThat(andClause.toString()).isEqualTo("")
    }

    // MARK: - Or Clause Tests

    @Test
    fun testOrClause() {
        val orClause = Clause.Or(listOf(
            Clause.Token(Clause.TokenClause("premium", Clause.Operator.EQUALS, "wrong_value")),
            Clause.Token(Clause.TokenClause("active", Clause.Operator.EQUALS, "active"))
        ))
        
        assertThat(orClause.evaluate(emptyMap())).isTrue()
        assertThat(orClause.toString()).isEqualTo("premium equals wrong_value OR active equals active")
    }

    @Test
    fun testOrClauseWithAllFalseConditions() {
        val orClause = Clause.Or(listOf(
            Clause.Token(Clause.TokenClause("premium", Clause.Operator.EQUALS, "wrong_value1")),
            Clause.Token(Clause.TokenClause("active", Clause.Operator.EQUALS, "wrong_value2"))
        ))
        
        assertThat(orClause.evaluate(emptyMap())).isFalse()
    }
    
    @Test
    fun testSingleOr() {
        val orClause = Clause.Or(listOf(
            Clause.Token(Clause.TokenClause("active", Clause.Operator.EQUALS, "active"))
        ))
        
        assertThat(orClause.evaluate(emptyMap())).isTrue()
        assertThat(orClause.toString()).isEqualTo("active equals active")
    }

    @Test
    fun testEmptyOrClause() {
        val orClause = Clause.Or(emptyList())
        val state = emptyMap<UUID, String>()

        // Empty OR clause should evaluate to false (no conditions are satisfied)
        assertThat(orClause.evaluate(state)).isFalse()
        assertThat(orClause.toString()).isEqualTo("")
    }

    // MARK: - Not Clause Tests

    @Test
    fun testNotClause() {
        val notClause = Clause.Not(
            Clause.Token(Clause.TokenClause("premium", Clause.Operator.EQUALS, "wrong_value"))
        )
        
        assertThat(notClause.evaluate(emptyMap())).isTrue()
        assertThat(notClause.toString()).isEqualTo("NOT premium equals wrong_value")
    }

    @Test
    fun testNotClauseWithTrueCondition() {
        val notClause = Clause.Not(
            Clause.Token(Clause.TokenClause("premium", Clause.Operator.EQUALS, "premium"))
        )
        
        assertThat(notClause.evaluate(emptyMap())).isFalse()
    }

    @Test
    fun testNestedNotClause() {
        val tokenClause = Clause.TokenClause("premium", Clause.Operator.EQUALS, "premium")
        val notClause = Clause.Not(Clause.Token(tokenClause))
        val doubleNotClause = Clause.Not(notClause)

        assertThat(doubleNotClause.evaluate(emptyMap())).isTrue()
        assertThat(doubleNotClause.toString()).isEqualTo("NOT NOT premium equals premium")
    }

    // MARK: - Survey Clause Tests

    @Test
    fun testSurveyClause() {
        val block = UUID.randomUUID()
        val state = mapOf(block to "test_value")

        val clause = Clause.Survey(Clause.SurveyClause(block, Clause.Operator.EQUALS, "test_value"))
        
        assertThat(clause.evaluate(state)).isTrue()
        assertThat(clause.toString()).isEqualTo("$block equals test_value")
    }

    @Test
    fun testSurveyClauseWithMissingBlock() {
        val block = UUID.randomUUID()
        val state = emptyMap<UUID, String>()

        val surveyClause = Clause.SurveyClause(block, Clause.Operator.EQUALS, "test_value")
        val clause = Clause.Survey(surveyClause)
        
        assertThat(clause.evaluate(state)).isFalse()
    }

    // MARK: - Token Clause Tests

    @Test
    fun testTokenClause() {
        val tokenClause = Clause.TokenClause("test_token", Clause.Operator.EQUALS, "test_token")
        val clause = Clause.Token(tokenClause)
        
        assertThat(clause.evaluate(emptyMap())).isTrue()
        assertThat(clause.toString()).isEqualTo("test_token equals test_token")
    }

    @Test
    fun testTokenClauseWithDifferentValues() {
        val tokenClause = Clause.TokenClause("test_token", Clause.Operator.EQUALS, "different_value")
        val clause = Clause.Token(tokenClause)

        assertThat(clause.evaluate(emptyMap())).isFalse()
        assertThat(clause.toString()).isEqualTo("test_token equals different_value")
    }

    // MARK: - Unknown Clause Tests

    @Test
    fun testUnknownClause() {
        val clause = Clause.Unknown()
        val state = emptyMap<UUID, String>()

        assertThat(clause.evaluate(state)).isFalse()
        assertThat(clause.toString()).isEqualTo("unknown clause")
    }

    // MARK: - Operator Tests

    @Test
    fun testEqualsOperator() {
        val op = Clause.Operator.EQUALS
        assertThat(op.evaluate("test", "test")).isTrue()
        assertThat(op.evaluate("test", "other")).isFalse()
        assertThat(op.toString()).isEqualTo("equals")
    }

    @Test
    fun testCaseSensitiveOperators() {
        val op = Clause.Operator.EQUALS
        assertThat(op.evaluate("Hello", "hello")).isFalse()
        assertThat(op.evaluate("Hello", "Hello")).isTrue()
    }

    @Test
    fun testWhitespaceHandling() {
        val op = Clause.Operator.EQUALS
        assertThat(op.evaluate("hello", " hello ")).isFalse()
        assertThat(op.evaluate(" hello ", " hello ")).isTrue()
    }

    @Test
    fun testDoesntEqualOperator() {
        val op = Clause.Operator.DOESNT_EQUAL
        assertThat(op.evaluate("test", "other")).isTrue()
        assertThat(op.evaluate("test", "test")).isFalse()
        assertThat(op.toString()).isEqualTo("doesn't equal")
    }

    @Test
    fun testContainsOperator() {
        val op = Clause.Operator.CONTAINS
        assertThat(op.evaluate("hello world", "world")).isTrue()
        assertThat(op.evaluate("hello world", "hello")).isTrue()
        assertThat(op.evaluate("hello world", "ll")).isTrue()
        assertThat(op.evaluate("hello world", "xyz")).isFalse()
        assertThat(op.toString()).isEqualTo("contains")
    }

    @Test
    fun testDoesntContainOperator() {
        val op = Clause.Operator.DOESNT_CONTAIN
        assertThat(op.evaluate("hello world", "xyz")).isTrue()
        assertThat(op.evaluate("hello world", "world")).isFalse()
        assertThat(op.evaluate("hello world", "hello")).isFalse()
        assertThat(op.toString()).isEqualTo("doesn't contain")
    }

    @Test
    fun testStartsWithOperator() {
        val op = Clause.Operator.STARTS_WITH
        assertThat(op.evaluate("hello world", "hello")).isTrue()
        assertThat(op.evaluate("hello world", "world")).isFalse()
        assertThat(op.evaluate("hello world", "xyz")).isFalse()
        assertThat(op.toString()).isEqualTo("starts with")
    }

    @Test
    fun testDoesntStartWithOperator() {
        val op = Clause.Operator.DOESNT_START_WITH
        assertThat(op.evaluate("hello world", "world")).isTrue()
        assertThat(op.evaluate("hello world", "xyz")).isTrue()
        assertThat(op.evaluate("hello world", "hello")).isFalse()
        assertThat(op.toString()).isEqualTo("doesn't start with")
    }

    @Test
    fun testEndsWithOperator() {
        val op = Clause.Operator.ENDS_WITH
        assertThat(op.evaluate("hello world", "world")).isTrue()
        assertThat(op.evaluate("hello world", "hello")).isFalse()
        assertThat(op.evaluate("hello world", "xyz")).isFalse()
        assertThat(op.toString()).isEqualTo("ends with")
    }

    @Test
    fun testDoesntEndWithOperator() {
        val op = Clause.Operator.DOESNT_END_WITH
        assertThat(op.evaluate("hello world", "hello")).isTrue()
        assertThat(op.evaluate("hello world", "xyz")).isTrue()
        assertThat(op.evaluate("hello world", "world")).isFalse()
        assertThat(op.toString()).isEqualTo("doesn't end with")
    }

    @Test
    fun testMatchesRegexOperator() {
        val op = Clause.Operator.MATCHES_REGEX
        assertThat(op.evaluate("hello123world", "\\d+")).isTrue()
        assertThat(op.evaluate("hello world", "\\d+")).isFalse()
        assertThat(op.toString()).isEqualTo("matches regex")
    }

    @Test
    fun testMatchesRegexOperatorWithInvalidPattern() {
        val op = Clause.Operator.MATCHES_REGEX
        assertThat(op.evaluate("test", "[")).isFalse() // Invalid regex pattern
        assertThat(op.evaluate("test", "")).isFalse() // Empty regex pattern
    }

    @Test
    fun testIsOneOfOperator() {
        val op = Clause.Operator.IS_ONE_OF
        assertThat(op.evaluate("option1\noption2\noption3", "option2")).isTrue()
        assertThat(op.evaluate("option1\noption2\noption3", "option1")).isTrue()
        assertThat(op.evaluate("option1\noption2\noption3", "option4")).isFalse()
        assertThat(op.toString()).isEqualTo("is one of")
    }

    @Test
    fun testIsntOneOfOperator() {
        val op = Clause.Operator.ISNT_ONE_OF
        assertThat(op.evaluate("option1\noption2\noption3", "option4")).isTrue()
        assertThat(op.evaluate("option1\noption2\noption3", "option2")).isFalse()
        assertThat(op.evaluate("option1\noption2\noption3", "option1")).isFalse()
        assertThat(op.toString()).isEqualTo("isn't one of")
    }

    @Test
    fun testNumericOperators() {
        // Greater than
        assertThat(Clause.Operator.IS_GREATER_THAN.evaluate("10", "5")).isTrue()
        assertThat(Clause.Operator.IS_GREATER_THAN.evaluate("5", "5")).isFalse()
        assertThat(Clause.Operator.IS_GREATER_THAN.evaluate("5", "10")).isFalse()
        assertThat(Clause.Operator.IS_GREATER_THAN.toString()).isEqualTo("is greater than")

        // Greater or equal
        assertThat(Clause.Operator.IS_GREATER_OR_EQUAL.evaluate("10", "5")).isTrue()
        assertThat(Clause.Operator.IS_GREATER_OR_EQUAL.evaluate("5", "5")).isTrue()
        assertThat(Clause.Operator.IS_GREATER_OR_EQUAL.evaluate("5", "10")).isFalse()
        assertThat(Clause.Operator.IS_GREATER_OR_EQUAL.toString()).isEqualTo("is greater than or equal to")

        // Less than
        assertThat(Clause.Operator.IS_LESS_THAN.evaluate("5", "10")).isTrue()
        assertThat(Clause.Operator.IS_LESS_THAN.evaluate("5", "5")).isFalse()
        assertThat(Clause.Operator.IS_LESS_THAN.evaluate("10", "5")).isFalse()
        assertThat(Clause.Operator.IS_LESS_THAN.toString()).isEqualTo("is less than")

        // Less or equal
        assertThat(Clause.Operator.IS_LESS_OR_EQUAL.evaluate("5", "10")).isTrue()
        assertThat(Clause.Operator.IS_LESS_OR_EQUAL.evaluate("5", "5")).isTrue()
        assertThat(Clause.Operator.IS_LESS_OR_EQUAL.evaluate("10", "5")).isFalse()
        assertThat(Clause.Operator.IS_LESS_OR_EQUAL.toString()).isEqualTo("is less than or equal to")
    }

    @Test
    fun testDecimalNumericOperators() {
        // Greater than
        assertThat(Clause.Operator.IS_GREATER_THAN.evaluate("10.25", "10.10")).isTrue()
        assertThat(Clause.Operator.IS_GREATER_THAN.evaluate("10.10", "10.10")).isFalse()
        assertThat(Clause.Operator.IS_GREATER_THAN.evaluate("10.10", "10.25")).isFalse()

        // Greater or equal
        assertThat(Clause.Operator.IS_GREATER_OR_EQUAL.evaluate("10.25", "10.10")).isTrue()
        assertThat(Clause.Operator.IS_GREATER_OR_EQUAL.evaluate("10.10", "10.10")).isTrue()
        assertThat(Clause.Operator.IS_GREATER_OR_EQUAL.evaluate("10.10", "10.25")).isFalse()

        // Less than
        assertThat(Clause.Operator.IS_LESS_THAN.evaluate("10.10", "10.25")).isTrue()
        assertThat(Clause.Operator.IS_LESS_THAN.evaluate("10.10", "10.10")).isFalse()
        assertThat(Clause.Operator.IS_LESS_THAN.evaluate("10.25", "10.10")).isFalse()

        // Less or equal
        assertThat(Clause.Operator.IS_LESS_OR_EQUAL.evaluate("10.10", "10.25")).isTrue()
        assertThat(Clause.Operator.IS_LESS_OR_EQUAL.evaluate("10.10", "10.10")).isTrue()
        assertThat(Clause.Operator.IS_LESS_OR_EQUAL.evaluate("10.25", "10.10")).isFalse()
    }

    @Test
    fun testNegativeNumericOperators() {
        // Greater than
        assertThat(Clause.Operator.IS_GREATER_THAN.evaluate("-5", "-10")).isTrue()
        assertThat(Clause.Operator.IS_GREATER_THAN.evaluate("-5", "-5")).isFalse()
        assertThat(Clause.Operator.IS_GREATER_THAN.evaluate("-10", "-5")).isFalse()

        // Greater or equal
        assertThat(Clause.Operator.IS_GREATER_OR_EQUAL.evaluate("-5", "-10")).isTrue()
        assertThat(Clause.Operator.IS_GREATER_OR_EQUAL.evaluate("-5", "-5")).isTrue()
        assertThat(Clause.Operator.IS_GREATER_OR_EQUAL.evaluate("-10", "-5")).isFalse()

        // Less than
        assertThat(Clause.Operator.IS_LESS_THAN.evaluate("-10", "-5")).isTrue()
        assertThat(Clause.Operator.IS_LESS_THAN.evaluate("-5", "-5")).isFalse()
        assertThat(Clause.Operator.IS_LESS_THAN.evaluate("-5", "-10")).isFalse()

        // Less or equal
        assertThat(Clause.Operator.IS_LESS_OR_EQUAL.evaluate("-10", "-5")).isTrue()
        assertThat(Clause.Operator.IS_LESS_OR_EQUAL.evaluate("-5", "-5")).isTrue()
        assertThat(Clause.Operator.IS_LESS_OR_EQUAL.evaluate("-5", "-10")).isFalse()
    }

    @Test
    fun testNumericOperatorsWithNonNumericStrings() {
        // Non-numeric values should throw NumberFormatException, but the evaluate method
        // should handle this gracefully and return false
        assertThat(Clause.Operator.IS_GREATER_THAN.evaluate("5", "a")).isFalse()
        assertThat(Clause.Operator.IS_GREATER_OR_EQUAL.evaluate("5", "a")).isFalse()
        assertThat(Clause.Operator.IS_LESS_THAN.evaluate("5", "a")).isFalse()
        assertThat(Clause.Operator.IS_LESS_OR_EQUAL.evaluate("5", "a")).isFalse()
    }

    @Test
    fun testOperatorRawValues() {
        assertThat(Clause.Operator.EQUALS.rawValue).isEqualTo("==")
        assertThat(Clause.Operator.DOESNT_EQUAL.rawValue).isEqualTo("!=")
        assertThat(Clause.Operator.CONTAINS.rawValue).isEqualTo("*")
        assertThat(Clause.Operator.DOESNT_CONTAIN.rawValue).isEqualTo("!*")
        assertThat(Clause.Operator.STARTS_WITH.rawValue).isEqualTo("^")
        assertThat(Clause.Operator.DOESNT_START_WITH.rawValue).isEqualTo("!^")
        assertThat(Clause.Operator.ENDS_WITH.rawValue).isEqualTo("$")
        assertThat(Clause.Operator.DOESNT_END_WITH.rawValue).isEqualTo("!$")
        assertThat(Clause.Operator.MATCHES_REGEX.rawValue).isEqualTo("regex")
        assertThat(Clause.Operator.IS_ONE_OF.rawValue).isEqualTo("in")
        assertThat(Clause.Operator.ISNT_ONE_OF.rawValue).isEqualTo("not in")
        assertThat(Clause.Operator.IS_GREATER_THAN.rawValue).isEqualTo(">")
        assertThat(Clause.Operator.IS_GREATER_OR_EQUAL.rawValue).isEqualTo(">=")
        assertThat(Clause.Operator.IS_LESS_THAN.rawValue).isEqualTo("<")
        assertThat(Clause.Operator.IS_LESS_OR_EQUAL.rawValue).isEqualTo("<=")
    }

    // MARK: - JSON Decoding Tests

    @Test
    fun testDecodeAndClause() {
        val json = """
        {
            "and": [
                {
                    "token": {
                        "token": "user_type",
                        "operator": "==",
                        "value": "premium"
                    }
                },
                {
                    "survey": {
                        "block": "123e4567-e89b-12d3-a456-426614174000",
                        "operator": "*",
                        "value": "contains"
                    }
                }
            ]
        }
        """.trimIndent()

        val clause = moshi.adapter(Clause::class.java).fromJson(json)
        assertThat(clause).isNotNull()
        
        when (clause) {
            is Clause.And -> {
                assertThat(clause.conditions).hasSize(2)
                
                val firstCondition = clause.conditions[0]
                assertThat(firstCondition).isInstanceOf(Clause.Token::class.java)
                if (firstCondition is Clause.Token) {
                    assertThat(firstCondition.clause.token).isEqualTo("user_type")
                    assertThat(firstCondition.clause.operator).isEqualTo(Clause.Operator.EQUALS)
                    assertThat(firstCondition.clause.value).isEqualTo("premium")
                }
                
                val secondCondition = clause.conditions[1]
                assertThat(secondCondition).isInstanceOf(Clause.Survey::class.java)
                if (secondCondition is Clause.Survey) {
                    assertThat(secondCondition.clause.block).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                    assertThat(secondCondition.clause.operator).isEqualTo(Clause.Operator.CONTAINS)
                    assertThat(secondCondition.clause.value).isEqualTo("contains")
                }
            }
            else -> Assert.fail("Expected and clause")
        }
    }

    @Test
    fun testDecodeOrClause() {
        val json = """
        {
            "or": [
                {
                    "survey": {
                        "block": "123e4567-e89b-12d3-a456-426614174000",
                        "operator": "!=",
                        "value": "wrong_value"
                    }
                }
            ]
        }
        """.trimIndent()

        val clause = moshi.adapter(Clause::class.java).fromJson(json)
        assertThat(clause).isNotNull()
        
        when (clause) {
            is Clause.Or -> {
                assertThat(clause.conditions).hasSize(1)
                
                val condition = clause.conditions[0]
                assertThat(condition).isInstanceOf(Clause.Survey::class.java)
                if (condition is Clause.Survey) {
                    assertThat(condition.clause.block).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                    assertThat(condition.clause.operator).isEqualTo(Clause.Operator.DOESNT_EQUAL)
                    assertThat(condition.clause.value).isEqualTo("wrong_value")
                }
            }
            else -> Assert.fail("Expected or clause")
        }
    }

    @Test
    fun testDecodeNotClause() {
        val json = """
        {
            "not": {
                "survey": {
                    "block": "123e4567-e89b-12d3-a456-426614174000",
                    "operator": "^",
                    "value": "prefix"
                }
            }
        }
        """.trimIndent()

        val clause = moshi.adapter(Clause::class.java).fromJson(json)
        assertThat(clause).isNotNull()
        
        when (clause) {
            is Clause.Not -> {
                assertThat(clause.clause).isInstanceOf(Clause.Survey::class.java)
                if (clause.clause is Clause.Survey) {
                    assertThat(clause.clause.clause.block).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                    assertThat(clause.clause.clause.operator).isEqualTo(Clause.Operator.STARTS_WITH)
                    assertThat(clause.clause.clause.value).isEqualTo("prefix")
                }
            }
            else -> Assert.fail("Expected not clause")
        }
    }

    @Test
    fun testDecodeSurveyClause() {
        val json = """
        {
            "survey": {
                "block": "123e4567-e89b-12d3-a456-426614174000",
                "operator": "$",
                "value": "suffix"
            }
        }
        """.trimIndent()

        val clause = moshi.adapter(Clause::class.java).fromJson(json)
        assertThat(clause).isNotNull()
        
        when (clause) {
            is Clause.Survey -> {
                assertThat(clause.clause.block).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                assertThat(clause.clause.operator).isEqualTo(Clause.Operator.ENDS_WITH)
                assertThat(clause.clause.value).isEqualTo("suffix")
            }
            else -> Assert.fail("Expected survey clause")
        }
    }

    @Test
    fun testDecodeTokenClause() {
        val json = """
        {
            "token": {
                "token": "user_type",
                "operator": "in",
                "value": "premium\nvip\nadmin"
            }
        }
        """.trimIndent()

        val clause = moshi.adapter(Clause::class.java).fromJson(json)
        assertThat(clause).isNotNull()
        
        when (clause) {
            is Clause.Token -> {
                assertThat(clause.clause.token).isEqualTo("user_type")
                assertThat(clause.clause.operator).isEqualTo(Clause.Operator.IS_ONE_OF)
                assertThat(clause.clause.value).isEqualTo("premium\nvip\nadmin")
            }
            else -> Assert.fail("Expected token clause")
        }
    }

    @Test
    fun testDecodeComplexNestedClause() {
        val json = """
        {
            "and": [
                { "or": [
                    { "token": { "token": "user_type", "operator": "==", "value": "premium" }},
                    { "survey": { "block": "123e4567-e89b-12d3-a456-426614174000", "operator": ">", "value": "5" }}
                ]},
                { "not": { "token": { "token": "user_status", "operator": "==", "value": "blocked" }}}
            ]
        }
        """.trimIndent()

        val clause = moshi.adapter(Clause::class.java).fromJson(json)
        assertThat(clause).isNotNull()
        
        when (clause) {
            is Clause.And -> {
                assertThat(clause.conditions).hasSize(2)
                
                val firstCondition = clause.conditions[0]
                assertThat(firstCondition).isInstanceOf(Clause.Or::class.java)
                if (firstCondition is Clause.Or) {
                    assertThat(firstCondition.conditions).hasSize(2)
                    
                    val firstOrCondition = firstCondition.conditions[0]
                    assertThat(firstOrCondition).isInstanceOf(Clause.Token::class.java)
                    if (firstOrCondition is Clause.Token) {
                        assertThat(firstOrCondition.clause.token).isEqualTo("user_type")
                        assertThat(firstOrCondition.clause.operator).isEqualTo(Clause.Operator.EQUALS)
                        assertThat(firstOrCondition.clause.value).isEqualTo("premium")
                    }
                    
                    val secondOrCondition = firstCondition.conditions[1]
                    assertThat(secondOrCondition).isInstanceOf(Clause.Survey::class.java)
                    if (secondOrCondition is Clause.Survey) {
                        assertThat(secondOrCondition.clause.operator).isEqualTo(Clause.Operator.IS_GREATER_THAN)
                        assertThat(secondOrCondition.clause.value).isEqualTo("5")
                    }
                }
                
                val secondCondition = clause.conditions[1]
                assertThat(secondCondition).isInstanceOf(Clause.Not::class.java)
                if (secondCondition is Clause.Not) {
                    assertThat(secondCondition.clause).isInstanceOf(Clause.Token::class.java)
                    if (secondCondition.clause is Clause.Token) {
                        assertThat(secondCondition.clause.clause.token).isEqualTo("user_status")
                        assertThat(secondCondition.clause.clause.operator).isEqualTo(Clause.Operator.EQUALS)
                        assertThat(secondCondition.clause.clause.value).isEqualTo("blocked")
                    }
                }
                
                val expectedDescription = "user_type equals premium " +
                    "OR 123e4567-e89b-12d3-a456-426614174000 is greater than 5 " +
                    "AND NOT user_status equals blocked"
                assertThat(clause.toString()).isEqualTo(expectedDescription)
            }
            else -> Assert.fail("Expected and clause")
        }
    }

    @Test
    fun testDecodeUnknownClause() {
        val json = """
        {
            "unknown_key": "some_value"
        }
        """.trimIndent()

        val clause = moshi.adapter(Clause::class.java).fromJson(json)
        assertThat(clause).isNotNull()
        assertThat(clause).isInstanceOf(Clause.Unknown::class.java)
    }

    @Test
    fun testDecodeEmptyObject() {
        val json = "{}"
        val clause = moshi.adapter(Clause::class.java).fromJson(json)
        assertThat(clause).isNotNull()
        assertThat(clause).isInstanceOf(Clause.Unknown::class.java)
    }

    @Test
    fun testDecodeMultipleKeysError() {
        val json = """
        {
            "and": [],
            "or": []
        }
        """.trimIndent()

        val clause = moshi.adapter(Clause::class.java).fromJson(json)
        assertThat(clause).isNull()
    }

    @Test
    fun testDecodeInvalidUUID() {
        val json = """
        {
            "survey": {
                "block": "invalid-uuid",
                "operator": "==",
                "value": "test"
            }
        }
        """.trimIndent()

        val clause = moshi.adapter(Clause::class.java).fromJson(json)
        assertThat(clause).isNotNull()
        assertThat(clause).isInstanceOf(Clause.Unknown::class.java)
    }

    @Test
    fun testDecodeInvalidSurveyOperator() {
        val json = """
        {
            "survey": {
                "block": "123e4567-e89b-12d3-a456-426614174000",
                "operator": "invalid_operator",
                "value": "test"
            }
        }
        """.trimIndent()

        val clause = moshi.adapter(Clause::class.java).fromJson(json)
        assertThat(clause).isNotNull()
        assertThat(clause).isInstanceOf(Clause.Unknown::class.java)
    }

    @Test
    fun testDecodeInvalidTokenOperator() {
        val json = """
        {
            "token": {
                "token": "test",
                "operator": "unknown",
                "value": "test"
            }
        }
        """.trimIndent()

        val clause = moshi.adapter(Clause::class.java).fromJson(json)
        assertThat(clause).isNotNull()
        assertThat(clause).isInstanceOf(Clause.Unknown::class.java)
    }

    // MARK: - Integration Tests

    @Test
    fun testComplexClauseEvaluation() {
        val tokenClause1 = Clause.TokenClause("user_type", Clause.Operator.EQUALS, "premium")
        val tokenClause2 = Clause.TokenClause("user_status", Clause.Operator.EQUALS, "active")
        val tokenClause3 = Clause.TokenClause("user_role", Clause.Operator.DOESNT_EQUAL, "admin")

        val orClause = Clause.Or(listOf(Clause.Token(tokenClause1), Clause.Token(tokenClause2)))
        val notClause = Clause.Not(Clause.Token(tokenClause3))
        val andClause = Clause.And(listOf(orClause, notClause))

        // (user_type == "premium" OR user_status == "active") AND NOT (user_role != "admin")
        // (true OR true) AND NOT (true) = true AND false = false
        assertThat(andClause.evaluate(emptyMap())).isFalse()
    }

    @Test
    fun testMixedClauseTypes() {
        val block = UUID.randomUUID()
        val state = mapOf(block to "yes")

        val surveyClause = Clause.SurveyClause(block, Clause.Operator.EQUALS, "yes")
        val tokenClause = Clause.TokenClause("premium", Clause.Operator.EQUALS, "premium")

        val andClause = Clause.And(listOf(Clause.Survey(surveyClause), Clause.Token(tokenClause)))
        
        assertThat(andClause.evaluate(state)).isTrue()
        assertThat(andClause.toString()).isEqualTo("$block equals yes AND premium equals premium")
    }

    @Test
    fun testMixedClauseTypesWithFalseCondition() {
        val block = UUID.randomUUID()
        val state = mapOf(block to "no")

        val orClause = Clause.Or(listOf(
            Clause.Survey(Clause.SurveyClause(block, Clause.Operator.EQUALS, "yes")),
            Clause.Token(Clause.TokenClause("premium", Clause.Operator.EQUALS, "premium"))
        ))

        // Survey condition is false, but token condition is true, so OR should be true
        assertThat(orClause.evaluate(state)).isTrue()
        assertThat(orClause.toString()).isEqualTo("$block equals yes OR premium equals premium")
    }
}
